package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
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

    /**
     * Number of milliseconds that special responses are delayed before being sent.
     */
    private static final int SPECIAL_RESPONSE_DELAY_IN_MS = 5000;

    /**
     * Number of milliseconds after which we terminate a client due to inactivity.
     * This is currently equal to 2 mins (for testing purposes).
     */
    private static final long TERMINATE_AFTER_INACTIVE_IN_MS = 120000;

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

    private String ip;

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

    private SQLDB db;

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
    private static final String ADDRESS = "IP";


    /**
     * Create a new thread with which we will communicate with this single client.
     *
     * @param client SocketChannel over which we will communicate with this new
     *               client
     * @throws IOException Exception thrown if we have trouble completing this
     *                     connection
     */
    public ClientRunnable(SocketChannel client) throws IOException {
        // initialize SQLDB
        db = SQLDB.getInstance();
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
                .setTimeInMillis(terminateInactivity.getTimeInMillis() + TERMINATE_AFTER_INACTIVE_IN_MS);
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
            // Update the time until we terminate this client due to inactivity.
            terminateInactivity.setTimeInMillis(
                    new GregorianCalendar().getTimeInMillis() + TERMINATE_AFTER_INACTIVE_IN_MS);
            Message msg = input.nextMessage();
            initialize(msg);
        }
    }

    private void initialize(Message msg) {
        if (setUserName(msg.getSender())) {
            // Set that the client is initialized.
            initialized = true;
        } else {
            initialized = false;
        }
    }

    /**
     * Check is the user is validate (if the new user - save his credentials in db)
     */
    private void checkForValidation() {
        // Check if there are any input messages to read
        if (input.hasNextMessage()) {
            terminateInactivity
                    .setTimeInMillis(terminateInactivity.getTimeInMillis() + TERMINATE_AFTER_INACTIVE_IN_MS);
            // If a message exists, try to use it to initialize the connection
            Message msg = input.nextMessage();
            validate(msg);
        }
    }

    private void validate(Message msg) {
        password = msg.getText();
        name = msg.getSender();
        if (msg.isSignupMessage() && !db.checkUser(getName())) {
            db.create(getUserId(), getName(), password, socket.socket().getInetAddress().toString(), 0);
            db.setIP(this.getName(), ip);
            validated = true;
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "Nice to meet you " + getName() + "! Remember your credentials to be able to sign-in in future."), getName());
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "If you are not familiar with the service we provide, user [HELP] command to get the instructions."), getName());
        } else if (msg.isSigninMessage() && db.validateCredentials(getName(), password)) {
            db.setIP(this.getName(), ip);
            int role = db.getUserRole(this.getName());
            if (role == 0) {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You are an admin. REMEMBER: With Great Power Comes Great Responsibility!"), getName());
            } else if (role == 1) {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "Welcome back " + getName() + "! You successfully signed-in."), getName());
            } else if (role == 2) {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You are an agency. You can wiretap other users and groups."), getName());
            }
            validated = true;
            sendAllQueuedMessages();
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "If you are not familiar with the service we provide, user [HELP] command to get the instructions."), getName());

        } else {
            validated = false;
            if (msg.isSignupMessage()){
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "Failed. Username is already taken."), getName());
            } else if (msg.isSigninMessage()){
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "Failed. Wrong credentials. Try again."), getName());
            } else {
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You need to sign-in/sign-up first."), getName());
            }
        }
    }


    public void signin(String username, String password) {
        this.name = username;
        this.initialize(Message.makeLoginMessage(username));
        this.validate(Message.makeSigninMessage(username, password));
    }


    public void signup(String username, String password) {
        this.name = username;
        this.initialize(Message.makeLoginMessage(username));
        this.validate(Message.makeSignupMessage(username, password));
    }


    /**
     * send all queued messages from all senders to respective user
     */
    private void sendAllQueuedMessages() {
        Timestamp lastSeen = db.retrieveLastSeen(getName());
        if (lastSeen != null) {
            List<String> queuedMessages = db.getAllQueuedMessagesForUser(getName(), lastSeen);
            if (!queuedMessages.isEmpty()){
                Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "We keep track of incoming messages you receive while being offline. Here is the list of all new messages:"), getName());
            }
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
        }
        try {
            respond();
        } finally {
            // When it is appropriate, terminate the current client.
            if (terminate) {
                terminateClient();
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
            terminateInactivity.setTimeInMillis(
                    new GregorianCalendar().getTimeInMillis() + TERMINATE_AFTER_INACTIVE_IN_MS);
            // Get the next message
            Message msg = input.nextMessage();
            // Update the time until we terminate the client for
            // inactivity.
            executeRequest(msg);
        }
    }

    /**
     * Depending on the user input the handles are considered and passed on to the server,
     * for implementing the CRUD operations.
     *
     * @param msg Message which we are interested in and that needs to be executed by the server
     */
    @SuppressWarnings("all")
    private void executeRequest(Message msg) {
        if (msg.terminate()) {
            terminate();
        }
        if (msg.isDirectMessage()) {
            directMessage(msg);
        } else if (msg.isGroupMessage()) {
            groupMessage(msg);
        } else if (msg.isRetrieveMessage()) {
            retrieve(msg);
        } else if (msg.isJoinMessage()) {
            join(msg);
        } else if (msg.isLeaveMessage()) {
            leave(msg);
        } else if (msg.isDeleteMessage()) {
            delete();
        } else if (msg.isUpdateMessage()) {
            update(msg);
        } else if (msg.isDisplayMessage()) {
            display(msg);
        } else if (msg.terminate()) {
            terminate();
        } else if (msg.isRecallMessage()) {
            recallMessage(msg);
        } else if (msg.isWiretapUserMessage()) {
            wiretapUserRequest(msg);
        } else if (msg.isWiretapGroupMessage()) {
            wiretapGroupRequest(msg);
        } else if (msg.isApproveMessage()) {
            wiretapApprove(msg);
        } else if (msg.isRejectMessage()) {
            wiretapReject(msg);
        } else if (msg.isSetRoleMessage()) {
            setRole(msg);
        } else if (msg.isLoggerMessage()) {
            logger(msg);
        } else if (msg.isPControlMessage()) {
            pcontrol(msg);
        } else if (msg.isHelpMessage()) {
            help(msg);
        }
    }


    private void help(Message msg){
        String help = "When you successfully log in, you can:\n" +
                "-communicate with the server by typing special messages like \"Hello\"\n" +
                "-send messages to everyone by simply typing your message.\n" +
                "-send direct messages by typing user>your_message (where user is the name of the user you want to talk to)\n" +
                "-send same messages to many users by typing user1,user2,user3>your_message\n" +
                "-send group messages by typing group>>your_message\n" +
                "-send files by typing user>file (::text.txt, picture.png, etc. - files from the resources/send folder)\n" +
                "*when message is received it is saved in the resources/receive folder in the destination user machine\n" +
                "\n" +
                "CRUD functionality:\n" +
                "-UPDATE new_password (updates the password of the current user)\n" +
                "-DELETE (deletes currently logged in user)\n" +
                "-JOIN group (adds current user to the group. creates group if there is no one)\n" +
                "-LEAVE group (removes current user from the group. deletes group if it is empty)\n" +
                "-RECALL <id> (recall certain message by the message id)\n" +
                "\n" +
                "-RETRIEVE PASSWORD(tells current user's password)\n" +
                "-RETRIEVE EPASSWORD(tells current user's encrypted password)\n" +
                "-RETRIEVE GROUPS (displays the name of all existing groups)\n" +
                "-RETRIEVE GROUP group (displays the users that are part of the group) *only if you are part of the group*\n" +
                "-RETRIEVE SEND_MESSAGES (displays all messages sent by the user ordered by time)\n" +
                "-RETRIEVE RECEIVE_MESSAGES (displays direct message received by the user ordered by time)\n" +
                "-RETRIEVE GROUP_MESSAGES group(displays all messages for a particular group) *only if you are part of the group*\n" +
                "-RETRIEVE USERS (all users in the database)\n" +
                "-RETRIEVE ONLINE (only online users)\n" +
                "-RETRIEVE ROLE (role of the current user)\n" +
                "\n" +
                "for admin:\n" +
                "\n" +
                "-RETRIEVE SENDER <username> (retrieve all messages sent by the username)\n" +
                "-RETRIEVE RECEIVER <username> (retrieve all messages retrieved by the username)\n" +
                "-RETRIEVE CONTENT <content>(retrieve message by the content)\n" +
                "-RETRIEVE DATE <yyyy-mm-dd> (retrieve messages by the date)\n" +
                "-RETRIEVE REQUESTS (retrieve all wiretap requests)\n" +
                "-LOGGER (toggle the logger on/off)\n" +
                "-PARENT_CONTROL (toogle the parent control on/off)\n" +
                "-<username> ROLE <role> (set the role of the user: 0-admin, 1-user, 2-agency)\n" +
                "-<agency> APPROVE <id> (approve certain wiretap request)\n" +
                "-<agency> APPROVE * (approve all wiretap requests for the agency)\n" +
                "-<agency> REJECT <id> (reject certain wiretap request)\n" +
                "\n" +
                "for agency:\n" +
                "-RETRIEVE WIRETAPS (Retrieve all wiretaps for current agency)\n" +
                "- [username]%>%[n] (wiretap user for n days)\n" +
                "- [groupname]%>>%[n] (wiretap group for n days)";
        Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), help), msg.getSender());
    }


    /**
     * method used to update recall status for last message send by user
     */
    private void recallMessage(Message msg) {

        if (db.setRecallFlagMessage(getName(), Integer.parseInt(msg.getText()))) {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "Your message was successfully recalled"), msg.getSender());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You dont have permission to recal this message"), msg.getSender());

        }
    }

    private void wiretapUserRequest(Message msg) {
        db.requestWiretap(msg.getSender(), msg.getReceiver(), 0, Integer.parseInt(msg.getText()));
    }

    private void wiretapGroupRequest(Message msg) {
        db.requestWiretap(msg.getSender(), msg.getReceiver(), 1, Integer.parseInt(msg.getText()));
    }

    private void setRole(Message msg) {
        if (db.getUserRole(this.getName()) == 0) {
            switch (msg.getText()) {
                case "user":
                    db.updateUserRole(msg.getReceiver(), 1);
                    Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "You've been granted a user role"), msg.getReceiver());
                    break;
                case "admin":
                    db.updateUserRole(msg.getReceiver(), 0);
                    Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "You've been granted an admin role"), msg.getReceiver());
                    break;
                case "agency":
                    db.updateUserRole(msg.getReceiver(), 2);
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
        Level level = Logger.getRootLogger().getLevel();
        if (db.getUserRole(this.getName()) == 0) {
            if (level == Level.DEBUG) {
                Logger.getRootLogger().setLevel(Level.OFF);
            } else if (level == Level.OFF) {
                Logger.getRootLogger().setLevel(Level.DEBUG);
            }
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to modify logger status"), msg.getSender());

        }
    }

    private void pcontrol(Message msg) {
        if (db.getUserRole(this.getName()) == 0) {
            int control = db.getControl(msg.getReceiver());
            if (control == 1) {
                control = 0;
            } else {
                control = 1;
            }
            db.setControl(msg.getReceiver(), control);
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to set parrent controll"), msg.getSender());

        }
    }

    private void wiretapApprove(Message msg) {
        if (db.getUserRole(this.getName()) == 0) {
            if (msg.getText().equals("*")) {
                db.setWireTap(msg.getSender(), msg.getReceiver(), 0);
            } else {
                db.setWireTap(msg.getSender(), null, Integer.parseInt(msg.getText()));
            }
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "Your wiretap request is approved"), msg.getReceiver());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to approve wiretaps"), msg.getSender());
        }

    }

    private void wiretapReject(Message msg) {
        if (db.getUserRole(this.getName()) == 0) {
            db.deleteWiretapRequest(Integer.parseInt(msg.getText()));
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getReceiver(), "Your wiretap request is rejected"), msg.getReceiver());

        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, msg.getSender(), "You are not permitted to reject wiretaps"), msg.getSender());

        }

    }


    /**
     * For communication between two users
     *
     * @param msg message sent from one user to other
     */
    private void directMessage(Message msg) {
        // Get list of agencies wiretapping sender and receiver
        Set<String> agencyList = new HashSet<>();
        if (db.isUserOrGroupWiretapped(msg.getSender(), 0)) {
            agencyList.addAll(db.getAgencyList(msg.getSender(), 0, 0));
        }
        if (db.isUserOrGroupWiretapped(msg.getReceiver(), 0)) {
            agencyList.addAll(db.getAgencyList(msg.getReceiver(), 0, 0));
        }
        for (String agency : agencyList) {
            String wiretapMessageAppender = "[ >> " + msg.getReceiver() + " ] " + msg.getText();
            msg.setText(wiretapMessageAppender);
            db.storeMessageIndividual(msg.getSender(), agency, msg.getText(), db.retrieve(msg.getSender(), ADDRESS), db.retrieve(agency, ADDRESS));
            Prattle.directMessage(msg, agency);
        }
        db.storeMessageIndividual(msg.getSender(), msg.getReceiver(), msg.getText(), db.retrieve(msg.getSender(), ADDRESS), db.retrieve(msg.getReceiver(), ADDRESS));
        // Send message to original receiver
        Prattle.directMessage(msg, msg.getReceiver());

    }

    /**
     * for communication between a user and group
     *
     * @param msg message sent from the user to a group/groups
     */
    private void groupMessage(Message msg) {
        String group = msg.getReceiver();

        if (db.checkGroup(group) && db.isGroupMember(group, getName())) {
            List<String> users = db.retrieveGroupMembers(group);
            Set<String> agencyList = new HashSet<>();
            db.storeMessageGroup(msg.getSender(), msg.getReceiver(), msg.getText(), db.retrieve(msg.getSender(), ADDRESS), null);

            // check if the group is being wire tapped
            if (db.isUserOrGroupWiretapped(group, 1)) {
                agencyList.addAll(db.getAgencyList(group, 1, 0));
            }

            // check if the sender is being wire tapped
            if (db.isUserOrGroupWiretapped(msg.getSender(), 0)) {
                agencyList.addAll(db.getAgencyList(msg.getSender(), 0, 0));
            }
            for (String user : users) {
                Prattle.directMessage(msg, user);
            }
            for (String agency : agencyList) {
                String wiretapMessageAppender = "[ >> " + msg.getReceiver() + " ] " + msg.getText();
                msg.setText(wiretapMessageAppender);
                db.storeMessageIndividual(msg.getSender(), agency, msg.getText(), db.retrieve(msg.getSender(), ADDRESS), db.retrieve(msg.getReceiver(), ADDRESS));

                Prattle.directMessage(msg, agency);
            }
        }
    }



    /**
     * Validate if the message is legal, check for special messages and show the message sent/received by the user/server
     *
     * @param msg Messages that needs to be displayed
     */
    private void display(Message msg) {
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
                    db.storeMessageBroadcast(getName(), msg.getText(), db.retrieve(msg.getSender(), ADDRESS), null);
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
        db.update(getName(), msg.getText());
    }

    /**
     * Delete a user/group profile
     */
    private void delete() {
        db.delete(getName());
        this.terminateClient();
    }

    /**
     * Deletes the member from the group
     *
     * @param msg Message with keyword LEAVE
     */
    private void leave(Message msg) {
        String group = msg.getText();
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
    private void retrieve(Message msg) {
        String text = msg.getText();
        if (!simpleRetrieve(text) &&
        !complexRetrieve(text)){
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "Incorrect Request"), getName());
        }
    }

    private boolean complexRetrieve(String text){
        if (text.contains(GROUP) && text.split(GROUP).length == 2) {
            return retrieveGroup(text);
        } else if (text.contains(GROUP_MESSAGES) && text.split(GROUP_MESSAGES).length == 2) {
            return retrieveGroupMessages(text);
        }else if (text.contains(CONTENT) && text.split(CONTENT).length == 2) {
            return retrieveContent(text);
        }else if (text.contains(SENDER) && text.split(SENDER).length == 2) {
            return retrieveSender(text);
        } else if (text.contains(RECEIVER) && text.split(RECEIVER).length == 2) {
            return retrieveReceiver(text);
        } else if (text.contains(DATE) && text.split(DATE).length == 2) {
            return retrieveDate(text);
        } else {
            return false;
        }
    }

    private boolean retrieveDate(String text) {
        if (db.getUserRole(this.getName()) == 0) {
            String content = text.split(DATE)[1];
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(),
                    String.valueOf(db.getAllMessagesDeliveredAtSpecificDate(java.sql.Date.valueOf(content)))), getName());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You are not permitted to search for date"), getName());
        }
        return true;
    }

    private boolean retrieveReceiver(String text) {
        if (db.getUserRole(this.getName()) == 0) {
            String content = text.split(RECEIVER)[1];
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(),
                    String.valueOf(db.getAllMessagesReceivedByReceiver(content))), getName());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You are not permitted to search for receiver"), getName());
        }
        return true;
    }

    private boolean retrieveSender(String text) {
        if (db.getUserRole(this.getName()) == 0) {
            String content = text.split(SENDER)[1];
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(),
                    String.valueOf(db.getAllMessagesSendBySender(content))), getName());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You are not permitted to search for sender"), getName());
        }
        return true;
    }

    private boolean retrieveContent(String text) {
        if (db.getUserRole(this.getName()) == 0) {
            String content = text.split(CONTENT)[1];
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(),
                    String.valueOf(db.getAllMessageBasedOnContent(content))), getName());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, text, "You are not permitted to search for content"), getName());
        }
        return true;
    }

    private boolean retrieveGroupMessages(String text) {
        String group = text.split(GROUP_MESSAGES)[1];
        if (db.checkGroup(group) && db.isGroupMember(group, getName())) {
            String logs = db.getAllMessagesForGroup(getName(), text.split(GROUP_MESSAGES)[1]);
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), logs), this.getName());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "You do not have access to the group!"), getName());
        }
        return true;
    }

    private boolean retrieveGroup(String text) {
        String group = text.split(GROUP)[1];
        if (db.checkGroup(group)) {
            String members = db.retrieveGroupMembers(group).toString();
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), members), getName());
        } else {
            Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "The group does not exist!"), getName());
        }
        return true;
    }

    private boolean simpleRetrieve(String text) {
        switch (text) {
            case EPASWD:
                return retrieveEpaswd();
            case PASWD:
                return retrievePaswd();
            case SEND_MESSAGES:
                return retrieveMessages("fromUser");
            case RECEIVE_MESSAGES:
                return retrieveMessages("toUSer");
            case USERS:
                return retrieveUsers();
            case ONLINE:
                return retrieveOnline();
            case GROUPS:
                return retrieveGroups();
            case WIRETAPS:
                return retrieveWiretaps();
            case REQUESTS:
                return retrieveRequests();
            default:
                return false;
        }
    }

    private boolean retrieveRequests() {
        String result = db.getWiretapRequests(this.getName(), "", 0).toString();
        Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), result), getName());
        return true;
    }

    private boolean retrieveWiretaps() {
        String msgs = db.getWiretappedUsers(this.getName(), 0).toString();
        msgs += db.getWiretappedUsers(this.getName(), 1).toString();
        Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), msgs), getName());
        return true;
    }

    private boolean retrieveGroups() {
        String groups = db.retrieveAllGroups().toString();
        Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), groups), getName());
        return true;
    }

    private boolean retrieveOnline() {
        String users = Prattle.getOnline().toString();
        Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), users), getName());
        return true;
    }

    private boolean retrieveUsers() {
        String users = db.retrieveAllUsers().toString();
        Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), users), getName());
        return true;
    }

    private boolean retrieveMessages(String type) {
        String logs = db.getAllMessagesForUser(getName(), type);
        Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), logs), getName());
        return true;
    }


    private boolean  retrievePaswd() {
        Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "your password is " + password), getName());
        return true;
    }

    private boolean retrieveEpaswd() {
        String epassword = db.retrieve(getName(), "paswd");
        Prattle.directMessage(Message.makeDirectMessage(Prattle.SERVER_NAME, getName(), "your encrypted password is " + epassword), getName());
        return true;
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
     * terminate a client if they logout
     */
    private void terminate() {
        // Reply with a quit message.
        enqueueMessage(Message.makeQuitMessage(name));
        terminate = true;
    }

    /**
     * Terminate a client that we wish to remove. This termination could happen at
     * the client's request or due to system need.
     */
    public void terminateClient() {
        try {
            // Once the communication is done, close this connection.
            if (db.updateLastSeen(this.getName())) {
                input.close();
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.info("unable to terminate");

        } finally {
            // Remove the client from our client listing.
            Prattle.removeClient(this);
            // And remove the client from our client pool.
            runnableMe.cancel(false);
        }
    }
}