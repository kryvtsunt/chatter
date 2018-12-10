package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class MessageTest {

    @Test
    void testBroadcast() {
        Message message = Message.makeBroadcastMessage("tim", "hello");
        assertEquals("BCT 3 tim 2 -- 5 hello", message.toString());
        assertEquals("tim", message.getSender());
        assertEquals("hello", message.getText());
        assertNull(message.getReceiver());
        assertTrue(message.isBroadcastMessage());
        assertTrue(message.isDisplayMessage());
        assertFalse(message.isAcknowledge());
        assertFalse(message.isDirectMessage());
        assertFalse(message.isInitialization());
    }

    @Test
    void testAcknowledge() {
        Message message = Message.makeAcknowledgeMessage("tim");
        assertTrue(message.isAcknowledge());
    }

    @Test
    void testNoAcknowledge() {
        Message message = Message.makeNoAcknowledgeMessage();
        assertFalse(message.isAcknowledge());
    }


    @Test
    void testHello() {
        Message message = Message.makeHelloMessage("Hello");
        assertFalse(message.isBroadcastMessage());
    }

    @Test
    void testLogger() {
        Message message = Message.makeLoggerMessage("abc");
        assertTrue(message.isLoggerMessage());
        assertEquals("LOG 3 abc 2 -- 2 --", message.toString());
    }

    @Test
    void testControl() {
        Message message = Message.makePControlMessage("abc", "tim");
        assertTrue(message.isPControlMessage());
        assertEquals("PCL 3 abc 3 tim 2 --", message.toString());
    }

    @Test
    void testRecall() {
        Message message = Message.makeRecallMessage("abc", "hi");
        assertTrue(message.isRecallMessage());
        assertEquals("RCL 3 abc 2 -- 2 hi", message.toString());
    }

    @Test
    void testRole() {
        Message message = Message.makeSetRoleMessage("abc", "def", "hi");
        assertTrue(message.isSetRoleMessage());
        assertEquals("RLE 3 abc 3 def 2 hi", message.toString());
    }

    @Test
    void testWiretapRequest() {
        Message message1 = Message.makeWiretapUserMessage("abc", "def", "hi");
        Message message2 = Message.makeWiretapGroupMessage("abc", "def", "hi");
        assertTrue(message1.isWiretapUserMessage());
        assertTrue(message2.isWiretapGroupMessage());
        assertEquals("WTU 3 abc 3 def 2 hi", message1.toString());
        assertEquals("WTG 3 abc 3 def 2 hi", message2.toString());
    }

    @Test
    void testWiretapRespond() {
        Message message1 = Message.makeWiretapApproveMessage("abc", "def", "hi");
        Message message2 = Message.makeWiretapRejectMessage("abp", "def", "hi");
        assertTrue(message1.isApproveMessage());
        assertTrue(message2.isRejectMessage());
        assertEquals("APR 3 abc 3 def 2 hi", message1.toString());
        assertEquals("RJT 3 abp 3 def 2 hi", message2.toString());
    }

    @Test
    void testSign() {
        Message message1 = Message.makeSigninMessage("abc", "def");
        Message message2 = Message.makeSignupMessage("abp", "def");
        assertTrue(message1.isSigninMessage());
        assertTrue(message2.isSignupMessage());
        assertEquals("SIN 3 abc 2 -- 3 def", message1.toString());
        assertEquals("SUP 3 abp 2 -- 3 def", message2.toString());
    }

    @Test
    void testHelp() {
        Message message = Message.makeHelpMessage("abc");
        assertTrue(message.isHelpMessage());
        assertEquals("HLP 3 abc 2 -- 2 --", message.toString());
    }

    @Test
    void testGroup() {
        Message message1 = Message.makeJoinMessage("tim", "group");
        assertTrue(message1.isJoinMessage());
        assertFalse(message1.isLeaveMessage());
        Message message2 = Message.makeLeaveMessage("tim", "group");
        assertFalse(message2.isJoinMessage());
        assertTrue(message2.isLeaveMessage());
    }


    @Test
    void testManual() {
        Message m;
        m = Message.makeMessage("ACK", "tim", null, null);
        assertTrue(m.isAcknowledge());
        m = Message.makeMessage("NAK", null, null, null);
        assertFalse(m.isAcknowledge());
        m= Message.makeMessage("LOG", "abc", "def", "hi");
        assertTrue(m.isLoggerMessage());
        m = Message.makeMessage("PCL", "abc", "def", "hi");
        assertTrue(m.isPControlMessage());
        m = Message.makeMessage("RCL", "abc", "def", "hi");
        assertTrue(m.isRecallMessage());
        m = Message.makeMessage("WTU", "abc", "def", "hi");
        assertTrue(m.isWiretapUserMessage());
        assertFalse(m.isWiretapGroupMessage());
        m = Message.makeMessage("APR", "abc", "def", "hi");
        assertTrue(m.isApproveMessage());
        m = Message.makeMessage("RJT", "abc", "def", "hi");
        assertTrue(m.isRejectMessage());
        m = Message.makeMessage("RLE", "abc", "def", "hi");
        assertTrue(m.isSetRoleMessage());
    }




}