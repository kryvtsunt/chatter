package edu.northeastern.ccs.im.server;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SQLDBTest {
    private SQLDB sqldb;

    @BeforeEach
    void setup(){
        sqldb = SQLDB.getInstance();
    }

    @Test
    void getInstance() {
        assertTrue(sqldb.retrieveAllGroups().size()>0);
    }

    @Test
    void checkUser() {
        assertFalse(sqldb.checkUser("samsung?"));
        assertTrue(sqldb.checkUser("Mikey"));
    }

    @Test
    void retrieve() {
        assertEquals("0bf44a9634896de88678e56222c1b012", sqldb.retrieve("Mikey"));
        assertNotEquals("Mikey",sqldb.retrieve("Mikey"));
    }


    @Test
    void create() {
        assertFalse(sqldb.create(105,"mockuser2","newpp"));
        sqldb.delete("mockUser1");
        assertTrue(sqldb.create(106,"mockUser1","newPass"));

    }

    @Test
    void update() {
        assertFalse(sqldb.update(null,null));
        assertFalse(sqldb.update("mockUser111","can't"));
        assertTrue(sqldb.update("mockUser1","newPass2"));
    }

    @Test
    void excpetion(){
        SQLDB db = SQLDB.getInstance();
        try {
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db.retrieve("nooo");
        db.update("mooksa","asdxas");
        db.checkUser("user");
        db.delete("tim");
        db.deleteGroupMember("asdf","asdf");
        db.createGroup("Asdf");
        db.checkGroup("asdf");
        db.getUsername("asdf");
        db.getGroupID("asdf");
        db.retrieveAllUsers();
        db.retrieve("tim");
        db.getGroupID("random");
        db.getUsername("none");
        db.checkGroup("none");
        db.createGroup("none");
        db.create(11,"none","none");
        db.deleteGroupMember("none","none");
        db.delete("none");
        db.deleteGroup("none");
        db.retrieve("nonoe");
        db.update("none","pass");
        db.validateCredentials("none","none");
        db.encryptPassword("none");
        db.updateGroup("none","nonw");
        db.addGroupMember("none","nonoe");
        db.reset();
        db.retrieveAllGroups();
        db.storeMessageIndividual("nonw","nonow","aonso");
        db.retrieveGroup("none");
        db.getUserID("none");
        db.storeMessageGroup("nonw","nonw","non");
        db.storeMessageGroup("none","mo","sa");
        db.getAllMessagesForUser("no");
        db.getAllMessagesForGroup("no","ni");
        db.retrieveGroupMembers("noin");

    }

    @Test
    void delete() {
//        assertFalse(sqldb.create(107,"deletetest","pass"));
        sqldb.delete("deletetest");
    }

    @Test
    void validateCredentials() {
        assertFalse(sqldb.validateCredentials("Mikey","Mikey"));
    }

    @Test
    void encryptPassword() {
//        assertTrue(sqldb.e);
        assertNotNull(sqldb.encryptPassword("testPass"));
    }

    @Test
    void checkGroup() {
        sqldb.createGroup("mockGroup");
        assertTrue(sqldb.checkGroup("mockGroup"));
        sqldb.deleteGroup("mockGroup");
    }

    @Test
    void updateGroup() {
        sqldb.createGroup("mockGroup3");
        assertTrue(sqldb.updateGroup("mockGroup3","newMockGroup4"));
        sqldb.deleteGroup("newMockGroup4");
    }

    @Test
    void retrieveGroup(){
        assertEquals("mockUser",sqldb.retrieveGroup("retrieveGroup").get(0));
    }

    @Test
    void getUserID() {
        assertEquals(105,sqldb.getUserID("mockUser"));
    }

    @Test
    void getUsername() {
        assertEquals("mockUser",sqldb.getUsername("105"));
    }

    @Test
    void getGroupID() {
        assertEquals(-1,(sqldb.getGroupID("nothing")));
    }

    @Test
    void addGroupMember() {
        sqldb.createGroup("mockGroup");
        assertTrue(sqldb.addGroupMember("mockGroup","mockUser"));
        sqldb.deleteGroup("mockGroup");
    }

    @Test
    void storeMessageIndividual() {
        assertTrue(sqldb.storeMessageIndividual("mockUser","Mikey","Hey"));

    }

    @Test
    void storeMessageGroup() {
        sqldb.createGroup("mockGroup");

        assertTrue(sqldb.storeMessageGroup("Mikey","mockGroup","grouptext"));
        sqldb.deleteGroup("mockGroup");
    }

    @Test
    void storeMessageBroadcast() {
        assertTrue(sqldb.storeMessageBroadcast("Mikey","test"));
    }

    @Test
    void getAllMessagesForUser() {
        assertNotNull(sqldb.getAllMessagesForUser("mockUser"));
    }

    @Test
    void getAllMessagesForGroup() {
//        sqldb.create(111,"105","pass");
//        sqldb.addGroupMember("retrieveGroup","mockUser");

        assertNotNull(sqldb.getAllMessagesForGroup("mockUser","retrieveGroup"));
    }

    @Test
    void retrieveAllUsers() {
        assertTrue(sqldb.retrieveAllUsers().size()>0);
    }

    @Test
    void retrieveGroupMembers() {
        assertTrue(sqldb.retrieveGroupMembers("retrieveGroup").size()>0);
    }

    @Test
    void retrieveAllGroups() {
        assertTrue(sqldb.retrieveAllGroups().size()>0);
    }

    @Test
    void deleteGroupMember(){
        sqldb.addGroupMember("newMockGroup2","mockUser");
        assertTrue(sqldb.deleteGroupMember("newMockGroup2","mockUser"));

    }
}