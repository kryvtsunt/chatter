package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLDBTest2 {
    SQLDB sqldb = SQLDB.getInstance();

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
        assertFalse(sqldb.update("mockUser111","can't"));
        assertTrue(sqldb.update("mockUser1","newPass2"));
    }

    @Test
    void delete() {
        assertTrue(sqldb.create(107,"deletetest","pass"));
        assertTrue(sqldb.delete("deletetest"));
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
        sqldb.createGroup("mockGroup");
        assertFalse(sqldb.updateGroup("newMockgroup","newMockGroup2"));
        sqldb.deleteGroup("mockGroup");
    }

    @Test
    void retrieveGroup() {
        assertFalse(sqldb.retrieveGroup("mockGroup").size()>0);
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
        System.out.println(sqldb.getAllMessagesForGroup("retrieveGroup"));
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
}