package edu.northeastern.ccs.im.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

public class SQLDB {
    final static String CONNECTION_URL = "aaw5ywu4d6dc4k.c7ohnssvtfpy.us-east-1.rds.amazonaws.com";
    final static String DB_PORT = "3306";
    final static String DB_NAME = "team105";
    final static String DB_USERNAME = "team105";
    final static String DB_PASSWORD = "Team-105";

    public static Connection connection = null;
    public static SQLDB instance = null;

    public SQLDB() {
        connection = null;
        try {
            connection = DriverManager.
                    getConnection("jdbc:mysql://" + CONNECTION_URL + ":" + DB_PORT + "/" + DB_NAME, DB_USERNAME, DB_PASSWORD);
            instance = this;
        } catch (SQLException e) {
            System.out.println("Connection Failed!:\n" + e.getMessage());
        }
    }

    public static SQLDB getInstance() {
        if(instance == null) {
            new SQLDB();
        }
        return instance;
    }

    public boolean checkUser(String username) {
        try {
            String sqlCheckUser = "SELECT COUNT(*) FROM users WHERE username=?";
            PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser);
            pStatement.setString(1,username);
            ResultSet userSet = pStatement.executeQuery();
            while(userSet.next()) {
                return userSet.getInt(1) > 0;
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public String retrieve(String username) {
        String userInformation = null;
        try {
            if(checkUser(username)) {
                String sqlCreateUser = "SELECT * FROM users WHERE username=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser);
                pStatement.setString(1, username);
                ResultSet userSet = pStatement.executeQuery();
                while(userSet.next()) {
                    userInformation = userSet.getString("password");
                }
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }

        return userInformation;
    }

    public boolean create(int userId, String username, String password){
        try {
            if(!checkUser(username)) {
                String sqlCreateUser = "INSERT INTO users (userId, username, password) VALUES (?, ?, ?)";
                PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser);
                pStatement.setInt(1, userId);
                pStatement.setString(2, username);
                pStatement.setString(3, encryptPassword(password));
                int userCount = pStatement.executeUpdate();
                return (userCount > 0);
            }
        }
        catch(SQLException sqlE) {
            System.out.println(sqlE);
        }
        return false;
    }

    public boolean update(String username, String password) {
        try {
            if(checkUser(username)) {
                String sqlCreateUser = "UPDATE users SET password=? WHERE username=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser);
                pStatement.setString(1, encryptPassword(password));
                pStatement.setString(2, username);
                int userCount = pStatement.executeUpdate();
                return (userCount > 0);
            }
        }
        catch(SQLException sqlE) {
            System.out.println(sqlE.toString());
        }

        return false;
    }

    public boolean delete(String username) {
        try {
            String sqlDeleteUser = "DELETE FROM users WHERE username=?";
            PreparedStatement pStatement = connection.prepareStatement(sqlDeleteUser);
            pStatement.setString(1, username);
            int userCount = pStatement.executeUpdate();
            return (userCount > 0);
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public boolean validateCredentials(String username, String password) {
        try {
            String encryptedPassword = encryptPassword(password);
            String dbPassword = "";
            if(checkUser(username)) {
                String sqlCheckUser = "SELECT password FROM users WHERE username=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser);
                pStatement.setString(1, username);
                ResultSet userSet = pStatement.executeQuery();
                while (userSet.next()) {
                    dbPassword = userSet.getString("password");
                }
            }
            return (encryptedPassword.equals(dbPassword));
        }

        catch(SQLException sqlE) {
            System.out.println(sqlE);
        }

        return false;
    }

    public String encryptPassword(String password) {
        StringBuilder hexConversion = new StringBuilder();
        try {
            MessageDigest encryptAlgorithm = MessageDigest.getInstance("MD5");

            byte[] bytes = encryptAlgorithm.digest(password.getBytes());

            for(int i=0; i< bytes.length ;i++)
            {
                hexConversion.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        }
        catch(NoSuchAlgorithmException e) {
            System.out.println(e.toString());
        }

        return hexConversion.toString();
    }

    public boolean checkGroup(String groupName) {
        try {
            String sqlCheckGroup = "SELECT COUNT(*) FROM groups WHERE groupName=?";
            PreparedStatement pStatement = connection.prepareStatement(sqlCheckGroup);
            pStatement.setString(1,groupName);
            ResultSet groupsSet = pStatement.executeQuery();
            while(groupsSet.next()) {
                return groupsSet.getInt(1) > 0;
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public boolean createGroup(String groupName){
        try {
            if(!checkGroup(groupName)) {
                String sqlCreateUser = "INSERT INTO groups (groupName) VALUES (?)";
                PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser);
                pStatement.setString(1, groupName);
                int groupCount = pStatement.executeUpdate();
                return (groupCount > 0);
            }
        }
        catch(SQLException sqlE) {
            System.out.println(sqlE);
        }

        return false;
    }

    public boolean deleteGroup(String groupName) {
        try {
            String sqlDeleteUser = "DELETE FROM groups WHERE groupName=?";
            PreparedStatement pStatement = connection.prepareStatement(sqlDeleteUser);
            pStatement.setString(1, groupName);
            int groupCount = pStatement.executeUpdate();
            return (groupCount > 0);
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }
        return false;

    }

    public boolean updateGroup(String groupName, String newGroupName){
        try {
            if(checkGroup(groupName)) {
                String sqlCreateUser = "UPDATE groups SET groupName=? WHERE groupName=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser);
                pStatement.setString(1, newGroupName);
                pStatement.setString(2, groupName);
                int groupCount = pStatement.executeUpdate();
                return (groupCount > 0);
            }
        }
        catch(SQLException sqlE) {
            System.out.println(sqlE);
        }

        return false;
    }

    public List<String> retrieveGroup(String groupName){
        List<String> userInformation = new ArrayList<>();
        try {
            if(checkGroup(groupName)) {
                int groupId = getGroupID(groupName);
                String sqlRetrieveGroup = "SELECT userId FROM groupMembers WHERE groupId=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup);
                pStatement.setInt(1, groupId);
                ResultSet userSet = pStatement.executeQuery();
                while(userSet.next()) {
                    String tempUserId = userSet.getString("userId");
                    String tempUsername = getUsername(tempUserId);
                    userInformation.add(tempUsername);
                }
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }
        return userInformation;

    }

    public int getUserID(String username){
        int userInformation = -1;
        try {
            if(checkUser(username)) {
                String sqlRetrieveGroup = "SELECT userId FROM users WHERE username=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup);
                pStatement.setString(1, username);
                ResultSet userSet = pStatement.executeQuery();
                while(userSet.next()) {
                    userInformation = userSet.getInt("userId");
                }
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }

        return userInformation;
    }

    public String getUsername(String userId){
        String userInformation = "";
        try {
            String sqlRetrieveGroup = "SELECT username FROM users WHERE userId=?";
            PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup);
            pStatement.setString(1, userId);
            ResultSet userSet = pStatement.executeQuery();
            while(userSet.next()) {
                userInformation = userSet.getString("username");
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }

        return userInformation;
    }

    public int getGroupID(String groupName){
        int groupInformation = -1;
        try {
            if(checkGroup(groupName)) {
                String sqlRetrieveGroup = "SELECT groupId FROM groups WHERE groupName=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup);
                pStatement.setString(1, groupName);
                ResultSet groupSet = pStatement.executeQuery();
                while(groupSet.next()) {
                    groupInformation = groupSet.getInt("groupId");
                }
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }

        return groupInformation;
    }

    public boolean addGroupMember(String groupName, String username){
        try {
            if(checkGroup(groupName)) {
                int userId = getUserID(username);
                int groupId = getGroupID(groupName);
                String sqlAddUser = "INSERT INTO groupMembers (groupId, userId) VALUES (?,?)";
                PreparedStatement pStatement = connection.prepareStatement(sqlAddUser);
                pStatement.setInt(1, groupId);
                pStatement.setInt(2, userId);
                int groupCount = pStatement.executeUpdate();
                return (groupCount > 0);
            }
        }
        catch(SQLException sqlE) {
            System.out.println(sqlE.toString());
        }

        return false;
    }

    public boolean storeMessageIndividual(String from, String to, String text) {
        int userID = getUserID(from);
        try {
            String sql = "INSERT INTO message_details (fromUser, toUser, IsMedia, IsGroupMsg, message, IsBroadcast) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pStatement = connection.prepareStatement(sql);
            pStatement.setInt(1, userID);
            pStatement.setString(2, to);
            pStatement.setBoolean(3, false);
            pStatement.setBoolean(4, false);
            pStatement.setString(5, text);
            pStatement.setBoolean(6, false);
            int msgCount = pStatement.executeUpdate();
            return (msgCount > 0);
        }catch(Exception e) {System.out.println(e.toString());}
        return false;
    }

    public boolean storeMessageGroup(String from, String group, String text) {
        int userID = getUserID(from);
        try {
            String sql = "INSERT INTO message_details (fromUser, toUser, IsMedia, IsGroupMsg, message, IsBroadcast) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pStatement = connection.prepareStatement(sql);
            pStatement.setInt(1, userID);
            pStatement.setString(2, group);
            pStatement.setBoolean(3, false);
            pStatement.setBoolean(4, true);
            pStatement.setString(5, text);
            pStatement.setBoolean(6, false);
            int msgCount = pStatement.executeUpdate();
            return (msgCount > 0);
        }catch(Exception e) {}
        return false;
    }

    public boolean storeMessageBroadcast(String from, String text) {
        int userID = getUserID(from);
        try {
            String sql = "INSERT INTO message_details (fromUser, toUser, IsMedia, IsGroupMsg, message, IsBroadcast) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pStatement = connection.prepareStatement(sql);
            pStatement.setInt(1, userID);
            pStatement.setString(2, "BROADCAST");
            pStatement.setBoolean(3, false);
            pStatement.setBoolean(4, false);
            pStatement.setString(5, text);
            pStatement.setBoolean(6, true);
            int msgCount = pStatement.executeUpdate();
            return (msgCount > 0);
        }catch(Exception e) {}
        return false;
    }

    public String getAllMessagesForUser(String user) {
        int userID = getUserID(user);
        String msgInformation = "";
        SortedMap<Timestamp,String> userHashMap = new TreeMap<Timestamp,String>();
        SortedMap<Timestamp,String> groupHashMap = new TreeMap<Timestamp,String>();
        SortedMap<Timestamp,String> broadcastHashMap = new TreeMap<Timestamp,String>();
        try {
            String sql = "SELECT toUser, IsGroupMsg, message, creationTime, IsBroadcast FROM message_details WHERE fromUser = '" + userID + "'";
            Statement pStatement = connection.createStatement();
            ResultSet rs = pStatement.executeQuery(sql);
            while(rs.next()) {
                String to = rs.getString("toUser");
                boolean groupMsg = rs.getBoolean("IsGroupMsg");
                String msg = rs.getString("message");
                Timestamp t = rs.getTimestamp("creationTime");
                boolean broadcastMsg = rs.getBoolean("IsBroadcast");
                if(groupMsg) {
                    groupHashMap.put(t,"TimeStamp:" + t.toString() + " => Group:" + to + ", Message:" + msg + "\n");
                    System.out.println("TimeStamp:" + t.toString() + " => Group:" + to + ", Message:" + msg + "\n");
                } else if(broadcastMsg) {
                    broadcastHashMap.put(t,"TimeStamp:" + t.toString() + " => Broadcast to all users, Message:" + msg + "\n");
                    System.out.println("TimeStamp:" + t.toString() + " => Broadcast to all users, Message:" + msg + "\n");
                } else {
                    userHashMap.put(t,"TimeStamp:" + t.toString() + " => User:" + to + ", Message:" + msg + "\n");
                    System.out.println("TimeStamp:" + t.toString() + " => User:" + to + ", Message:" + msg + "\n");
                }
            }
        } catch(Exception e) {System.out.println(e.toString());}

        // for all messages which are broadcast
        msgInformation = msgInformation + "------------------BROADCAST MESSAGES------------------" + "\n";
        Iterator i = broadcastHashMap.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry m = (Map.Entry)i.next();
            msgInformation = msgInformation + m.getValue();
        }

        // for all messages which are broadcast
        msgInformation = msgInformation + "------------------GROUP MESSAGES------------------" + "\n";
        i = groupHashMap.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry m = (Map.Entry)i.next();
            msgInformation = msgInformation + m.getValue();
        }

        // for all messages which are broadcast
        msgInformation = msgInformation + "------------------USER MESSAGES------------------" + "\n";
        i = userHashMap.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry m = (Map.Entry)i.next();
            msgInformation = msgInformation + m.getValue();
        }
        return msgInformation;
    }

    public String getAllMessagesForGroup(String group) {
        //NOTE: write code to check if user belongs to that group
        String msgInformation = "";
        SortedMap<Timestamp,String> hmap = new TreeMap<Timestamp,String>(Collections.reverseOrder());
        try {
            // check whether user belongs to specific group or not
            String sql = "SELECT fromUser, message, creationTime FROM message_details WHERE toUser='" + group + "' AND IsGroupMsg = " + true;
            Statement pStatement = connection.createStatement();
            ResultSet rs = pStatement.executeQuery(sql);
            while(rs.next()) {
                String from = getUsername(String.valueOf(rs.getInt("fromUser")));
                String msg = rs.getString("message");
                Timestamp t = rs.getTimestamp("creationTime");
                hmap.put(t,"TimeStamp:" + t.toString() + " => From:" + from + ", Message:" + msg + "\n");
            }
        } catch(Exception e) {}

        Iterator i = hmap.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry m = (Map.Entry)i.next();
            msgInformation = msgInformation + m.getValue();
        }
        return msgInformation;
    }

    public List<String> retrieveAllUsers(){
        List<String> userInformation = new ArrayList<>();
        try {
            // '*' in case we require userId or more fields in future
            String sqlRetrieveAllUsers = "SELECT * FROM users";
            PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveAllUsers);
            ResultSet userSet = pStatement.executeQuery();
            while(userSet.next()) {
                userInformation.add(userSet.getString("username"));
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }
        return userInformation;

    }

    public List<String> retrieveGroupMembers(String groupName){
        List<String> userInformation = new ArrayList<>();
        try {
            if(checkGroup(groupName)) {
                int groupId = getGroupID(groupName);
                String sqlRetrieveGroup = "SELECT userId FROM groupMembers WHERE groupId=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveGroup);
                pStatement.setInt(1, groupId);
                ResultSet userSet = pStatement.executeQuery();
                while(userSet.next()) {
                    int tempUserId = userSet.getInt("userId");
                    String tempUsername = getUsername(Integer.toString(tempUserId));
                    userInformation.add(tempUsername);
                }
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }
        return userInformation;

    }

    public List<String> retrieveAllGroups(){
        List<String> groupInformation = new ArrayList<>();
        try {
            // '*' in case we require more fields in future
            String sqlRetrieveAllUsers = "SELECT * FROM groups";
            PreparedStatement pStatement = connection.prepareStatement(sqlRetrieveAllUsers);
            ResultSet userSet = pStatement.executeQuery();
            while(userSet.next()) {
                groupInformation.add(userSet.getString("groupName"));
            }
        }
        catch(SQLException e) {
            System.out.println(e.toString());
        }
        return groupInformation;

    }
}