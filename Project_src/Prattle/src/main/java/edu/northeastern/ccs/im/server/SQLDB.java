package edu.northeastern.ccs.im.server;

import java.security.MessageDigest;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Logger;


@SuppressWarnings("all")
public class SQLDB {
    final static String CONNECTION_URL = "aaw5ywu4d6dc4k.c7ohnssvtfpy.us-east-1.rds.amazonaws.com";
    final static String DB_PORT = "3306";
    final static String DB_NAME = "team105";
    final static String DB_USERNAME = "team105";
    final static String DB_PASWD = "Team-105";
    final static int USER_ROLE_NORMAL_ID = 1;
    final static int USER_ROLE_AGENCY_ID = 2;
    final static int USER_ROLE_ADMIN_ID = 0;

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(SQLDB.class.getName());

    /**
     * Database connection
     */
    private Connection connection;

    /**
     * Instance of the Database
     */
    private static SQLDB instance;

    /**
     * Constructor which establishes the connection between DB and JDBC
     */
    private SQLDB() {
        connection = null;
        try {
            connection = DriverManager.
                    getConnection("jdbc:mysql://" + CONNECTION_URL + ":" + DB_PORT + "/" + DB_NAME, DB_USERNAME, DB_PASWD);
        } catch (SQLException e) {
            LOGGER.info("Connection Failed!:\n" + e.getMessage());
        }
    }

    /**
     * @return instance of the Database
     */
    public static SQLDB getInstance() {
        if (instance == null) {
            instance = new SQLDB();
        }
        return instance;
    }

    /**
     * resets the Database connection
     */
    public void reset() {
        instance = null;
    }

    /**
     * Close the DB connection
     *
     * @throws SQLException
     */
    public void close() throws SQLException {
        connection.close();
    }

