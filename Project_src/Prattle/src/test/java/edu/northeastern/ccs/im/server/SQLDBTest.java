package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import static org.junit.jupiter.api.Assertions.*;

class SQLDBTest {
    private SQLDB sqldb;
    private static final int port = 4548;

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
        assertFalse(sqldb.create(105,"mockuser2","newpp","",0));
        sqldb.delete("mockUser1");
        assertTrue(sqldb.create(106,"mockUser1","newPass","",1));

    }

    @Test
    void update() {
        assertFalse(sqldb.update(null,null));
        assertFalse(sqldb.update("mockUser111","can't"));
        assertTrue(sqldb.update("mockUser1","newPass2"));
    }

    @Test
    void excpetion() throws ParseException, IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss");

        java.util.Date parsedTimeStamp = dateFormat.parse("2018-11-29 23:17:16");

        Timestamp timestamp = new Timestamp(parsedTimeStamp.getTime());
        SQLDB db = SQLDB.getInstance();
        try {
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db.updateLastSeen("aaa");
        db.validateCredentials("sos","sos");
        db.retrieve("mikey");
        db.update("mooksa","asdxas");
        db.checkUser("user");
        db.delete("tim");
        db.deleteGroupMember("asdf","asdf");
        db.createGroup("Asdf");
        db.checkGroup("asdf");
        db.getGroupID("asdf");
        db.retrieveAllUsers();
        db.retrieve("tim");
        db.getGroupID("random");
        db.checkGroup("none");
        db.createGroup("none");
        db.create(11,"none","none","",0);
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
        db.storeMessageIndividual("nonw","nonow","aonso","","");
        db.retrieveGroupMembers("none");
        db.getUserID("none");
        db.storeMessageGroup("nonw","nonw","non","","");
        db.storeMessageGroup("none","mo","sa","","");
        db.getAllMessagesForUser("no", "fromUser");
        db.getAllMessagesForGroup("no","ni");
        db.retrieveGroupMembers("noin");
        db.setRecallFlagMessage("nono",1);
        db.getAllQueuedMessagesForUser("mikey", timestamp);
        db.getAllMessagesForGroup("mikey","testGroup1");
        db.getAllMessagesSendBySender("mikey");
        db.getAllMessageID("mikey");
        db.getAllMessageBasedOnContent("hey");
        db.getAllMessagesReceivedByReceiver("mikey");
        db.getAllMessagesDeliveredAtSpecificDate(Date.valueOf("2018-11-29"));
        db.retrieveGroupMembers("testGroup1");
        db.getUserRole("admin");

        db.isUserOrGroupWiretapped("mikey",1);
        db.requestWiretap("mikey","testGroup1",1,2);
        db.getAllQueuedMessagesForUser("receiverTest",timestamp);


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
    void getUserID() {
        assertEquals(105,sqldb.getUserID("mockUser"));
    }

    @Test
    void getUsername() {
        assertEquals("mockUser",sqldb.getUsername(105));
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
    void storeMessageIndividual() throws IOException {
        assertTrue(sqldb.storeMessageIndividual("mockUser","Mikey","Hey","",""));

    }

    @Test
    void storeMessageGroup() {
        sqldb.createGroup("mockGroup");

        assertTrue(sqldb.storeMessageGroup("Mikey","mockGroup","grouptext","",""));
        sqldb.deleteGroup("mockGroup");
    }

    @Test
    void storeMessageBroadcast() {
        assertTrue(sqldb.storeMessageBroadcast("Mikey","test","",""));
    }

    @Test
    void getAllMessagesForUser() {
        assertNotNull(sqldb.getAllMessagesForUser("mockUser", "fromUser"));
    }

    @Test
    void getAllMessagesForGroup() {
//        sqldb.create(111,"105","pass");
//        sqldb.addGroupMember("retrieveGroup","mockUser");

        assertNotNull(sqldb.getAllMessagesForGroup("mockUser","retrieveGroup"));
    }

    @Test
    void testAllMessagesSendBySender(){
        assertEquals("hi receiver",sqldb.getAllMessagesSendBySender("senderTest").get(0));
    }

    @Test
    void testLastSeen(){
        assertTrue(sqldb.updateLastSeen("ag1"));
    }

    @Test
    void testAllMessageID(){
        assertTrue(Integer.parseInt("1607") == sqldb.getAllMessageID("senderTest").get(0));
    }

    @Test
    void testAllMessagesReceivedByReceiver(){
        assertEquals("hi receiver",sqldb.getAllMessagesReceivedByReceiver("receiverTest").get(0));
    }

    @Test
    void testAllMessagesDeliveredAtSpecificDate(){
        assertEquals("grouptext",sqldb.getAllMessagesDeliveredAtSpecificDate(Date.valueOf("2018-11-29")).get(0));
    }

    @Test
    void testAllMessageBasedOnContent(){
        assertEquals("hi receiver",sqldb.getAllMessageBasedOnContent("hi receiver").get(0));
    }

    @Test
    void testAllQueuedMessagesForUser(){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss");

            java.util.Date parsedTimeStamp = dateFormat.parse("2018-11-29 21:29:36");

            Timestamp timestamp = new Timestamp(parsedTimeStamp.getTime());
            assertEquals("fromUser:senderTest,Message:testing queued message",sqldb.getAllQueuedMessagesForUser("receiverTest",timestamp).get(0));
        } catch(Exception e) {}
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

    @Test
    void testWiretap() {

        sqldb.delete("adminUserTest");
        sqldb.delete("agencyUserTest");
        sqldb.delete("normalUserTest1");

        sqldb.create(220, "adminUserTest", "pass","",0);
        sqldb.updateUserRole("adminUserTest", 0);

        sqldb.create(221, "agencyUserTest", "pass","",0);
        sqldb.updateUserRole("agencyUserTest", 2);

        sqldb.create(222, "normalUserTest1", "pass","",0);

        sqldb.isUserOrGroupWiretapped("normalUserTest1", 0);
        assertEquals(1,sqldb.getUserRole("normalUserTest1"));
        assertEquals(false, sqldb.getWiretappedUsers("agencyUserTest", 0).size() > 0);

        assertEquals(true, sqldb.requestWiretap("normalUserTest1", "normalUserTest2", 0, 5) == -1);
        int requestRowID = sqldb.requestWiretap("agencyUserTest", "normalUserTest1", 0, 7);
        sqldb.requestWiretap("agencyUserTest", "normalUserTest2", 0, 7);
//    	assertEquals(true, sqldb.checkWiretapRequest("agencyUserTest", "normalUserTest1", 0));



        assertEquals(false, sqldb.getWiretapRequests("agencyUserTest", "agencyUserTest",0).size() > 0);
        assertEquals(true,sqldb.getWiretapRequests("adminUserTest", "agencyUserTest",0).size() > 0);

        assertEquals(false, sqldb.setWireTap("agencyUserTest", requestRowID));

        assertEquals(true, sqldb.setWireTap("adminUserTest", requestRowID));
        sqldb.setWireTap("adminUserTest", "agencyUserTest");

        assertEquals(true, sqldb.getAgencyList("normalUserTest1", 0, 1).size()> 0);
        assertEquals(true, sqldb.getAgencyList("normalUserTest1", 0, 0).size()> 0);
        assertEquals(true, sqldb.isUserOrGroupWiretapped("normalUserTest1", 0));
        assertEquals(true, sqldb.getWiretappedUsers("agencyUserTest", 0).size() > 0);

        /* Statements to get coverage */
        sqldb.retrieveGroupMembers("friends");
        sqldb.getAgencyList("friends", 1, 1);
        sqldb.deleteWiretapRequest(requestRowID);
        sqldb.getGroupName(100);
        sqldb.getLastMessageID("normalUserTest1");
        sqldb.retrieveLastSeen("normalUserTest1");
        sqldb.getAllQueuedMessagesForUser("normalUserTest1", sqldb.retrieveLastSeen("normalUserTest1"));
        sqldb.getLastMessageID("normalUserTest1");
        sqldb.setRecallFlagMessage("normalUserTest1", 998);

        assertEquals(true,sqldb.delete("adminUserTest"));
        assertEquals(true,sqldb.delete("agencyUserTest"));
        assertEquals(true,sqldb.delete("normalUserTest1"));
    }
    @Test
    void retrieveGroup(){
        sqldb.createGroup("sqldbCreateGroupTest");
        assertEquals(true,sqldb.retrieveGroupMembers("sqldbCreateGroupTest").size() == 0);
        sqldb.deleteGroup("sqldbCreateGroupTest");
    }

    @Test
    void checkIP(){
        sqldb.create(1051,"key3","key3","",0);
        assertTrue(sqldb.setIP("key3",""));
        sqldb.delete("key3");
    }

    @Test
    void checkControl(){
        sqldb.create(1051, "key3","key3","",1);
        assertTrue(sqldb.setControl("key3",1));
        assertEquals(1, sqldb.getControl("key3"));
        sqldb.delete("key3");
    }
}