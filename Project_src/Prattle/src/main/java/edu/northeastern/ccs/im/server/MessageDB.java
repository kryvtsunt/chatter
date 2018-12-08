package edu.northeastern.ccs.im.server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

@SuppressWarnings("all")
public class MessageDB {
    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(UserDB.class.getName());
    Connection connection;
    SQLDB sqlDB;
    
    public MessageDB(Connection con) {
    	connection = con;
    	sqlDB = SQLDB.getInstance();
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
        int userID = sqlDB.getUserID(from);
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
        int userID = sqlDB.getUserID(from);
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
        int userID = sqlDB.getUserID(from);
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
            String sql = "SELECT messageID FROM message_details WHERE fromUser = '" + sqlDB.getUserID(user) + "' ORDER BY messageID DESC LIMIT 1";
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
                pStatement.setInt(3, sqlDB.getUserID(userName));
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
            username = Integer.toString(sqlDB.getUserID(user));
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
                        String fromName = this.sqlDB.getUsername(Integer.parseInt(from));
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
                        String fromUser = sqlDB.getUsername(rs.getInt("fromUser"));
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
        if (!sqlDB.isGroupMember(group, userName)) {
            return "User not a member of group";
        }
        String msgInformation = "";

        SortedMap<Timestamp, String> hmap = new TreeMap<Timestamp, String>();
        try {
            // check whether user belongs to specific group or not
            String sql = "SELECT messageID, isRecall, fromUser, message, creationTime FROM message_details WHERE toUser=? AND IsGroupMsg =?";
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setString(1, group);
                pStatement.setBoolean(2, true);
                try (ResultSet rs = pStatement.executeQuery()) {
                    while (rs.next()) {
                        String from = sqlDB.getUsername(rs.getInt("fromUser"));
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
                pStatement.setInt(1, sqlDB.getUserID(fromUser));
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
                pStatement.setInt(1, sqlDB.getUserID(fromUser));
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
}