    /**
     * To validate a user, the user's credentials are compared against the users table in MySQL
     *
     * @param username string entered by the user which is used for validating
     * @return true if the user exists in the Database
     */
    public boolean checkUser(String username) {
        boolean flag = false;
        try {
            String sqlCheckUser = "SELECT COUNT(*) FROM users WHERE username=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser)) {
                pStatement.setString(1, username);
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        flag = userSet.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return flag;
    }

    /**
     * Retreive the user's data from the Database
     *
     * @param username keyword against which the sql operations are carried on
     * @return the user's details stored in the Database
     */
    public String retrieve(String username) {
        String userInformation = null;
        try {
            if (checkUser(username)) {
                String sqlCreateUser = "SELECT * FROM users WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setString(1, username);
                    try (ResultSet userSet = pStatement.executeQuery()) {
                        while (userSet.next()) {
                            userInformation = userSet.getString("paswd");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return userInformation;
    }

    /**
     * SPRINT 3(PREM)
     * Retreive the user's lastseen from the Database
     *
     * @param username keyword against which the sql operations are carried on
     * @return the user's lastseen stored in the Database
     */
    public Timestamp retrieveLastSeen(String username) {
        Timestamp lastSeen = null;
        try {
            if (checkUser(username)) {
                String sqlCreateUser = "SELECT lastSeen FROM users WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setString(1, username);
                    try (ResultSet userSet = pStatement.executeQuery()) {
                        while (userSet.next()) {
                            lastSeen = userSet.getTimestamp("lastSeen");
                            //adding time difference between java and mysql time
                            lastSeen.setTime(lastSeen.getTime() + ((5 * 60 * 60) * 1000));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return lastSeen;
    }

    /**
     * creates a user if they don't exist in the database
     *
     * @param userId   integer which acts a primary key in the Database
     * @param username name the user wants to have
     * @param password string entered by the user for their password
     * @return true if the details entered are in a legal format and when they stored in the Database
     */
    public boolean create(int userId, String username, String password, String ip, int control) {
        boolean flag = false;
        try {
            if (!checkUser(username)) {
                String sqlCreateUser = "INSERT INTO users (userId, username, paswd, ip, control) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setInt(1, userId);
                    pStatement.setString(2, username);
                    pStatement.setString(3, encryptPassword(password));
                    pStatement.setString(4, ip);
                    pStatement.setInt(5, control);
                    int userCount = pStatement.executeUpdate();
                    flag = (userCount > 0);
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * if the user wants to update their informatin escpecially their password
     *
     * @param username user who is trying to update their password
     * @param password new password the user wants to update to
     * @return true if the sql operation of updating is successful
     */
    public boolean update(String username, String password) {
        boolean flag = false;
        try {
            if (checkUser(username)) {
                String sqlCreateUser = "UPDATE users SET paswd=? WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setString(1, encryptPassword(password));
                    pStatement.setString(2, username);
                    int userCount = pStatement.executeUpdate();
                    flag = (userCount > 0);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * SPRINT 3(PREM)
     * if the user wants to update their username
     *
     * @param oldUsername user who is trying to update their last seen
     * @return true if the sql operation of updating is successful
     */
    public boolean updateLastSeen(String oldUsername) {
        boolean flag = false;
        try {
            if (checkUser(oldUsername)) {
                String sqlCreateUser = "UPDATE users SET lastSeen=? WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    pStatement.setString(2, oldUsername);
                    int userCount = pStatement.executeUpdate();
                    flag = (userCount > 0);
                    System.out.println("updation flag:" + flag);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }


    /**
     * deletes a user from the Database
     *
     * @param username name that is supposed to be deleted
     * @return true if the sql operation of deletion is successful
     */
    public boolean delete(String username) {
        boolean flag = false;
        try {
            String sqlDeleteUser = "DELETE FROM users WHERE username=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlDeleteUser)) {
                pStatement.setString(1, username);
                int userCount = pStatement.executeUpdate();
                flag = (userCount > 0);
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * The first step of establishing a connection
     *
     * @param username users input of their username
     * @param password users input of their password
     * @return true if the credentials match the Database and the sql operation of matching succeeds
     */
    public boolean validateCredentials(String username, String password) {
        boolean flag = false;
        try {
            String encryptedPassword = encryptPassword(password);
            String dbPaswd = "";
            if (checkUser(username)) {
                String sqlCheckUser = "SELECT paswd FROM users WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser)) {
                    pStatement.setString(1, username);
                    try (ResultSet userSet = pStatement.executeQuery()) {
                        while (userSet.next()) {
                            dbPaswd = userSet.getString("paswd");
                        }
                    }
                }
            }
            flag = (encryptedPassword.equals(dbPaswd));
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * users password when created, is stored in MD5 format
     * (hehe, we are not stupid to store as a plain string)
     *
     * @param password string that needs encryption
     * @return the encrypted password
     */
    public String encryptPassword(String password) {
        StringBuilder hexConversion = new StringBuilder();
        try {
            MessageDigest encryptAlgorithm = MessageDigest.getInstance("MD5");

            byte[] bytes = encryptAlgorithm.digest(password.getBytes());

            for (int i = 0; i < bytes.length; i++) {
                hexConversion.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (Exception e) {
            LOGGER.info("Caught NoSuchAlgorithmException:" + e.toString());
        }

        return hexConversion.toString();
    }

    /**
     * A group must be in the database for a user to join, this method validates it
     *
     * @param groupName the group name user wants to join or to retrive messages from
     * @return true if there exists a group in the Database and the SQL operation is successful
     */
    public boolean checkGroup(String groupName) {
        boolean flag = false;
        try {
            String sqlCheckGroup = "SELECT COUNT(*) FROM groups WHERE groupName=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlCheckGroup)) {
                pStatement.setString(1, groupName);
                try (ResultSet groupsSet = pStatement.executeQuery()) {
                    while (groupsSet.next()) {
                        flag = groupsSet.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * If a group doesn't exists it can be create or if a user wants, they can crete one
     *
     * @param groupName name of the group
     * @return true if the sql operation of creating a group with a valid name is successful
     */
    public boolean createGroup(String groupName) {
        boolean flag = false;
        try {
            if (!checkGroup(groupName)) {
                String sqlCreateUser = "INSERT INTO groups (groupName) VALUES (?)";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setString(1, groupName);
                    int groupCount = pStatement.executeUpdate();
                    flag = (groupCount > 0);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * you don't want a group? no problem just delete it
     *
     * @param groupName name which needs to be deleted
     * @return true if the groupname exixts and the sql operation of deleting is successful
     */
    public boolean deleteGroup(String groupName) {
        boolean flag = false;
        try {
            String sqlDeleteUser = "DELETE FROM groups WHERE groupName=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlDeleteUser)) {
                pStatement.setString(1, groupName);
                int groupCount = pStatement.executeUpdate();
                flag = (groupCount > 0);
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;

    }

    /**
     * well, you might want to change the groupname at some point
     *
     * @param groupName    the existing group
     * @param newGroupName new name for the group
     * @return true if group name exists, new name is valid and the sql operation is successful
     */
    public boolean updateGroup(String groupName, String newGroupName) {
        boolean flag = false;
        try {
            if (checkGroup(groupName)) {
                String sqlCreateUser = "UPDATE groups SET groupName=? WHERE groupName=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setString(1, newGroupName);
                    pStatement.setString(2, groupName);
                    int groupCount = pStatement.executeUpdate();
                    flag = (groupCount > 0);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * retrives the group information
     *
     * @param groupName group that is expected to be retrieved
     * @return list of information of the group which can be user details
     */
    public List<String> retrieveGroup(String groupName) {
        List<String> userInformation = new ArrayList<>();

        try {
            if (checkGroup(groupName)) {
                int groupId = getGroupID(groupName);
                String sqlRetrieveGroup = "SELECT userId FROM groupMembers WHERE groupId=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup)) {
                    pStatement.setInt(1, groupId);
                    try (ResultSet userSet = pStatement.executeQuery()) {
                        while (userSet.next()) {
                            int tempUserId = userSet.getInt("userId");
                            String tempUsername = getUsername(tempUserId);
                            userInformation.add(tempUsername);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return userInformation;

    }

    /**
     * gets the userID for a given user
     *
     * @param username name used to retrive the ID from Database
     * @return the member ID (which is an integer)
     */
    public int getUserID(String username) {
        int userInformation = -1;
        try {
            if (checkUser(username)) {
                String sqlRetrieveGroup = "SELECT userId FROM users WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup)) {
                    pStatement.setString(1, username);
                    try (ResultSet userSet = pStatement.executeQuery()) {
                        while (userSet.next()) {
                            userInformation = userSet.getInt("userId");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return userInformation;
    }


    /**
     * gets the user name
     *
     * @param userId unique interger for retrieval
     * @return the name of the user according to the ID
     */
    public String getUsername(int userId) {
        String userInformation = "";
        try {
            String sqlRetrieveGroup = "SELECT username FROM users WHERE userId=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup)) {
                pStatement.setInt(1, userId);
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        userInformation = userSet.getString("username");
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return userInformation;
    }

    /**
     * Each group is given a unique ID which can be retrieved
     *
     * @param groupName name against which the sql operation is performed
     * @return the unique number of the group
     */
    public int getGroupID(String groupName) {
        int groupInformation = -1;
        try {
            if (checkGroup(groupName)) {
                String sqlRetrieveGroup = "SELECT groupId FROM groups WHERE groupName=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup)) {
                    pStatement.setString(1, groupName);
                    try (ResultSet groupSet = pStatement.executeQuery()) {
                        while (groupSet.next()) {
                            groupInformation = groupSet.getInt("groupId");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return groupInformation;
    }

    /**
     * when the situation of adding a member arises
     *
     * @param groupName group to which an user is added
     * @param username  the user who is expected to be added
     * @return true of the group and user exists and sql operation is successful
     */
    public boolean addGroupMember(String groupName, String username) {
        boolean flag = false;
        try {
            if (checkGroup(groupName)) {
                int userId = getUserID(username);
                int groupId = getGroupID(groupName);
                String sqlAddUser = "INSERT INTO groupMembers (groupId, userId) VALUES (?,?)";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlAddUser)) {
                    pStatement.setInt(1, groupId);
                    pStatement.setInt(2, userId);
                    int groupCount = pStatement.executeUpdate();
                    flag = (groupCount > 0);
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return flag;
    }

    /**
     * stroes the messages for each user
     * @param from user who sent the message
     * @param to user who received the message
     * @param text message sene from one user to other
     * @param senderIP ip address of sender
     * @param receiverIP ip address of receiver
     * @return true from user exists and sql operation is successful
     */
    public boolean storeMessageIndividual(String from, String to, String text, String senderIP, String receiverIP) {
        int userID = getUserID(from);
//        PreparedStatement pStatement = null;
        boolean flag = false;
        try {
            String sql = "INSERT INTO message_details (fromUser, toUser, IsMedia, IsGroupMsg, message, IsBroadcast, senderIP, receiverIP) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setInt(1, userID);
                pStatement.setString(2, to);
                pStatement.setBoolean(3, false);
                pStatement.setBoolean(4, false);
                pStatement.setString(5, text);
                pStatement.setBoolean(6, false);
                pStatement.setString(7,senderIP);
                pStatement.setString(8,receiverIP);
                int msgCount = pStatement.executeUpdate();
                flag = (msgCount > 0);
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return flag;
    }

    /**
     * stores the messages for a group
     * @param from user who sent the message
     * @param group group to which the message is sent
     * @param text the messages sent by the user/users
     * @param senderIP ip address of sender
     * @param receiverIP ip address of receiver
     * @return true if users/groups exists and sql operation is successful
     */
    public boolean storeMessageGroup(String from, String group, String text, String senderIP, String receiverIP) {
        int userID = getUserID(from);
        boolean flag = false;
        try {
            String sql = "INSERT INTO message_details (fromUser, toUser, IsMedia, IsGroupMsg, message, IsBroadcast, senderIP, receiverIP) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setInt(1, userID);
                pStatement.setString(2, group);
                pStatement.setBoolean(3, false);
                pStatement.setBoolean(4, true);
                pStatement.setString(5, text);
                pStatement.setBoolean(6, false);
                pStatement.setString(7,senderIP);
                pStatement.setString(8,receiverIP);
                int msgCount = pStatement.executeUpdate();
                flag = (msgCount > 0);
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return flag;
    }

    /**
     * stores the broadcast messages
     * @param from user who sent the message
     * @param text message sent by the user
     * @param senderIP ip address of sender
     * @param receiverIP ip address of receiver
     * @return true if the user exists and sql operation is successful
     */
    public boolean storeMessageBroadcast(String from, String text, String senderIP, String receiverIP) {
        int userID = getUserID(from);
        boolean flag = false;
        try {
            String sql = "INSERT INTO message_details (fromUser, toUser, IsMedia, IsGroupMsg, message, IsBroadcast, senderIP, receiverIP) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setInt(1, userID);
                pStatement.setString(2, "BROADCAST");
                pStatement.setBoolean(3, false);
                pStatement.setBoolean(4, false);
                pStatement.setString(5, text);
                pStatement.setBoolean(6, true);
                pStatement.setString(7,senderIP);
                pStatement.setString(8,receiverIP);
                int msgCount = pStatement.executeUpdate();
                flag = (msgCount > 0);
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return flag;
    }

    /**
     * SPRINT 3(PREM)
     * retrieve message id of last message send by user
     *
     * @param user name of user
     * @return message id of last message sent by user
     */
    public int getLastMessageID(String user) {
        int msgID = -1;
        try {
            String sql = "SELECT messageID FROM message_details WHERE fromUser = '" + getUserID(user) + "' ORDER BY messageID DESC LIMIT 1";
            try (Statement pStatement = connection.createStatement()) {
                try (ResultSet rs = pStatement.executeQuery(sql)) {
                    while (rs.next()) {
                        msgID = rs.getInt("messageID");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return msgID;
    }

    /**
     * SPRINT 3(PREM)
     * set recall flag to true for last message send by respective user
     *
     * @param userName  the user name
     * @param messageID id of message whose recall flag needs to be set
     * @return true if updation is successful otherwise false
     */
    public boolean setRecallFlagMessage(String userName, int messageID) {
        boolean flag = false;
        try {
            String sqlCreateUser = "UPDATE message_details SET isRecall=? WHERE messageID=? AND fromUser=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                pStatement.setBoolean(1, true);
                pStatement.setInt(2, messageID);
                pStatement.setInt(3, getUserID(userName));
                int msgCount = pStatement.executeUpdate();
                flag = (msgCount > 0);
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * retrieves all the messages of a given user
     *
     * @param user name of the user
     * @return list of all the broadcast, group, individual messsages sent/received by the user
     */
    public String getAllMessagesForUser(String user, String type) {
        String username = "";
        if (type.equals("fromUser")) {
            username = Integer.toString(getUserID(user));
        } else {
            username = user;
        }
        String msgInformation = "";
        SortedMap<Timestamp, String> userHashMap = new TreeMap<Timestamp, String>();
        SortedMap<Timestamp, String> groupHashMap = new TreeMap<Timestamp, String>();
        SortedMap<Timestamp, String> broadcastHashMap = new TreeMap<Timestamp, String>();
        try {
            String sql = "SELECT messageID, fromUser, toUser, IsGroupMsg, message, creationTime, IsBroadcast FROM message_details WHERE " + type + " = '" + username + "'" + "AND isRecall = 0";
            try (Statement pStatement = connection.createStatement()) {
                try (ResultSet rs = pStatement.executeQuery(sql)) {
                    while (rs.next()) {
                        String to = rs.getString("toUser");
                        String from = rs.getString("fromUser");
                        boolean groupMsg = rs.getBoolean("IsGroupMsg");
                        String msg = rs.getString("message");
                        Timestamp t = rs.getTimestamp("creationTime");
                        boolean broadcastMsg = rs.getBoolean("IsBroadcast");
                        int id = rs.getInt("messageID");
                        String fromName = this.getUsername(Integer.parseInt(from));
                        String toName = to;
                        if (groupMsg) {
                            groupHashMap.put(t, id + " TimeStamp:" + t.toString() + " => fromUser:" + fromName + ", toGroup:" + toName + ", Message:" + msg + "\n");
                            System.out.println("TimeStamp:" + t.toString() + " => Group:" + toName + ", Message:" + msg + "\n");
                        } else if (broadcastMsg) {
                            broadcastHashMap.put(t, id+" TimeStamp:" + t.toString() + " => fromUser:" + fromName + ", Broadcast to all users, Message:" + msg + "\n");
                            System.out.println("TimeStamp:" + t.toString() + " => Broadcast to all users, Message:" + msg + "\n");
                        } else {
                            userHashMap.put(t, id+" TimeStamp:" + t.toString() + " => fromUser:" + fromName + ", toUser:" + toName + ", Message:" + msg + "\n");
                            System.out.println("TimeStamp:" + t.toString() + " => toUser:" + toName + ", Message:" + msg + "\n");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        Iterator i;
        if (type.equals("fromUser")) {
            msgInformation = msgInformation + "\n------------------BROADCAST MESSAGES------------------" + "\n";
            i = broadcastHashMap.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry m = (Map.Entry) i.next();
                msgInformation = msgInformation + m.getValue();
            }
            msgInformation = msgInformation + "------------------GROUP MESSAGES------------------" + "\n";
            i = groupHashMap.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry m = (Map.Entry) i.next();
                msgInformation = msgInformation + m.getValue();
            }
        }
        // for all messages which are broadcast
        msgInformation = msgInformation + "------------------DIRECT MESSAGES------------------" + "\n";
        i = userHashMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry m = (Map.Entry) i.next();
            msgInformation = msgInformation + m.getValue();
        }
        return msgInformation;
    }

    /**
     * retrieves all the queued messages of a given user
     *
     * @param user     name of the user
     * @param lastSeen last seen of respective user
     * @return list of all the queued messsages received by the user
     */
    public List<String> getAllQueuedMessagesForUser(String user, Timestamp lastSeen) {
        List<String> msgInformation = new ArrayList<>();
        SortedMap<Timestamp, String> queuedMsgs = new TreeMap<Timestamp, String>();
        try {
            String sql = "SELECT fromUser, toUser, IsGroupMsg, message, creationTime, IsBroadcast, isRecall FROM message_details WHERE creationTime > '" + lastSeen + "'" + "AND IsBroadcast = 0";
            try (Statement pStatement = connection.createStatement()) {
                try (ResultSet rs = pStatement.executeQuery(sql)) {
                    while (rs.next()) {
                        String fromUser = getUsername(rs.getInt("fromUser"));
                        String to = rs.getString("toUser");
                        boolean groupMsg = rs.getBoolean("IsGroupMsg");
                        String msg = rs.getString("message");
                        Timestamp t = rs.getTimestamp("creationTime");
                        boolean broadcastMsg = rs.getBoolean("IsBroadcast");
                        boolean recallMsg = rs.getBoolean("isRecall");
                        // if not a recall message then store it
                        if (!recallMsg) {
                            if (groupMsg) {
                                //if user is memeber of that group then store msg
                                if (SQLDB.getInstance().isGroupMember(to, user)) {
                                    queuedMsgs.put(t, "fromUser:" + fromUser + ",Message:" + msg);
                                }
                            } else if (broadcastMsg) {
                                //if broadcast msg then store msg
                                queuedMsgs.put(t, "fromUser:" + fromUser + ",Message:" + msg);
                            } else if (to.equals(user)) {
                                //if direct message belongs to respective user then store msg
                                queuedMsgs.put(t, "fromUser:" + fromUser + ",Message:" + msg);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        Iterator i = queuedMsgs.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry m = (Map.Entry) i.next();
            msgInformation.add((String) m.getValue());
        }
        return msgInformation;
    }

    /**
     * gets the messages of a group in which the user is present
     *
     * @param userName name of the user
     * @param group    name of thr group
     * @return list of all messages in the group in which the user is present
     */
    public String getAllMessagesForGroup(String userName, String group) {
        if (!isGroupMember(group, userName)) {
            return "User not a member of group";
        }
        String msgInformation = "";

        SortedMap<Timestamp, String> hmap = new TreeMap<Timestamp, String>();
        try {
            // check whether user belongs to specific group or not
//            String sql = "SELECT fromUser, message, creationTime FROM message_details WHERE toUser='".concat(group).concat("' AND IsGroupMsg = ");
            String sql = "SELECT messageID, isRecall, fromUser, message, creationTime FROM message_details WHERE toUser=? AND IsGroupMsg =?";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setString(1, group);
                pStatement.setBoolean(2, true);
                try (ResultSet rs = pStatement.executeQuery()) {
                    while (rs.next()) {
                        String from = getUsername(rs.getInt("fromUser"));
                        String msg = rs.getString("message");
                        Timestamp t = rs.getTimestamp("creationTime");
                        boolean recallMsg = rs.getBoolean("isRecall");
                        int id = rs.getInt("messageID");
                        if (!recallMsg) {
                            hmap.put(t, id + "TimeStamp:" + t.toString() + " => From:" + from + ", Message:" + msg + "\n");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        Iterator i = hmap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry m = (Map.Entry) i.next();
            msgInformation = msgInformation + m.getValue();
        }
        return msgInformation;
    }

    /**
     * SPRINT 3(PREM)
     * get all messages send by specific user
     *
     * @param fromUser name of user
     * @return list of messages which are send by respective user
     */
    public List<String> getAllMessagesSendBySender(String fromUser) {
        List<String> getAllMessages = new ArrayList<>();
        try {
            String sql = "SELECT message FROM message_details WHERE fromUser=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setInt(1, getUserID(fromUser));
                try (ResultSet rs = pStatement.executeQuery()) {
                    while (rs.next()) {
                        String msg = rs.getString("message");
                        getAllMessages.add(msg);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return getAllMessages;
    }

    /**
     * SPRINT 3(PREM)
     * get all messageid of messages send by specific user
     *
     * @param fromUser name of user
     * @return list of message id which were send by respective user
     */
    public List<Integer> getAllMessageID(String fromUser) {
        List<Integer> getAllMessageID = new ArrayList<>();
        try {
            String sql = "SELECT messageID FROM message_details WHERE fromUser=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setInt(1, getUserID(fromUser));
                try (ResultSet rs = pStatement.executeQuery()) {
                    while (rs.next()) {
                        int msgID = rs.getInt("messageID");
                        getAllMessageID.add(msgID);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return getAllMessageID;
    }

    /**
     * SPRINT 3(PREM)
     * get all messages which have content as substring
     * @param content string to be searched for
     * @return list of messages which have content as a substring
     */
    public List<String> getAllMessageBasedOnContent(String content) {
        List<String> getAllMessages = new ArrayList<>();
        try {
            String sql = "SELECT message FROM message_details WHERE LOCATE(?,message)>0";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setString(1, content);
                try (ResultSet rs = pStatement.executeQuery()) {
                    while (rs.next()) {
                        String msg = rs.getString("message");
                        getAllMessages.add(msg);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return getAllMessages;
    }

    /**
     * SPRINT 3(PREM)
     * get all messages delivered to specific user
     *
     * @param toUser name of user or group
     * @return list of messages which are delivered to respective user
     */
    public List<String> getAllMessagesReceivedByReceiver(String toUser) {
        List<String> getAllMessages = new ArrayList<>();
        try {
            String sql = "SELECT message FROM message_details WHERE toUser=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setString(1, toUser);
                try (ResultSet rs = pStatement.executeQuery()) {
                    while (rs.next()) {
                        String msg = rs.getString("message");
                        getAllMessages.add(msg);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return getAllMessages;
    }

    /**
     * SPRINT 3(PREM)
     * get all messages delivered at specific date
     *
     * @param d date
     * @return list of messages which are delivered at specific date
     */
    public List<String> getAllMessagesDeliveredAtSpecificDate(Date d) {
        List<String> getAllMessages = new ArrayList<>();
        try {
            String sql = "SELECT message FROM message_details WHERE DATE(creationTime)=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setDate(1, d);
                try (ResultSet rs = pStatement.executeQuery()) {
                    while (rs.next()) {
                        String msg = rs.getString("message");
                        getAllMessages.add(msg);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return getAllMessages;
    }


    /**
     * @return list of all users in the DB
     */
    public List<String> retrieveAllUsers() {
        List<String> userInformation = new ArrayList<>();
        try {
            // '*' in case we require userId or more fields in future
            String sqlRetrieveAllUsers = "SELECT * FROM users";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveAllUsers)) {
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        userInformation.add(userSet.getString("username"));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return userInformation;

    }

    /**
     * to see all the members in a group
     *
     * @param groupName name of the group that is expected to be retrieved
     * @return list of users in the group
     */
    public List<String> retrieveGroupMembers(String groupName) {
        List<String> userInformation = new ArrayList<>();
        try {
            if (checkGroup(groupName)) {
                int groupId = getGroupID(groupName);
                String sqlRetrieveGroup = "SELECT userId FROM groupMembers WHERE groupId=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup)) {
                    pStatement.setInt(1, groupId);
                    try (ResultSet userSet = pStatement.executeQuery()) {
                        while (userSet.next()) {
                            int tempUserId = userSet.getInt("userId");
                            String tempUsername = getUsername(tempUserId);
                            userInformation.add(tempUsername);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return userInformation;
    }

    /**
     * @return all the groups in the DB
     */
    public List<String> retrieveAllGroups() {
        List<String> groupInformation = new ArrayList<>();

        try {
            // '*' in case we require more fields in future
            String sqlRetrieveAllUsers = "SELECT * FROM groups";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveAllUsers)) {
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        groupInformation.add(userSet.getString("groupName"));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return groupInformation;

    }

    /**
     * deletes a user from the group
     *
     * @param groupName name in which deletion occurs
     * @param username  name of the user who is supposed to be deleted
     * @return true if user exists and sql operation is successful
     */
    public boolean deleteGroupMember(String groupName, String username) {
        boolean flag = false;

        try {
            int userId = getUserID(username);
            int groupId = getGroupID(groupName);
            String sqlDeleteUser = "DELETE FROM groupMembers WHERE userId=? AND groupId=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlDeleteUser)) {
                pStatement.setInt(1, userId);
                pStatement.setInt(2, groupId);
                int userCount = pStatement.executeUpdate();
                flag = (userCount > 0);
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * check if a user is a member of a group
     *
     * @param groupName name of the group in which the check occurs
     * @param userName  name of the user who is supposed to be checked
     * @return true if the user exists in the group
     */
    public boolean isGroupMember(String groupName, String userName) {
        boolean flag = false;

        try {
            int userId = getUserID(userName);
            int groupId = getGroupID(groupName);
            String sqlCheckUser = "SELECT COUNT(*) FROM groupMembers WHERE userId=? AND groupId=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser)) {
                pStatement.setInt(1, userId);
                pStatement.setInt(2, groupId);
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        flag = (userSet.getInt(1) > 0);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;

    }

    /**
     * check if a user is an agency or not
     *
     * @param userName name of the user who is supposed to be checked
     * @return user's role type
     */
    public int getUserRole(String userName) {
        int roleId = -1;
        try {
            int userId = getUserID(userName);
            String sqlCheckUser = "SELECT roleId FROM users WHERE userId=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser)) {
                pStatement.setInt(1, userId);
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        roleId = userSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return roleId;
    }

    /**
     * check if a user is an agency or not
     *
     * @param userName name of the user who is supposed to be checked
     * @return true if the user is agency/has agency privilege
     */
    public boolean isUserOrGroupWiretapped(String userOrGroupName, int isGroup) {
        boolean flag = false;
        try {
            String sqlCheckUser = "SELECT COUNT(*) FROM wiretapUsers WHERE userWiretapped=?";
            int wiretapCandidateId = (isGroup == 1) ? getGroupID(userOrGroupName) : getUserID(userOrGroupName);
            if (isGroup == 1) {
                sqlCheckUser = "SELECT COUNT(*) FROM wiretapGroups WHERE userWiretapped=?";
            }

            try (PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser)) {
                pStatement.setInt(1, wiretapCandidateId);
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        flag = (userSet.getInt(1) > 0);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    public int requestWiretap(String requestingUser, String userOrGroupName, int isGroup, int requestDurationDays) {
        int insertedRowID = -1;

        try {
            if (getUserRole(requestingUser) != USER_ROLE_AGENCY_ID) return insertedRowID;
            if(checkWiretapRequest(requestingUser, userOrGroupName, isGroup)) return -1;
            int requestingUserId = getUserID(requestingUser);
            int wireTapCandidate = (isGroup == 1) ? getGroupID(userOrGroupName) : getUserID(userOrGroupName);
            if (wireTapCandidate == -1) return insertedRowID;
            String sqlStatement = "INSERT INTO wiretapRequests(userRequestingId, userVictimId, requestDurationDays, isGroup) VALUES(?,?,?,?)";

            try (PreparedStatement pStatement = connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {
                pStatement.setInt(1, requestingUserId);
                pStatement.setInt(2, wireTapCandidate);
                pStatement.setInt(3, requestDurationDays);
                pStatement.setInt(4, isGroup);
                int wiretapCount = pStatement.executeUpdate();
                ResultSet keySet = pStatement.getGeneratedKeys();
                if (keySet.next()) {
                    insertedRowID = keySet.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return insertedRowID;
    }

    public Map<Integer, String> getWiretapRequests(String requestingUser, String agencyUser, int isApproved) {
        Map<Integer, String> wiretapRequests = new HashMap<>();
        String wiretapRequestString = "";
        if(getUserRole(requestingUser) != USER_ROLE_ADMIN_ID) return wiretapRequests;
        try {
            int agencyUserId = getUserID(agencyUser);
            String sqlRetrieveAllUsers = "SELECT * FROM wiretapRequests WHERE userRequestingId LIKE ? AND isApproved=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveAllUsers)) {
                if(agencyUserId == -1) {
                    pStatement.setString(1, "%");
                }
                else {
                    pStatement.setInt(1, agencyUserId);
                }
                pStatement.setInt(2, isApproved);
                try (ResultSet requestSet = pStatement.executeQuery()) {
                    while (requestSet.next()) {
                        wiretapRequestString = "Agency " + getUsername(requestSet.getInt("userRequestingId")) + " has made a request to wiretap ";
                        if(requestSet.getInt("isGroup") == 1) {
                            wiretapRequestString += "group: ";
                            wiretapRequestString += getGroupName(requestSet.getInt("userVictimId"));
                        }
                        else {
                            wiretapRequestString += "user: ";
                            wiretapRequestString += getUsername(requestSet.getInt("userVictimId"));
                        }
                        wiretapRequestString += " for " + requestSet.getInt("requestDurationDays") + " days.";

                        wiretapRequests.put(requestSet.getInt("requestId"), wiretapRequestString);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return wiretapRequests;
    }


    public boolean setWireTap(String requestingUser, String agencyUsername) {
        boolean flag = false;
        try {
            if(getUserRole(requestingUser) != USER_ROLE_ADMIN_ID) return false;
            String sqlStatement = "SELECT * FROM wiretapRequests WHERE userRequestingId=? AND isApproved=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlStatement)) {
                pStatement.setInt(1, getUserID(agencyUsername));
                pStatement.setInt(2, 0);
                try (ResultSet requestSet = pStatement.executeQuery()) {
                    while (requestSet.next()) {
                        if(requestSet.getInt("isGroup") == 1) {
                            sqlStatement = "INSERT INTO wiretapGroups(userWiretapping, userWiretapped, expireAfterDays) VALUES(?,?,?)";
                        }
                        else {
                            sqlStatement = "INSERT INTO wiretapUsers(userWiretapping, userWiretapped, expireAfterDays) VALUES(?,?,?)";
                        }

                        try (PreparedStatement subPStatement = connection.prepareStatement(sqlStatement)) {
                            subPStatement.setInt(1, requestSet.getInt("userRequestingId"));
                            subPStatement.setInt(2, requestSet.getInt("userVictimId"));
                            subPStatement.setInt(3,requestSet.getInt("requestDurationDays"));
                            int wiretapCount = subPStatement.executeUpdate();
                            flag = (wiretapCount > 0);
                            if(flag) {
                                String sqlUpdateRequest = "UPDATE wiretapRequests SET isApproved=? WHERE userRequestingId=? AND isApproved=?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(sqlUpdateRequest)) {
                                    updateStatement.setInt(1, 1);
                                    updateStatement.setInt(2, getUserID(agencyUsername));
                                    updateStatement.setInt(3,0);
                                    updateStatement.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * set wiretap on a user or a group
     * @param requestingUser name of the user requesting wiretap
     * @param userOrGroupName name or group name on whom the wire tap is supposed to be set
     * @param isGroup 1 if wire tap request is for group, 0 otherwise
     * @param isSetWiretap 1 if user is requesting to set wire tap, 0 otherwise
     * @return true if the wire tap was successfully set
     */
    public boolean setWireTap(String requestingUser, int requestId) {
        boolean flag = false;
        try {
            if(getUserRole(requestingUser) != USER_ROLE_ADMIN_ID) return false;
            String sqlStatement = "SELECT * FROM wiretapRequests WHERE requestId=? AND isApproved=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlStatement)) {
                pStatement.setInt(1, requestId);
                pStatement.setInt(2, 0);
                try (ResultSet requestSet = pStatement.executeQuery()) {
                    while (requestSet.next()) {
                        if(requestSet.getInt("isGroup") == 1) {
                            sqlStatement = "INSERT INTO wiretapGroups(userWiretapping, userWiretapped, expireAfterDays) VALUES(?,?,?)";
                        }
                        else {
                            sqlStatement = "INSERT INTO wiretapUsers(userWiretapping, userWiretapped, expireAfterDays) VALUES(?,?,?)";
                        }

                        try (PreparedStatement subPStatement = connection.prepareStatement(sqlStatement)) {
                            subPStatement.setInt(1, requestSet.getInt("userRequestingId"));
                            subPStatement.setInt(2, requestSet.getInt("userVictimId"));
                            subPStatement.setInt(3,requestSet.getInt("requestDurationDays"));
                            int wiretapCount = subPStatement.executeUpdate();
                            flag = (wiretapCount > 0);
                            if(flag) {
                                String updateRequestQ = "UPDATE wiretapRequests SET isApproved=? WHERE requestId=? AND isApproved=?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateRequestQ)) {
                                    updateStatement.setInt(1, 1);
                                    updateStatement.setInt(2, requestId);
                                    updateStatement.setInt(3, 0);
                                    updateStatement.executeUpdate();
                                }
                            }
                        }

                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }





    /**
     * get wiretapped users
     *
     * @param requestingUser name of the user requesting wiretapped users/groups
     * @param isGroup        true if wire tap request is for group, false otherwise
     * @return a list of users/groups wiretapped by the requestingUser
     */
    public List<String> getWiretappedUsers(String agencyUsername, int isGroup) {
        List<String> tappedUsersOrGroups = new ArrayList<>();
        String sqlCheckUser = "";
        int requestingUserId = -1;
        try {
            requestingUserId = getUserID(agencyUsername);
            sqlCheckUser = "SELECT userWiretapped FROM wiretapUsers WHERE userWireTapping=?";
            if (isGroup == 1) {
                sqlCheckUser = "SELECT userWiretapped FROM wiretapGroups WHERE userWireTapping=?";
            }

            try (PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser)) {
                pStatement.setInt(1, requestingUserId);
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        tappedUsersOrGroups.add(this.getUsername(userSet.getInt("userWiretapped")));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return tappedUsersOrGroups;
    }


    /**
     * set wiretap on a user or a group
     * @param requestingUser name of the user requesting wiretapped users/groups
     * @param isGroup true if wire tap request is for group, false otherwise
     * @return a list of users/groups wiretapped by the requestingUser
     */
    public List<String> getAgencyList(String userOrGroupName, int isGroup, int isIncludeExpired) {
        List<String> agencyList = new ArrayList<>();
        String sqlCheckUser = "";

        try {
            int wireTapCandidate = -1;
            if (isGroup == 1) {
                wireTapCandidate = getGroupID(userOrGroupName);
                sqlCheckUser = "SELECT w.userWiretapping"
                        + " FROM wiretapGroups w WHERE userWireTapped=?";
            }
            else {
                wireTapCandidate = getUserID(userOrGroupName);
                sqlCheckUser = "SELECT w.userWiretapping"
                        + " FROM wiretapUsers w WHERE userWireTapped=?";
            }

            if(isIncludeExpired == 0) {
                sqlCheckUser += " AND CURDATE() < DATE_ADD(w.creationTime, INTERVAL w.expireAfterDays DAY)";
            }
            try (PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser)) {
                pStatement.setInt(1, wireTapCandidate);
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        agencyList.add(this.getUsername(userSet.getInt("userWiretapping")));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return agencyList;
    }

    public boolean updateUserRole(String username, int roleId) {
        boolean flag = false;
        try {
            if (checkUser(username)) {
                String sqlCreateUser = "UPDATE users SET roleId=? WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setInt(1, roleId);
                    pStatement.setString(2, username);
                    int userCount = pStatement.executeUpdate();
                    flag = (userCount > 0);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    public boolean deleteWiretapRequest(int requestID) {
        boolean flag = false;
        try {
            String sqlDeleteRequest = "DELETE FROM wiretapRequests WHERE requestId=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlDeleteRequest)) {
                pStatement.setInt(1, requestID);
                int userCount = pStatement.executeUpdate();
                flag = (userCount > 0);

            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * gets the group name
     *
     * @param groupId unique interger for retrieval
     * @return the name of the group according to the ID
     */
    public String getGroupName(int groupId) {
        String userInformation = "";
        try {
            String sqlRetrieveGroup = "SELECT groupName FROM groups WHERE groupId=?";
            try (PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup)) {
                pStatement.setInt(1, groupId);
                try (ResultSet userSet = pStatement.executeQuery()) {
                    while (userSet.next()) {
                        userInformation = userSet.getString("username");
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return userInformation;
    }

    public boolean checkWiretapRequest(String requestingUser, String userOrGroupName, int isGroup) {
        boolean flag = false;
        int wireTapCandidate = getUserID(userOrGroupName);
        String sqlCheckWiretapRequet = "";
        try {
            int requestingUserId = getUserID(requestingUser);
            if (isGroup == 1) {
                wireTapCandidate = getGroupID(userOrGroupName);
            }
            sqlCheckWiretapRequet = "SELECT COUNT(*)"
                    + " FROM wiretapRequests w WHERE userRequestingId=? AND userVictimId=? AND isGroup=?"
                    + " AND CURDATE() <= DATE_ADD(w.creationTime, INTERVAL w.requestDurationDays DAY)";

            try (PreparedStatement pStatement = connection.prepareStatement(sqlCheckWiretapRequet)) {
                pStatement.setInt(1, requestingUserId);
                pStatement.setInt(2, wireTapCandidate);
                pStatement.setInt(3, isGroup);
                try (ResultSet requestSet = pStatement.executeQuery()) {
                    while (requestSet.next()) {
                        flag = requestSet.getInt(1) > 0;
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }

        return flag;
    }


    /**
     * This method updates the IP address of a user
     * @param username
     * @return true if IP updation works successfully, false otherwise
     */
    public boolean setIP(String username, String ipAddress) {
        boolean flag = false;
        try {
            if (checkUser(username)) {
                String sqlCreateUser = "UPDATE users SET IP=? WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setString(1, ipAddress);
                    pStatement.setString(2, username);
                    int userCount = pStatement.executeUpdate();
                    flag = (userCount > 0);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }


    /**
     * Method to set parent control on a user
     * @param username user on whom the parent control is turned on
     * @param i value 0 or 1 to turn on and off respectively
     * @return true if the SQL operation is successful
     */
    public boolean setControl(String username, int i) {
        boolean flag = false;
        try {
            if (checkUser(username)) {
                String sqlCreateUser = "UPDATE users SET control=? WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setInt(1, i);
                    pStatement.setString(2, username);
                    int userCount = pStatement.executeUpdate();
                    flag = (userCount > 0);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return flag;
    }

    /**
     * Method to get the parent control from the DB
     * @param username person whose data is being retrieved
     * @return the value of the column for the user requested
     */
    public int getControl(String username) {
        int userInformation = 0;
        try {
            if (checkUser(username)) {
                String sqlCreateUser = "SELECT * FROM users WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setString(1, username);
                    try (ResultSet userSet = pStatement.executeQuery()) {
                        while (userSet.next()) {
                            userInformation = userSet.getInt("control");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Caught SQL Exception:" + e.toString());
        }
        return userInformation;
    }


}