package edu.northeastern.ccs.im.server;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("all")
public class UserDB {
	
	SQLDB sqlDB;
    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(UserDB.class.getName());
    Connection connection;
    public UserDB(Connection con) {
    	connection = con;
    	sqlDB = SQLDB.getInstance();
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
    public String retrieve(String username, String type) {
        String userInformation = null;
        try {
            if (checkUser(username)) {
                String sqlCreateUser = "SELECT * FROM users WHERE username=?";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setString(1, username);
                    try (ResultSet userSet = pStatement.executeQuery()) {
                        while (userSet.next()) {
                            userInformation = userSet.getString(type);
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
     * creates a     user if they don't exist in the database
     *
     * @param userId   integer which acts a primary key in the Database
     * @param username name the user wants to have
     * @param password string entered by the user for their password
     * @return true if the details entered are in a legal format and when they stored in the Database
     */
    public boolean create(int userId, String username, String password, String IP, int control) {
        boolean flag = false;
        try {
            if (!checkUser(username)) {
                String sqlCreateUser = "INSERT INTO users (userId, username, paswd, IP, control) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pStatement = connection.prepareStatement(sqlCreateUser)) {
                    pStatement.setInt(1, userId);
                    pStatement.setString(2, username);
                    pStatement.setString(3, encryptPassword(password));
                    pStatement.setString(4, IP);
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
        String sqlCheckUser = "SELECT w.userWiretapping"
                + " FROM wiretapUsers w WHERE userWireTapped=?";

        try {
            int wireTapCandidate = -1;
            if (isGroup == 1) {
                wireTapCandidate = sqlDB.getGroupID(userOrGroupName);
                sqlCheckUser = "SELECT w.userWiretapping"
                        + " FROM wiretapGroups w WHERE userWireTapped=?";
            }
            else {
                wireTapCandidate = getUserID(userOrGroupName);
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
