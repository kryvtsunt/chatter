package edu.northeastern.ccs.im.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.ScanNetNB;


/**
 * Instances of this class handle all of the incoming communication from a
 * single IM client. Instances are created when the client signs-on with the
 * server. After instantiation, it is executed periodically on one of the
 * threads from the thread pool and will stop being run only when the client
 * signs off.
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class ClientRunnable implements Runnable {

    /* Logger */
    private static final Logger LOGGER = Logger.getLogger(Prattle.class.getName());

    /**
     * Number of milliseconds that special responses are delayed before being sent.
     */
    private static final int SPECIAL_RESPONSE_DELAY_IN_MS = 5000;

    /**
     * Number of milliseconds after which we terminate a client due to inactivity.
     * This is currently equal to 5 hours.
     */
    private static final long TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS = 18000000;

    /**
     * Number of milliseconds after which we terminate a client due to inactivity.
     * This is currently equal to 5 hours.
     */
    private static final long TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS = 600000;

    /**
     * Time at which we should send a response to the (private) messages we were
     * sent.
     */
    private Date sendResponses;

    /**
     * Time at which the client should be terminated due to lack of activity.
     */
    private GregorianCalendar terminateInactivity;

    /**
     * Queue of special Messages that we must send immediately.
     */
    private Queue<Message> immediateResponse;

    /**
     * Queue of special Messages that we will need to send.
     */
    private Queue<Message> specialResponse;

    /**
     * Socket over which the conversation with the single client occurs.
     */
    private final SocketChannel socket;

    /**
     * Utility class which we will use to receive communication from this client.
     */
    private ScanNetNB input;

    /**
     * Utility class which we will use to send communication to this client.
     */
    private PrintNetNB output;

    /**
     * Id for the user for whom we use this ClientRunnable to communicate.
     */
    private int userId;

    /**
     * Name that the client used when connecting to the server.
     */
    private String name;

    /**
     * Whether this client has been initialized, set its user name, and is ready to
     * receive messages.
     */
    private boolean initialized;

    private boolean validated;

    private final static String serverName = "Prattle";

    /**
     * The future that is used to schedule the client for execution in the thread
     * pool.
     */
    private ScheduledFuture<ClientRunnable> runnableMe;

    /**
     * Collection of messages queued up to be sent to this client.
     */
    private static Queue<Message> waitingList;

    /**
     * Create a new thread with which we will communicate with this single client.
     *
     * @param client SocketChannel over which we will communicate with this new
     *               client
     * @throws IOException Exception thrown if we have trouble completing this
     *                     connection
     */
    ClientRunnable(SocketChannel client) throws IOException {
        // Set up the SocketChannel over which we will communicate.
        socket = client;
        socket.configureBlocking(false);
        // Create the class we will use to receive input
        input = new ScanNetNB(socket);
        // Create the class we will use to send output
        output = new PrintNetNB(socket);
        // Mark that we are not initialized
        initialized = false;
        // Create our queue of special messages
        specialResponse = new LinkedList<>();
        // Create the queue of messages to be sent
        waitingList = new ConcurrentLinkedQueue<>();
        // Create our queue of message we must respond to immediately
        immediateResponse = new LinkedList<>();
        // Mark that the client is active now and start the timer until we
        // terminate for inactivity.
        terminateInactivity = new GregorianCalendar();
        terminateInactivity
                .setTimeInMillis(terminateInactivity.getTimeInMillis() + TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
    }

    /**
     * Determines if this is a special message which we handle differently. It will
     * handle the messages and return true if msg is "special." Otherwise, it
     * returns false.
     *
     * @param msg Message in which we are interested.
     * @return True if msg is "special"; false otherwise.
     */
    private boolean broadcastMessageIsSpecial(Message msg) {
        boolean result = false;
        String text = msg.getText();
        if (text != null) {
            List<Message> responses = ServerConstants.getBroadcastResponses(text);
            if (responses != null) {
                for (Message current : responses) {
                    handleSpecial(current);
                }
                result = true;
            }
        }
        return result;
    }

    /**
     * Check to see for an initialization attempt and process the message sent.
     */
    private void checkForInitialization() {
        // Check if there are any input messages to read
        if (input.hasNextMessage()) {
            // If a message exists, try to use it to initialize the connection
            Message msg = input.nextMessage();
            if (setUserName(msg.getName())) {
                // Update the time until we terminate this client due to inactivity.
                terminateInactivity.setTimeInMillis(
                        new GregorianCalendar().getTimeInMillis() + TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
                // Set that the client is initialized.
                initialized = true;

            } else {
                initialized = false;
            }
        }
    }

    private void checkForValidation() {
        String password = null;

        // Check if there are any input messages to read
        if (input.hasNextMessage()) {
            // If a message exists, try to use it to initialize the connection
            Message msg = input.nextMessage();
            String passwordInput = msg.getText();

            try {
                password = PrattleDB.instance().retrieve(this.getName());
            } catch (FileNotFoundException ae) {
                LOGGER.info(ae.toString());
            }

            if (password == null) {
                try {
                    PrattleDB.instance().create(getName(), passwordInput);
                } catch (IOException ae) {
                    LOGGER.info(ae.toString());
                }
                validated = true;
                Prattle.directMessage(Message.makeBroadcastMessage(serverName, "Nice to meet you " + getName() + "! Remember your credentials to be able to log in in future."), getName());
                return;
            }

            if (passwordInput.equals(password)) {
                validated = true;
                Prattle.directMessage(Message.makeBroadcastMessage(serverName, "Welcocme back " + getName() + "! You are successfully logged in."), getName());
            } else {
                validated = false;
            }
        }
    }

    /**
     * Process one of the special responses
     *
     * @param msg Message to add to the list of special responses.
     */
    private void handleSpecial(Message msg) {
        if (specialResponse.isEmpty()) {
            sendResponses = new Date();
            sendResponses.setTime(sendResponses.getTime() + SPECIAL_RESPONSE_DELAY_IN_MS);
        }
        specialResponse.add(msg);
    }

    /**
     * Check if the message is properly formed. At the moment, this means checking
     * that the identifier is set properly.
     *
     * @param msg Message to be checked
     * @return True if message is correct; false otherwise
     */
    private boolean messageChecks(Message msg) {
        // Check that the message name matches.
        return (msg.getName() != null) && (msg.getName().compareToIgnoreCase(getName()) == 0);
    }

    /**
     * Immediately send this message to the client. This returns if we were
     * successful or not in our attempt to send the message.
     *
     * @param message Message to be sent immediately.
     * @return True if we sent the message successfully; false otherwise.
     */
    private boolean sendMessage(Message message) {
        LOGGER.log(Level.INFO,  message.toString());
        return output.print(message);
    }

    /**
     * Try allowing this user to set his/her user name to the given username.
     *
     * @param userName The new value to which we will try to set userName.
     * @return True if the username is deemed acceptable; false otherwise
     */
    private boolean setUserName(String userName) {
        // Now make sure this name is legal.
        if (userName != null) {
            // Optimistically set this users ID number.
            setName(userName);
            userId = hashCode();
            return true;
        }
        // Clear this name; we cannot use it. *sigh*
        return false;
    }


    /**
     * Add the given message to this client to the queue of message to be sent to
     * the client.
     *
     * @param message Complete message to be sent.
     */
    void enqueueMessage(Message message) {
        waitingList.add(message);
    }

    /**
     * Get the name of the user for which this ClientRunnable was created.
     *
     * @return Returns the name of this client.
     */
    String getName() {
        return name;
    }

    /**
     * Set the name of the user for which this ClientRunnable was created.
     *
     * @param name The name for which this ClientRunnable.
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the user for which this ClientRunnable was created.
     *
     * @return Returns the current value of userName.
     */
    int getUserId() {
        return userId;
    }

    /**
     * Return if this thread has completed the initialization process with its
     * client and is read to receive messages.
     *
     * @return True if this thread's client should be considered; false otherwise.
     */
    boolean isInitialized() {
        return initialized;
    }

    boolean isValidated() {
        return validated;
    }

    /**
     * Perform the periodic actions needed to work with this client.
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        boolean terminate = false;

        // The client must be initialized before we can do anything else
        if (!initialized) {
            checkForInitialization();

        } else if (!validated) {
            checkForValidation();
        } else {
            try {
                // Client has already been initialized, so we should first check
                // if there are any input
                // messages.
                if (input.hasNextMessage()) {
                    // Get the next message
                    Message msg = input.nextMessage();
                    // Update the time until we terminate the client for
                    // inactivity.
                    terminateInactivity.setTimeInMillis(
                            new GregorianCalendar().getTimeInMillis() + TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS);
                    // If the message is a broadcast message, send it out
                    if (msg.getText() != null && msg.getText().contains(">")) {
                        String[] args = msg.getText().split(">");
                        String destination = args[0];
                        String content = args[1];
                        String[] to = destination.split(",");
                        for (String user : to) {
                            Prattle.directMessage(Message.makeBroadcastMessage(msg.getName(), content), user);
                        }
                    } else if (msg.getText() != null && msg.getText().contains("DELETE")) {
                        try {
                            PrattleDB.instance().delete(getName());
                            this.terminateClient();
                            return;
                        } catch (IOException ae) {
                            LOGGER.info(ae.toString());
                        }
                    } else if (msg.getText() != null && msg.getText().contains("UPDATE")) {
                        try {
                            PrattleDB.instance().update(getName(), msg.getText().split("UPDATE ")[1]);
                            return;
                        } catch (IOException ae) {
                            LOGGER.info( ae.toString());
                        }
                    } else if (msg.getText() != null && msg.getText().contains("RETRIEVE")) {
                        try {
                            String password = PrattleDB.instance().retrieve(getName());
                            Prattle.directMessage(Message.makeBroadcastMessage(serverName, password), this.getName());

                        } catch (IOException ae) {
                            LOGGER.info(ae.toString());
                        }
                    } else if (msg.isDisplayMessage()) {
                        // Check if the message is legal formatted
                        if (messageChecks(msg)) {
                            // Check for our "special messages"
                            if ((msg.isBroadcastMessage()) && (!broadcastMessageIsSpecial(msg))) {
                                // Check for our "special messages"
                                if ((msg.getText() != null)
                                        && (msg.getText().compareToIgnoreCase(ServerConstants.BOMB_TEXT) == 0)) {
                                    initialized = false;
                                    Prattle.broadcastMessage(Message.makeQuitMessage(name));
                                } else {
                                    Prattle.broadcastMessage(msg);
                                }
                            }
                        } else {
                            Message sendMsg;
                            sendMsg = Message.makeBroadcastMessage(ServerConstants.BOUNCER_ID,
                                    "Last message was rejected because it specified an incorrect user name.");
                            enqueueMessage(sendMsg);
                        }
                    } else if (msg.terminate()) {
                        // Stop sending the poor client message.
                        terminate = true;
                        // Reply with a quit message.
                        enqueueMessage(Message.makeQuitMessage(name));
                    }
                    // Otherwise, ignore it (for now).
                }
                if (!immediateResponse.isEmpty()) {
                    while (!immediateResponse.isEmpty()) {
                        sendMessage(immediateResponse.remove());
                    }
                }

                // Check to make sure we have a client to send to.
                boolean processSpecial = !specialResponse.isEmpty()
                        && ((!initialized) || (!waitingList.isEmpty()) || sendResponses.before(new Date()));
                boolean keepAlive = !processSpecial;
                // Send the responses to any special messages we were asked.
                if (processSpecial) {
                    // Send all of the messages and check that we get valid
                    // responses.
                    while (!specialResponse.isEmpty()) {
                        keepAlive |= sendMessage(specialResponse.remove());
                    }
                }
                if (!waitingList.isEmpty()) {
                    if (!processSpecial) {
                        keepAlive = false;
                    }
                    // Send out all of the message that have been added to the
                    // queue.
                    do {
                        Message msg = waitingList.remove();
                        boolean sentGood = sendMessage(msg);
                        keepAlive |= sentGood;
                    } while (!waitingList.isEmpty());
                }
                terminate |= !keepAlive;
            } finally {
                // When it is appropriate, terminate the current client.
                if (terminate) {
                    terminateClient();
                }
            }
        }
        // Finally, check if this client have been inactive for too long and,
        // when they have, terminate
        // the client.
        if (!terminate && terminateInactivity.before(new GregorianCalendar())) {
            LOGGER.log(Level.INFO, "Timing out or forcing off a user " + name);
            terminateClient();
        }
    }

    /**
     * Store the object used by this client runnable to control when it is scheduled
     * for execution in the thread pool.
     *
     * @param future Instance controlling when the runnable is executed from within
     *               the thread pool.
     */
    void setFuture(ScheduledFuture<ClientRunnable> future) {
        runnableMe = future;
    }

    /**
     * Terminate a client that we wish to remove. This termination could happen at
     * the client's request or due to system need.
     */
    private void terminateClient() {
        try {
            // Once the communication is done, close this connection.
            input.close();
            socket.close();
        } catch (IOException ae) {
            LOGGER.info( ae.toString());
        } finally {
            // Remove the client from our client listing.
            Prattle.removeClient(this);
            // And remove the client from our client pool.
            runnableMe.cancel(false);
        }
    }

    Queue<Message> getWaitingList() {
        return new ConcurrentLinkedQueue<>(waitingList);
    }
}