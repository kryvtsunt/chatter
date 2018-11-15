package edu.northeastern.ccs.im.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public boolean checkUser(String username) throws SQLException {
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
        String userInformation = "";
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
}