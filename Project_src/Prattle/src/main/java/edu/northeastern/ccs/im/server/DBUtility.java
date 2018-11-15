package edu.northeastern.ccs.im.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.sql.*;

public class DBUtility {
    final static String CONNECTION_URL = "aaw5ywu4d6dc4k.c7ohnssvtfpy.us-east-1.rds.amazonaws.com";
    final static String DB_PORT = "3306";
    final static String DB_NAME = "team105";
    final static String DB_USERNAME = "team105";
    final static String DB_PASSWORD = "Team-105";

    public static Connection connection;
    public static DBUtility instance;

    public DBUtility() {
        connection = null;
        try {
            connection = DriverManager.
                    getConnection("jdbc:mysql://" + CONNECTION_URL + ":" + DB_PORT + "/" + DB_NAME, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection Failed!:\n" + e.getMessage());
        }
    }

    public static DBUtility getInstance() {
        if (instance == null) {
            instance = new DBUtility();
        }
        return instance;
    }

    public boolean checkUser(String username) {
        try {
            String sqlCheckUser = "SELECT COUNT(*) FROM users WHERE username=?";
            PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser);
            pStatement.setString(1, username);
            ResultSet userSet = pStatement.executeQuery();
            while (userSet.next()) {
                return userSet.getInt(1) > 0;
            }

        } catch (SQLException e) {

        }
        return false;
    }

    public String retrieve(String username)  {
        try {
            String userInformation = null;
            if (checkUser(username)) {
                String sqlCreateUser = "SELECT * FROM users WHERE username=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser);
                pStatement.setString(1, username);
                ResultSet userSet = pStatement.executeQuery();
                while (userSet.next()) {
                    userInformation = userSet.getString("password");
                }
            }
            return userInformation;
        } catch (SQLException e ){

        }
        return null;
    }

    public boolean create(int userId, String username, String password) {
        try {
            if (!checkUser(username)) {
                String sqlCreateUser = "INSERT INTO users (userId, username, password) VALUES (?, ?, ?)";
                PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser);
                pStatement.setInt(1, userId);
                pStatement.setString(2, username);
                pStatement.setString(3, encryptPassword(password));
                int userCount = pStatement.executeUpdate();
                return (userCount > 0);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }

    public boolean update(String username, String password){
        try {
            if (checkUser(username)) {
                String sqlCreateUser = "UPDATE users SET password=? WHERE username=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser);
                pStatement.setString(1, encryptPassword(password));
                pStatement.setString(2, username);
                int userCount = pStatement.executeUpdate();
                return (userCount > 0);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }

    public boolean delete(String username){
        try {
            String sqlDeleteUser = "DELETE FROM users WHERE username=?";
            PreparedStatement pStatement = connection.prepareStatement(sqlDeleteUser);
            pStatement.setString(1, username);
            int userCount = pStatement.executeUpdate();
            return (userCount > 0);
        } catch (SQLException e ){

        }
        return false;
    }

    public boolean validateCredentials(String username, String password) {
        try {
            String encryptedPassword = encryptPassword(password);
            String dbPassword = "";
            if (checkUser(username)) {
                String sqlCheckUser = "SELECT password FROM users WHERE username=?";
                PreparedStatement pStatement = connection.prepareStatement(sqlCheckUser);
                pStatement.setString(1, username);
                ResultSet userSet = pStatement.executeQuery();
                while (userSet.next()) {
                    dbPassword = userSet.getString("password");
                }
            }
            return (encryptedPassword.equals(dbPassword));
        } catch (Exception e){

        }
        return false;
    }

    public String encryptPassword(String password) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[] saltValue = generateSalt();
        MessageDigest encryptAlgorithm = MessageDigest.getInstance("MD5");
        //encryptAlgorithm.update(saltValue); - not using salt value in order to compare passwords

        byte[] bytes = encryptAlgorithm.digest(password.getBytes());
        StringBuilder hexConversion = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            hexConversion.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return hexConversion.toString();
    }

    public byte[] generateSalt() throws NoSuchAlgorithmException, NoSuchProviderException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

        byte[] saltValue = new byte[16];
        random.nextBytes(saltValue);
        return saltValue;
    }
}