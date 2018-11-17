package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.server.ClientRunnable;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
@RunWith(PowerMockRunner.class)
@PrepareForTest(Message.class)
class MessageTest1 {
    @Before
    public void init() throws Exception {
        Message messageSpy;
        MockitoAnnotations.initMocks(this);
//        clientRunnableSpy = PowerMockito.spy();
//        PowerMockito.doNothing().when(ClientRunnable.class,"setName");
    }

    @Test
    void makeQuitMessage() {
        Message msgQuit = Message.makeQuitMessage("mockUser");
        assertEquals("BYE",msgQuit.getType().toString());
    }

    @Test
    void makeBroadcastMessage() {
        Message makeBCT = Message.makeBroadcastMessage("mockUser","User input");
        assertEquals("BCT",makeBCT.getType().toString());
    }

    @Test
    void makeDirectMessage() {
        Message makeDM = Message.makeDirectMessage("mockUser","mockUser2","received?");
        assertEquals("DIR",makeDM.getType().toString());
    }

    @Test
    void makeGroupMessage() {

        Message makeGM = Message.makeGroupMessage("mockUser","mockUserGroup","group received?");
        assertEquals("GRP",makeGM.getType().toString());

    }

    @Test
    void makeRetrieveMessage() {
        Message makeRM = Message.makeRetrieveMessage("mockUser","text");
        assertEquals("RET",makeRM.getType().toString());

    }

    @Test
    void makeDeleteMessage() {
        Message makeDelM = Message.makeDeleteMessage("mockUser","deleted?");
        assertEquals("DEL", makeDelM.getType().toString());
    }

    @Test
    void makeJoinMessage() {
        Message makeJM = Message.makeJoinMessage("mockUser","joined?");
        assertTrue(makeJM.isJoinMessage());
        assertEquals("JIN",makeJM.getType().toString());
    }

    @Test
    void makeLeaveMessage() {
        Message msgLV = Message.makeLeaveMessage("mockUser","left?");
        assertEquals("LVE",msgLV.getType().toString());
    }

    @Test
    void makeUpdateMessage() {
        Message msgUp = Message.makeUpdateMessage("mockUser","updte");
        assertEquals("UPD",msgUp.getType().toString());
    }

    @Test
    void makeHelloMessage() {
        Message msgHello = Message.makeHelloMessage("mockUser");
        assertEquals("mockUser",msgHello.getText());
    }

    @Test
    void makeMessage() {
        Message m1 = Message.makeMessage("HLO","mockUser","mockUser1","hello");
        Message m2 = Message.makeMessage("ACK","mockUser","mockUser1","hello");
        Message m3 = Message.makeMessage("NAK","mockUser","mockUser1","hello");
        Message m4 = Message.makeMessage("BYE","mockUser","mockUser1","hello");
        Message m5 = Message.makeMessage("BCT","mockUser","mockUser1","hello");
        Message m6 = Message.makeMessage("DIR","mockUser","mockUser1","hello");
        Message m7 = Message.makeMessage("GRP","mockUser","mockUser1","hello");
        Message m8 = Message.makeMessage("RET","mockUser","mockUser1","hello");
        Message m9 = Message.makeMessage("UPD","mockUser","mockUser1","hello");
        Message m10 = Message.makeMessage("DEL","mockUser","mockUser1","hello");
        Message m11 = Message.makeMessage("JIN","mockUser","mockUser1","hello");
        Message m12 = Message.makeMessage("LVE","mockUser","mockUser1","hello");
        assertTrue(m2.isAcknowledge());
        assertTrue(m5.isDisplayMessage());
        assertTrue(m5.isBroadcastMessage());
        assertTrue(m6.isDirectMessage());
        assertTrue(m7.isGroupMessage());
        assertTrue(m8.isRetrieveMessage());
        assertTrue(m10.isDeleteMessage());
        assertTrue(m12.isLeavenMessage());
        assertTrue(m11.isJoinMessage());

    }

    @Test
    void makeNoAcknowledgeMessage() {
        Message msg = Message.makeNoAcknowledgeMessage();
        assertEquals("NAK",msg.getType().toString());

    }

    @Test
    void makeAcknowledgeMessage() {
        Message msg = Message.makeAcknowledgeMessage("mockUser");
        assertEquals("ACK",msg.getType().toString());

    }

    @Test
    void makeLoginMessage() {
        assertEquals("HLO 8 mockUser 2 -- 2 --",Message.makeLoginMessage("mockUser").toString());
    }

    @Test
    void getType() {
        assertEquals("ACK",Message.makeAcknowledgeMessage("mockUser").getType().toString());
    }

    @Test
    void getSender() {
        assertNull(Message.makeDirectMessage(null,"user","text").getSender());
//        assertNull(Message.makeDirectMessage(null,"user","text").getReceiver());
        assertNull(Message.makeDirectMessage("user",null,"text").getReceiver());
        assertNull(Message.makeDirectMessage(null,"user",null).getText());
        assertNotNull(Message.makeDirectMessage("user","user1","text").getReceiver());
        assertNotNull(Message.makeDirectMessage("user","user1","text").getText());
        assertEquals("mockUser",Message.makeDirectMessage("mockUser","user2","test").getSender());

        System.out.println(Message.makeDirectMessage("user","user2",null).getReceiver());
        System.out.println(Message.makeMessage("DIR","user","user2",null).getReceiver());
    }

    @Test
    void getReceiver() {
        System.out.println(Message.makeDirectMessage(null,null,null));
        assertEquals("mockUser",Message.makeDirectMessage("mockUser","user2","test").getSender());

    }

    @Test
    void getText() {
        assertEquals("mockUser",Message.makeDirectMessage("mockUser","user2","test").getSender());
//        System.out.println(Message.makeDirectMessage("user","user1","text").getText());
//        System.out.println(Message.makeDirectMessage(null,"user1","text").getText());
//        System.out.println(Message.makeDirectMessage("user",null,"text").getText());
//        System.out.println(Message.makeDirectMessage(null,"user1",null).getText());
//        System.out.println(Message.makeDirectMessage("user",null,null).getText());
//        System.out.println(Message.makeDirectMessage(null,null,null).getText());
//        System.out.println(Message.makeDirectMessage(null,null,"text").getText());

    }

    @Test
    void isAcknowledge() {
    }

    @Test
    void isBroadcastMessage() {
    }

    @Test
    void isDirectMessage() {
    }

    @Test
    void isUpdateMessage() {
        assertEquals("UPD",Message.makeUpdateMessage("mockUser","update").getType().toString());
        assertTrue(Message.makeUpdateMessage("mockUser","update").isUpdateMessage());
    }

    @Test
    void isGroupMessage() {
    }

    @Test
    void isRetrieveMessage() {
    }

    @Test
    void isJoinMessage() {
    }

    @Test
    void isLeavenMessage() {
    }

    @Test
    void isDeleteMessage() {
    }

    @Test
    void isDisplayMessage() {
    }

    @Test
    void isInitialization() {
        assertTrue(Message.makeHelloMessage("text").isInitialization());
    }

    @Test
    void terminate() {
        assertTrue(Message.makeQuitMessage("bye").terminate());
    }

}