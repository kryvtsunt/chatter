package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Level;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.ScanNetNB;
import org.apache.log4j.Logger;


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
    public static final Logger LOGGER = Logger.getLogger(Prattle.class.getName());


    static boolean PARENT_CONTROL = false;


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
     * Password the client used when connecting to the server
     */
    private String password;

    /**
     * Whether this client has been initialized, set its user name, and is ready to
     * receive messages.
     */
    private boolean initialized;

    /**
     * Whether this client is validated, confirms is the username and password match
     */
    private boolean validated;

    /**
     * To terminate a clients session. Scenarios include: inactivity and logoff
     */
    private boolean terminate;

    /**
     * The future that is used to schedule the client for execution in the thread
     * pool.
     */
    private ScheduledFuture<ClientRunnable> runnableMe;

    /**
     * Collection of messages queued up to be sent to this client.
     */
    private Queue<Message> waitingList;

    /**
     * Keyword in the user input for CRUD operations. Used to view the messages
     * in a group in which the user is present
     */
    private static final String GROUP_MESSAGES = "GROUP_MESSAGES ";

    /**
     * Keyword in the user input for CRUD operations. Used by the user to join a group
     */
    private static final String GROUP = "GROUP ";

    /**
     * Keyword in the user input for CRUD operations. Used to view the users messages
     */
    private static final String SEND_MESSAGES = "SEND_MESSAGES";

    private static final String RECEIVE_MESSAGES = "RECEIVE_MESSAGES";

    /**
     * Keyword in the user input for CRUD operations. Used to see all the existing groups
     */
    private static final String GROUPS = "GROUPS";

    /**
     * Keyword in the user input for CRUD operations. Used to see all the users in the database
     */
    private static final String USERS = "USERS";

    /**
     * Keyword in the user input for CRUD operations. Used to see only the online users
     */
    private static final String ONLINE = "ONLINE";

    /**
     * Keyword in the user input for CRUD operations. Used to see the current user's password
     */
    private static final String PASWD = "PASSWORD";

    /**
     * Keyword in the user input for CRUD operations. Used to see the current user's encrypted password
     */
    private static final String EPASWD = "EPASSWORD";

    private static final String REQUESTS = "REQUESTS";

    private static final String SENDER = "SENDER ";
    private static final String RECEIVER = "RECEIVER ";
    private static final String CONTENT = "CONTENT ";
    private static final String DATE = "DATE ";


    private static final String WIRETAPS = "WIRETAPS";

    private String ip;


    /**
     * Create a new thread with which we will communicate with this single client.
     *
     * @param client SocketChannel over which we will communicate with this new
     *               client
     * @throws IOException Exception thrown if we have trouble completing this
     *                     connection
     */
    public ClientRunnable(SocketChannel client) throws IOException {
        // Set up the SocketChannel over which we will communicate.
        socket = client;
        socket.configureBlocking(false);
        // Create the class we will use to receive input
        input = new ScanNetNB(socket);
        socket.getRemoteAddress();
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
        terminate = false;

        ip = socket.getRemoteAddress().toString();
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
            if (setUserName(msg.getSender())) {
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

    /**
     * Check is the user is validate (if the new user - save his credentials in db)
     */
    private void checkForValidation() {
        // Check if there are any input messages to read
        if (input.hasNextMessage()) {
            // If a message exists, try to use it to initialize the connection
            Message msg = input.nextMessage();
            password = msg.getText();
            SQLDB db = SQLDB.getInstance();
            if (!db.checkUser(getName())) {
                SQLDB.getInstance().create(getUserId(), getName(), password);
                this.ip = SQLDB.getInstance().getIP(getName());
                validated = true;
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "Nice to meet you " + getName() + "! Remember your credentials to be able to log in in future."), getName());
                return;
            }

            if (db.validateCredentials(getName(), password)) {
                this.ip = SQLDB.getInstance().getIP(getName());
                validated = true;
                int role = db.getUserRole(this.getName());
                if (role == 0) {
                    Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You are an admin. REMEMBER: With Great Power Comes Great Responsibility!"), getName());
                } else if (role == 1) {
                    Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "Welcome back " + getName() + "! You are successfully logged in. "), getName());
                } else if (role == 2) {
                    Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You are an agency. You can wiretap other users and groups"), getName());
                }
                sendAllQueuedMessages();

            } else {
                validated = false;
            }
        }
    }

    /**
     * send all queued messages from all senders to respective user
     */
    private void sendAllQueuedMessages() {
        Timestamp lastSeen = null;
        try {
            lastSeen = SQLDB.getInstance().retrieveLastSeen(this.getName());
        } catch (Exception e) {
            SQLDB.getInstance().updateLastSeen(this.getName());
            return;
        }
        if (lastSeen != null) {
            List<String> queuedMessages = SQLDB.getInstance().getAllQueuedMessagesForUser(getName(), lastSeen);
            for (String msg : queuedMessages) {
                String fromUser = msg.split(",")[0].split(":")[1];
                String message = msg.split(",")[1].split(":")[1];
                Prattle.directMessage(Message.makeDirectMessage(fromUser, getName(), message), getName());
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
        return (msg.getSender() != null) && (msg.getSender().compareToIgnoreCase(getName()) == 0);
    }

    /**
     * Immediately send this message to the client. This returns if we were
     * successful or not in our attempt to send the message.
     *
     * @param message Message to be sent immediately.
     * @return True if we sent the message successfully; false otherwise.
     */
    private boolean sendMessage(Message message) {
        String str = "\t" + message.toString();
        LOGGER.log(Level.INFO, str);
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
    public void enqueueMessage(Message message) {
        waitingList.add(message);
    }

    /**
     * Get the name of the user for which this ClientRunnable was created.
     *
     * @return Returns the name of this client.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the user for which this ClientRunnable was created.
     *
     * @param name The name for which this ClientRunnable.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the user for which this ClientRunnable was created.
     *
     * @return Returns the current value of userName.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Return if this thread has completed the initialization process with its
     * client and is read to validate.
     *
     * @return True if this thread's client should be considered; false otherwise.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Return if this thread has completed the validation process with its
     * client and is read to receive messages.
     *
     * @return True if this thread's client should be considered; false otherwise.
     */
    public boolean isValidated() {
        return validated;
    }

    /**
     * Perform the periodic actions needed to work with this client.
     *
     * @see Thread#run()
     */
    public void run() {
        // The client must be initialized and validated before we can do anything else
        if (!initialized) {
            checkForInitialization();
        } else if (!validated) {
            checkForValidation();
        } else {
            try {
                respond();
            } finally {
                // When it is appropriate, terminate the current client.
                if (terminate) {
                    terminateClient();
                }
            }
        }
        // Finally, check if this client have been inactive for too long and, when they have, terminate
        // the client.
        terminateInactive();
    }

    /**
     * Method that is responsible for an active communication between the server and a validated user.
     */
    private void respond() {
        // Client has already been initialized, so we should first check
        // if there are any input
        // messages.
        respondIncoming();
        respondImmediate();

        // Check to make sure we have a client to send to.
        boolean processSpecial = !specialResponse.isEmpty()
                && ((!initialized) || (!waitingList.isEmpty()) || sendResponses.before(new Date()));
        boolean keepAlive = !processSpecial;
        // Send the responses to any special messages we were asked.
        keepAlive = respondSpecial(processSpecial, keepAlive);
        keepAlive = respondWaiting(processSpecial, keepAlive);
        terminate |= !keepAlive;
    }

    /**
     * If a user is logged in for a long time without activity, their connection will be terminated by this method
     */
    private void terminateInactive() {
        if (!terminate && terminateInactivity.before(new GregorianCalendar())) {
            String str = "Timing out or forcing off a user " + name;
            LOGGER.log(Level.INFO, str);
            terminateClient();
        }
    }

    /**
     * To keep the thread active between user and server to send/receive messages
     *
     * @param processSpecial boolean to process queued messages
     * @param keepAlive      boolean which determines if the thread needs to be active or not
     * @return true if the queued message is valid and needs a response
     */
    private boolean respondWaiting(boolean processSpecial, boolean keepAlive) {
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
        return keepAlive;
    }

    /**
     * To keep the thread active between user and server to send/receive special messages
     *
     * @param processSpecial boolean to check if the message is considered special
     * @param keepAlive      boolean to keep the thread active
     * @return true if the message is special and needs a response
     */
    private boolean respondSpecial(boolean processSpecial, boolean keepAlive) {
        if (processSpecial) {
            // Send all of the messages and check that we get valid
            // responses.
            while (!specialResponse.isEmpty()) {
                keepAlive |= sendMessage(specialResponse.remove());
            }
        }
        return keepAlive;
    }

    /**
     * For an immediate response from the server to the user
     */
    private void respondImmediate() {
        if (!immediateResponse.isEmpty()) {
            while (!immediateResponse.isEmpty()) {
                sendMessage(immediateResponse.remove());
            }
        }
    }

    /**
     * Gets the next message from the user's console
     */
    private void respondIncoming() {
        if (input.hasNextMessage()) {
            // Get the next message
            Message msg = input.nextMessage();
            if (PARENT_CONTROL) {
                if (msg.getText() != null) {
                    msg.controlText();
                }
            }
            // Update the time until we terminate the client for
            // inactivity.
            terminateInactivity.setTimeInMillis(
                    new GregorianCalendar().getTimeInMillis() + TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS);
            executeRequest(msg);
        }
    }

    /**
     * Depending on the user input the handles are considered and passed on to the server,
     * for implementing the CRUD operations.
     *
     * @param msg Message which we are interested in and that needs to be executed by the server
     */
    private void executeRequest(Message msg) {
        // If the message is a direct message, send it out
        if (msg.isDirectMessage()) {
            directMessage(msg);
        } else if (msg.isGroupMessage()) {
            groupMessage(msg);
        }
        // If the message is a RETRIEVE
        else if (msg.isRetrieveMessage()) {
            retrieve(msg);
        }
        // If the message is a RETRIEVE user
        else if (msg.isJoinMessage()) {
            join(msg);
        } else if (msg.isLeaveMessage()) {
            leave(msg);
        }
        // If the message is a DELETE user
        else if (msg.isDeleteMessage()) {
            delete();
        }
        // If the message is a UPDATE user
        else if (msg.isUpdateMessage()) {
            update(msg);
        } else if (msg.isDisplayMessage()) {
            display(msg);
        } else if (msg.terminate()) {
            terminate();
        }
        //if message is a recall message
        else if (msg.isRecall()) {
            recallMessage(msg);
        } else if (msg.isWiretapUserMessage()) {
            wiretapUserRequest(msg);
        } else if (msg.isWiretapGroupMessage()) {
            wiretapGroupRequest(msg);
        }
        // admin msgs
        else if (msg.isApproveMessage()) {
            wiretapApprove(msg);
        } else if (msg.isRejectMessage()) {
            wiretapReject(msg);
        } else if (msg.isSetRoleMessage()) {
            setRole(msg);
        } else if (msg.isLoggerMessage()) {
            logger(msg);
        } else if (msg.isPControlMessage()) {
            pcontrol(msg);
        }
        // Otherwise, ignore it (for now).
    }


    /**
     * method used to update recall status for last message send by user
     */
    private void recallMessage(Message msg) {

        if (SQLDB.getInstance().setRecallFlagMessage(getName(), Integer.parseInt(msg.getText()))) {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "Your message was successfully recalled"), msg.getSender());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You dont have permission to recal this message"), msg.getSender());

        }
    }

    private void wiretapUserRequest(Message msg) {
        SQLDB.getInstance().requestWiretap(msg.getSender(), msg.getReceiver(), 0, Integer.parseInt(msg.getText()));
    }

    private void wiretapGroupRequest(Message msg) {
        SQLDB.getInstance().requestWiretap(msg.getSender(), msg.getReceiver(), 1, Integer.parseInt(msg.getText()));
    }

    private void setRole(Message msg) {
        if (SQLDB.getInstance().getUserRole(this.getName()) == 0) {
            switch (msg.getText()) {
                case "user":
                    SQLDB.getInstance().updateUserRole(msg.getReceiver(), 1);
                    Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "You've been granted a user role"), msg.getReceiver());
                    break;
                case "admin":
                    SQLDB.getInstance().updateUserRole(msg.getReceiver(), 0);
                    Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "You've been granted an admin role"), msg.getReceiver());
                    break;
                case "agency":
                    SQLDB.getInstance().updateUserRole(msg.getReceiver(), 2);
                    Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "You've been granted an agency role"), msg.getReceiver());
                    break;
                default:
                    Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "Incorrect request"), msg.getSender());
                    break;
            }
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify user's role"), msg.getSender());

        }
    }

    private void logger(Message msg) {
        if (SQLDB.getInstance().getUserRole(this.getName()) == 0) {
            System.out.println("logger");
            if (LOGGER.getRootLogger().getLevel() == Level.OFF) {
                System.out.println("off");
                LOGGER.getRootLogger().setLevel(Level.INFO);
            } else {
                System.out.println("on");
                LOGGER.getRootLogger().setLevel(Level.OFF);
            }
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify user's role"), msg.getSender());

        }
    }

    private void pcontrol(Message msg) {
        if (SQLDB.getInstance().getUserRole(this.getName()) == 0) {
            PARENT_CONTROL = !PARENT_CONTROL;
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify user's role"), msg.getSender());

        }
    }

    private void wiretapApprove(Message msg) {
        if (SQLDB.getInstance().getUserRole(this.getName()) == 0) {
            if (msg.getText().equals("*")) {
                SQLDB.getInstance().setWireTap(msg.getSender(), msg.getReceiver());
            } else {
                SQLDB.getInstance().setWireTap(msg.getSender(), Integer.parseInt(msg.getText()));
            }
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "Your wiretap request is approved"), msg.getReceiver());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify user's role"), msg.getSender());
        }

    }

    private void wiretapReject(Message msg) {
        if (SQLDB.getInstance().getUserRole(this.getName()) == 0) {
            SQLDB.getInstance().deleteWiretapRequest(Integer.parseInt(msg.getText()));
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "Your wiretap request is rejected"), msg.getReceiver());

        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify user's role"), msg.getSender());

        }

    }


    /**
     * For communication between two users
     *
     * @param msg message sent from one user to other
     */
    private void directMessage(Message msg) {
        SQLDB db = SQLDB.getInstance();

        // Get list of agencies wiretapping sender and receiver
        List<String> agencyList = new ArrayList<>();
        if (SQLDB.getInstance().isUserOrGroupWiretapped(msg.getSender(), 0)) {
            agencyList.addAll(db.getAgencyList(msg.getSender(), 0, 0));
        }
        if (SQLDB.getInstance().isUserOrGroupWiretapped(msg.getReceiver(), 0)) {
            agencyList.addAll(db.getAgencyList(msg.getReceiver(), 0, 0));
        }
        for (String agency : agencyList) {
            db.storeMessageIndividual(msg.getSender(), msg.getReceiver(), msg.getText(), db.getIP(msg.getSender()), db.getIP(agency));
            Prattle.directMessage(msg, agency);
        }
        db.storeMessageIndividual(msg.getSender(), msg.getReceiver(), msg.getText(), db.getIP(msg.getSender()), db.getIP(msg.getReceiver()));
        // Send message to original receiver
        Prattle.directMessage(msg, msg.getReceiver());

    }

    /**
     * for communication between a user and group
     *
     * @param msg message sent from the user to a group/groups
     */
    private void groupMessage(Message msg) {
        SQLDB db = SQLDB.getInstance();
        String group = msg.getReceiver();
        if (db.checkGroup(group) && db.isGroupMember(group, getName())) {
            List<String> users = db.retrieveGroupMembers(group);
            db.storeMessageGroup(msg.getSender(), msg.getReceiver(), msg.getText(), db.getIP(msg.getSender()), null);

            // check if the group is being wire tapped
            if (SQLDB.getInstance().isUserOrGroupWiretapped(group, 1)) {
                users.addAll(db.getAgencyList(group, 1, 0));
            }

            // check if the sender is being wire tapped
            if (SQLDB.getInstance().isUserOrGroupWiretapped(msg.getSender(), 0)) {
                users.addAll(db.getAgencyList(msg.getSender(), 0, 0));
            }
            for (String user : users) {
                Prattle.directMessage(msg, user);
            }
        }
    }

    /**
     * terminate a client if they logout
     */
    private void terminate() {
        // Reply with a quit message.
        enqueueMessage(Message.makeQuitMessage(name));
        terminate = true;
    }


    /**
     * Validate if the message is legal, check for special messages and show the message sent/received by the user/server
     *
     * @param msg Messages that needs to be displayed
     */
    private void display(Message msg) {
        SQLDB db = SQLDB.getInstance();
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
                    db.storeMessageBroadcast(getName(), msg.getText(), db.getIP(msg.getSender()), null);
                    Prattle.broadcastMessage(msg);
                }
            }
        } else {
            Message sendMsg;
            sendMsg = Message.makeBroadcastMessage(ServerConstants.BOUNCER_ID,
                    "Last message was rejected because it specified an incorrect user name.");
            enqueueMessage(sendMsg);
        }
    }

    /**
     * Update the user/group profile
     *
     * @param msg Message containing the keyword UPDATE
     */
    private void update(Message msg) {
        password = msg.getText();
        SQLDB.getInstance().update(getName(), msg.getText());
    }

    /**
     * Delete a user/group profile
     */
    private void delete() {
        SQLDB.getInstance().delete(getName());
        this.terminateClient();
    }

    /**
     * Deletes the member from the group
     *
     * @param msg Message with keyword LEAVE
     */
    private void leave(Message msg) {
        String group = msg.getText();
        SQLDB db = SQLDB.getInstance();
        if (db.checkGroup(group)) {
            db.deleteGroupMember(group, getName());
            if (db.retrieveGroupMembers(group).isEmpty()) {
                db.deleteGroup(group);
            }
        }
    }

    /**
     * Lets a user join a group if it exists
     *
     * @param msg Message with keyword JOIN
     */
    private void join(Message msg) {
        String group = msg.getText();
        SQLDB db = SQLDB.getInstance();
        if (!db.checkGroup(group)) {
            db.createGroup(group);
        }
        db.addGroupMember(group, getName());
    }

    /**
     * Method which handles all the requests from the user, it performs all the individual and group CRUD operations
     *
     * @param msg Message with keyword handle
     *            -UPDATE <new_password></new_password> (updates the password of the current user)
     *            -DELETE (deletes currently logged in user)
     *            -JOIN group (adds current user to the group. creates group if there is no one)
     *            -LEAVE group (removes current user from the group. deletes group if it is empty)
     *            -RETRIEVE PASSWORD(tells current user's password)
     *            -RETRIEVE EPASSWORD(tells current user's encrypted password)
     *            -RETRIEVE GROUPS (displays the name of all existing groups)
     *            -RETRIEVE GROUP <group>.</group> (displays the users that are part of the group) *only if you are part of the group*
     *            -RETRIEVE MESSAGES (displays all messages sent by the user ordered by time)
     *            -RETRIEVE GROUP_MESSAGES group(displays all messages for a particular group) *only if you are part of the group*
     *            -RETRIEVE USERS (all users in the database)
     *            -RETRIEVE ONLINE (only online users)
     */
    @SuppressWarnings("all")
    private void retrieve(Message msg) {
        if (msg.getText().equals(EPASWD)) {
            String epassword = SQLDB.getInstance().retrieve(getName());
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "your encrypted password is " + epassword), getName());
        } else if (msg.getText().equals(PASWD)) {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "your password is " + password), getName());
        } else if (msg.getText().equals(SEND_MESSAGES)) {
            String logs = SQLDB.getInstance().getAllMessagesForUser(getName(), "fromUser");
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), logs), getName());
        } else if (msg.getText().equals(RECEIVE_MESSAGES)) {
            String logs = SQLDB.getInstance().getAllMessagesForUser(getName(), "toUser");
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), logs), getName());
        } else if (msg.getText().contains(GROUP_MESSAGES) && msg.getText().split(GROUP_MESSAGES).length == 2) {
            String group = msg.getText().split(GROUP_MESSAGES)[1];
            if (SQLDB.getInstance().checkGroup(group) && SQLDB.getInstance().isGroupMember(group, getName())) {
                String logs = SQLDB.getInstance().getAllMessagesForGroup(getName(), msg.getText().split(GROUP_MESSAGES)[1]);
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), logs), this.getName());
            } else {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You do not have access to the group!"), this.getName());
            }
        } else if (msg.getText().equals(USERS)) {
            String users = SQLDB.getInstance().retrieveAllUsers().toString();
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), users), getName());
        } else if (msg.getText().equals(ONLINE)) {
            String users = Prattle.getOnline().toString();
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), users), getName());
        } else if (msg.getText().equals(GROUPS)) {
            String groups = SQLDB.getInstance().retrieveAllGroups().toString();
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), groups), getName());
        } else if (msg.getText().contains(GROUP) && msg.getText().split(GROUP).length == 2) {
            String group = msg.getText().split(GROUP)[1];
            if (SQLDB.getInstance().checkGroup(group)) {
                String members = SQLDB.getInstance().retrieveGroupMembers(group).toString();
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), members), getName());
            } else {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "The group does not exist!"), this.getName());
            }
        } else if (msg.getText().contains(REQUESTS)) {
            String result = SQLDB.getInstance().getWiretapRequests(this.getName(), "", 0).toString();
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), result), this.getName());
        } else if (msg.getText().contains(CONTENT) && msg.getText().split(CONTENT).length == 2) {
            if (SQLDB.getInstance().getUserRole(this.getName()) == 0) {
                String content = msg.getText().split(CONTENT)[1];
                StringBuilder msgs = new StringBuilder();
                msgs.append(SQLDB.getInstance().getAllMessageBasedOnContent(content));
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), msgs.toString()), getName());
            } else {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify user's role"), msg.getSender());

            }
        } else if (msg.getText().contains(SENDER) && msg.getText().split(SENDER).length == 2) {
            if (SQLDB.getInstance().getUserRole(this.getName()) == 0) {
                String content = msg.getText().split(SENDER)[1];
                StringBuilder msgs = new StringBuilder();
                msgs.append(SQLDB.getInstance().getAllMessagesSendBySender(content));
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), msgs.toString()), getName());
            } else {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify user's role"), msg.getSender());

            }
        } else if (msg.getText().contains(RECEIVER) && msg.getText().split(RECEIVER).length == 2) {
            if (SQLDB.getInstance().getUserRole(this.getName()) == 0) {
                String content = msg.getText().split(RECEIVER)[1];
                StringBuilder msgs = new StringBuilder();
                msgs.append(SQLDB.getInstance().getAllMessagesReceivedByReceiver(content));
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), msgs.toString()), getName());
            } else {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify user's role"), msg.getSender());

            }
        } else if (msg.getText().contains(DATE) && msg.getText().split(DATE).length == 2) {
            if (SQLDB.getInstance().getUserRole(this.getName()) == 0) {
                String content = msg.getText().split(DATE)[1];
                StringBuilder msgs = new StringBuilder();
                msgs.append(SQLDB.getInstance().getAllMessagesDeliveredAtSpecificDate(java.sql.Date.valueOf(content)));
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), msgs.toString()), getName());
            } else {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify user's role"), msg.getSender());

            }
        } else if (msg.getText().contains(WIRETAPS)) {
            String msgs = SQLDB.getInstance().getWiretappedUsers(this.getName(), 0).toString();
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), msgs), getName());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "Incorect Request"), getName());
        }
    }

    /**
     * Store the object used by this client runnable to control when it is scheduled
     * for execution in the thread pool.
     *
     * @param future Instance controlling when the runnable is executed from within
     *               the thread pool.
     */
    public void setFuture(ScheduledFuture<ClientRunnable> future) {
        runnableMe = future;
    }

    /**
     * Terminate a client that we wish to remove. This termination could happen at
     * the client's request or due to system need.
     */
    public void terminateClient() {
        try {
            // Once the communication is done, close this connection.
            SQLDB.getInstance().updateLastSeen(this.getName());
            input.close();
            socket.close();
        } catch (IOException e) {
            LOGGER.info("unable to terminate");

        } finally {
            // Remove the client from our client listing.
            Prattle.removeClient(this);
            // And remove the client from our client pool.
            runnableMe.cancel(false);
        }
    }

    /**
     * Method created solely for testing purpose
     *
     * @return all the messages enqueued to the server
     */
    public Queue<Message> getWaitingList() {
        return new ConcurrentLinkedQueue<>(waitingList);
    }
}