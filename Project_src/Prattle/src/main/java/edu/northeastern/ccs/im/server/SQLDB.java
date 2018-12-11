package edu.northeastern.ccs.im.server;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Logger;


public class SQLDB {
    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(SQLDB.class.getName());

    /**
     * Database connection
     */
    private static Connection connection;

    /**
     * Instance of the Database
     */
    private static SQLDB instance;

    static UserDB userDBObject;
    static GroupDB groupDBObject;
    static MessageDB messageDBObject;

    final String CONNECTION_URL = "aaw5ywu4d6dc4k.c7ohnssvtfpy.us-east-1.rds.amazonaws.com";
    final String DB_PORT = "3306";
    final String DB_NAME = "team105";
    final String DB_USERNAME = "team105";
    final String DB_PASWD = "Team-105";
    final int USER_ROLE_NORMAL_ID = 1;
    final int USER_ROLE_AGENCY_ID = 2;
    final int USER_ROLE_ADMIN_ID = 0;

    final String EXCEPTIONMSG = "Caught SQL Exception:";

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
            userDBObject = new UserDB(connection);
            groupDBObject = new GroupDB(connection);
            messageDBObject = new MessageDB(connection);
        }
        return instance;
    }

    /**
     * resets the Database connection
     */
    public static void reset() {
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
    public boolean checkUser(String username)  {
        return userDBObject.checkUser(username);
    }

    /**
     * Retreive the user's data from the Database
     *
     * @param username keyword against which the sql operations are carried on
     * @return the user's details stored in the Database
     */
    public String retrieve(String username, String type) {
        return userDBObject.retrieve(username, type);
    }

    /**
     * SPRINT 3(PREM)
     * Retreive the user's lastseen from the Database
     *
     * @param username keyword against which the sql operations are carried on
     * @return the user's lastseen stored in the Database
     */
    public Timestamp retrieveLastSeen(String username) {
        return userDBObject.retrieveLastSeen(username);
    }

    /**
     * creates a     user if they don't exist in the database
     *
     * @param userId   integer which acts a primary key in the Database
     * @param username name the user wants to have
     * @param password string entered by the user for their password
     * @return true if the details entered are in a legal format and when they stored in the Database
     */
    public boolean create(int userId, String username, String password, String ipAddress, int control) {
        return userDBObject.create(userId, username, password, ipAddress, control);
    }

    /**
     * if the user wants to update their informatin escpecially their password
     *
     * @param username user who is trying to update their password
     * @param password new password the user wants to update to
     * @return true if the sql operation of updating is successful
     */
    public boolean update(String username, String password) {
        return userDBObject.update(username, password);
    }

    /**
     * SPRINT 3(PREM)
     * if the user wants to update their username
     *
     * @param oldUsername user who is trying to update their last seen
     * @return true if the sql operation of updating is successful
     */
    public boolean updateLastSeen(String oldUsername) {
        return userDBObject.updateLastSeen(oldUsername);
    }


    /**
     * deletes a user from the Database
     *
     * @param username name that is supposed to be deleted
     * @return true if the sql operation of deletion is successful
     */
    public boolean delete(String username) {
        return userDBObject.delete(username);
    }

    /**
     * The first step of establishing a connection
     *
     * @param username users input of their username
     * @param password users input of their password
     * @return true if the credentials match the Database and the sql operation of matching succeeds
     */
    public boolean validateCredentials(String username, String password) {
        return userDBObject.validateCredentials(username, password);
    }

    /**
     * users password when created, is stored in MD5 format
     * (hehe, we are not stupid to store as a plain string)
     *
     * @param password string that needs encryption
     * @return the encrypted password
     */
    public String encryptPassword(String password) {
        return userDBObject.encryptPassword(password);
    }

    /**
     * A group must be in the database for a user to join, this method validates it
     *
     * @param groupName the group name user wants to join or to retrive messages from
     * @return true if there exists a group in the Database and the SQL operation is successful
     */
    public boolean checkGroup(String groupName) {
        return groupDBObject.checkGroup(groupName);
    }

    /**
     * If a group doesn't exists it can be create or if a user wants, they can crete one
     *
     * @param groupName name of the group
     * @return true if the sql operation of creating a group with a valid name is successful
     */
    public boolean createGroup(String groupName) {
        return groupDBObject.createGroup(groupName);
    }

    /**
     * you don't want a group? no problem just delete it
     *
     * @param groupName name which needs to be deleted
     * @return true if the groupname exixts and the sql operation of deleting is successful
     */
    public boolean deleteGroup(String groupName) {
        return groupDBObject.deleteGroup(groupName);

    }

    /**
     * well, you might want to change the groupname at some point
     *
     * @param groupName    the existing group
     * @param newGroupName new name for the group
     * @return true if group name exists, new name is valid and the sql operation is successful
     */
    public boolean updateGroup(String groupName, String newGroupName) {
        return groupDBObject.updateGroup(groupName, newGroupName);
    }


    /**
     * gets the userID for a given user
     *
     * @param username name used to retrive the ID from Database
     * @return the member ID (which is an integer)
     */
    public int getUserID(String username) {
        return userDBObject.getUserID(username);
    }


    /**
     * gets the user name
     *
     * @param userId unique interger for retrieval
     * @return the name of the user according to the ID
     */
    public String getUsername(int userId) {
        return userDBObject.getUsername(userId);
    }

    /**
     * Each group is given a unique ID which can be retrieved
     *
     * @param groupName name against which the sql operation is performed
     * @return the unique number of the group
     */
    public int getGroupID(String groupName) {
        return groupDBObject.getGroupID(groupName);
    }

    /**
     * when the situation of adding a member arises
     *
     * @param groupName group to which an user is added
     * @param username  the user who is expected to be added
     * @return true of the group and user exists and sql operation is successful
     */
    public boolean addGroupMember(String groupName, String username) {
        return groupDBObject.addGroupMember(groupName, username);
    }

    /**
     * stores the messages for each user
     * @param from user who sent the message
     * @param to user who received the message
     * @param text message sene from one user to other
     * @param senderIP ip address of sender
     * @param receiverIP ip address of receiver
     * @return true from user exists and sql operation is successful
     */
    public boolean storeMessageIndividual(String from, String to, String text, String senderIP, String receiverIP) {
        return messageDBObject.storeMessageIndividual(from, to, text, senderIP, receiverIP);
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
        return messageDBObject.storeMessageGroup(from, group, text, senderIP, receiverIP);
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
        return messageDBObject.storeMessageBroadcast(from, text, senderIP, receiverIP);
    }

    /**
     * SPRINT 3(PREM)
     * retrieve message id of last message send by user
     *
     * @param user name of user
     * @return message id of last message sent by user
     */
    public int getLastMessageID(String user) {
        return messageDBObject.getLastMessageID(user);
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
        return messageDBObject.setRecallFlagMessage(userName, messageID);
    }

    /**
     * retrieves all the messages of a given user
     *
     * @param user name of the user
     * @return list of all the broadcast, group, individual messages sent/received by the user
     */
    public String getAllMessagesForUser(String user, String type) {
        return messageDBObject.getAllMessagesForUser(user, type);
    }

    /**
     * retrieves all the queued messages of a given user
     *
     * @param user     name of the user
     * @param lastSeen last seen of respective user
     * @return list of all the queued messsages received by the user
     */
    public List<String> getAllQueuedMessagesForUser(String user, Timestamp lastSeen) {
        return messageDBObject.getAllQueuedMessagesForUser(user, lastSeen);
    }

    /**
     * gets the messages of a group in which the user is present
     *
     * @param userName name of the user
     * @param group    name of thr group
     * @return list of all messages in the group in which the user is present
     */
    public String getAllMessagesForGroup(String userName, String group) {
        return messageDBObject.getAllMessagesForGroup(userName, group);
    }

    /**
     * SPRINT 3(PREM)
     * get all messages send by specific user
     *
     * @param fromUser name of user
     * @return list of messages which are send by respective user
     */
    public List<String> getAllMessagesSendBySender(String fromUser) {
        return messageDBObject.getAllMessagesSendBySender(fromUser);
    }

    /**
     * SPRINT 3(PREM)
     * get all messageid of messages send by specific user
     *
     * @param fromUser name of user
     * @return list of message id which were send by respective user
     */
    public List<Integer> getAllMessageID(String fromUser) {
        return messageDBObject.getAllMessageID(fromUser);
    }

    /**
     * SPRINT 3(PREM)
     * get all messages which have content as substring
     * @param content string to be searched for
     * @return list of messages which have content as a substring
     */
    public List<String> getAllMessageBasedOnContent(String content) {
        return messageDBObject.getAllMessageBasedOnContent(content);
    }

    /**
     * SPRINT 3(PREM)
     * get all messages delivered to specific user
     *
     * @param toUser name of user or group
     * @return list of messages which are delivered to respective user
     */
    public List<String> getAllMessagesReceivedByReceiver(String toUser) {
        return messageDBObject.getAllMessagesReceivedByReceiver(toUser);
    }

    /**
     * SPRINT 3(PREM)
     * get all messages delivered at specific date
     *
     * @param d date
     * @return list of messages which are delivered at specific date
     */
    public List<String> getAllMessagesDeliveredAtSpecificDate(Date d) {
        return messageDBObject.getAllMessagesDeliveredAtSpecificDate(d);
    }


    /**
     * @return list of all users in the DB
     */
    public List<String> retrieveAllUsers() {
        return userDBObject.retrieveAllUsers();

    }

    /**
     * to see all the members in a group
     *
     * @param groupName name of the group that is expected to be retrieved
     * @return list of users in the group
     */
    public List<String> retrieveGroupMembers(String groupName) {
        return groupDBObject.retrieveGroupMembers(groupName);
    }

    /**
     * @return all the groups in the DB
     */
    public List<String> retrieveAllGroups() {
        return groupDBObject.retrieveAllGroups();

    }

    /**
     * deletes a user from the group
     *
     * @param groupName name in which deletion occurs
     * @param username  name of the user who is supposed to be deleted
     * @return true if user exists and sql operation is successful
     */
    public boolean deleteGroupMember(String groupName, String username) {
        return groupDBObject.deleteGroupMember(groupName, username);
    }

    /**
     * check if a user is a member of a group
     *
     * @param groupName name of the group in which the check occurs
     * @param userName  name of the user who is supposed to be checked
     * @return true if the user exists in the group
     */
    public boolean isGroupMember(String groupName, String userName) {
        return groupDBObject.isGroupMember(groupName, userName);

    }

    /**
     * check if a user is an agency or not
     *
     * @param userName name of the user who is supposed to be checked
     * @return user's role type
     */
    public int getUserRole(String userName) {
        return userDBObject.getUserRole(userName);
    }

    /**
     * check if a user is an agency or not
     *
     * @param userOrGroupName name of the user who is supposed to be checked
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
            LOGGER.info(EXCEPTIONMSG + e.toString());
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
                pStatement.executeUpdate();
                try(ResultSet keySet = pStatement.getGeneratedKeys()) {
                    if (keySet.next()) {
                        insertedRowID = keySet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info(EXCEPTIONMSG + e.toString());
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
                    String userVictimId = "userVictimId";
                    while (requestSet.next()) {
                        wiretapRequestString = "Agency " + getUsername(requestSet.getInt("userRequestingId")) + " has made a request to wiretap ";
                        if(requestSet.getInt("isGroup") == 1) {
                            wiretapRequestString += "group: ";
                            wiretapRequestString += getGroupName(requestSet.getInt(userVictimId));
                        }
                        else {
                            wiretapRequestString += "user: ";
                            wiretapRequestString += getUsername(requestSet.getInt(userVictimId));
                        }
                        wiretapRequestString += " for " + requestSet.getInt("requestDurationDays") + " days.";

                        wiretapRequests.put(requestSet.getInt("requestId"), wiretapRequestString);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info(EXCEPTIONMSG + e.toString());
        }
        return wiretapRequests;
    }


    /**
     * set wiretap on a user or a group
     * @param requestingUser should be admin user
     * @param agencyName name of the user requesting wiretap
     * @param id id of user user requesting wiretap
     * @return true if the wire tap was successfully set
     */
    public boolean setWireTap(String requestingUser, String agencyName, int id) {
        int requestId;
        String sqlStatement = "SELECT * FROM wiretapRequests WHERE userRequestingId=? AND isApproved=?";
        String sqlUpdateRequest = "UPDATE wiretapRequests SET isApproved=? WHERE userRequestingId=? AND isApproved=?";
        if (agencyName == null){
            requestId = id;
            sqlStatement = "SELECT * FROM wiretapRequests WHERE requestId=? AND isApproved=?";
            sqlUpdateRequest = "UPDATE wiretapRequests SET isApproved=? WHERE requestId=? AND isApproved=?";
        } else {
            requestId = getUserID(agencyName);
        }
        boolean flag = false;
        try {
            if(getUserRole(requestingUser) != USER_ROLE_ADMIN_ID) return false;
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
                                try (PreparedStatement updateStatement = connection.prepareStatement(sqlUpdateRequest)) {
                                    updateStatement.setInt(1, 1);
                                    updateStatement.setInt(2, requestId);
                                    updateStatement.setInt(3,0);
                                    updateStatement.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info(EXCEPTIONMSG + e.toString());
        }
        return flag;
    }




    /**
     * get wiretapped users
     *
     * @param agencyUsername name of the user requesting wiretapped users/groups
     * @param isGroup        true if wire tap request is for group, false otherwise
     * @return a list of users/groups wiretapped by the requestingUser
     */
    public List<String> getWiretappedUsers(String agencyUsername, int isGroup) {
        return userDBObject.getWiretappedUsers(agencyUsername, isGroup);
    }


    /**
     * set wiretap on a user or a group
     * @param userOrGroupName name of the user requesting wiretapped users/groups
     * @param isGroup true if wire tap request is for group, false otherwise
     * @return a list of users/groups wiretapped by the requestingUser
     */
    public List<String> getAgencyList(String userOrGroupName, int isGroup, int isIncludeExpired) {
        return userDBObject.getAgencyList(userOrGroupName, isGroup, isIncludeExpired);
    }

    public boolean updateUserRole(String username, int roleId) {
        return userDBObject.updateUserRole(username, roleId);
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
            LOGGER.info(EXCEPTIONMSG + e.toString());
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
        return groupDBObject.getGroupName(groupId);
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
            LOGGER.info(EXCEPTIONMSG + e.toString());
        }

        return flag;
    }


    /**
     * This method updates the IP address of a user
     * @param username
     * @return true if IP updation works successfully, false otherwise
     */
    public boolean setIP(String username, String ipAddress) {
        return userDBObject.setIP(username, ipAddress);
    }


    /**
     * Method to set parent control on a user
     * @param username user on whom the parent control is turned on
     * @param i value 0 or 1 to turn on and off respectively
     * @return true if the SQL operation is successful
     */
    public boolean setControl(String username, int i) {
        return userDBObject.setControl(username, i);
    }

    /**
     * Method to get the parent control from the DB
     * @param username person whose data is being retrieved
     * @return the value of the column for the user requested
     */
    public int getControl(String username) {
        return userDBObject.getControl(username);
    }


}