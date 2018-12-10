package edu.northeastern.ccs.im.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GroupDB {

    SQLDB sqlDB;

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(GroupDB.class.getName());
    Connection connection;

    public GroupDB(Connection con) {
        connection = con;
        sqlDB = SQLDB.getInstance();
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
                int userId = sqlDB.getUserID(username);
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
                            String tempUsername = sqlDB.getUsername(tempUserId);
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
            int userId = sqlDB.getUserID(username);
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
            int userId = sqlDB.getUserID(userName);
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

}