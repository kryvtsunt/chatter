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
        assertFalse(message.isBroadcastMessage());
        assertFalse(message.isDisplayMessage());
        assertFalse(message.isInitialization());
    }


    @Test
    void testHello() {
        Message message = Message.makeHelloMessage("Hello");
        assertFalse(message.isBroadcastMessage());
    }

    @Test
    void test() {
        assertEquals("LOG 3 abc 2 -- 2 --", Message.makeLoggerMessage("abc").toString());
        assertEquals("PCL 3 abc 3 tim 2 --", Message.makePControlMessage("abc", "tim").toString());
        assertEquals("RCL 3 abc 2 -- 2 hi", Message.makeRecallMessage("abc", "hi").toString());
        assertEquals("RLE 3 abc 3 def 2 hi", Message.makeSetRoleMessage("abc", "def", "hi").toString());
        assertEquals("WTU 3 abc 3 def 2 hi", Message.makeWiretapUserMessage("abc", "def", "hi").toString());
        assertEquals("WTG 3 abc 3 def 2 hi", Message.makeWiretapGroupMessage("abc", "def", "hi").toString());

        assertEquals("APR 3 abc 3 def 2 hi", Message.makeWiretapApproveMessage("abc", "def", "hi").toString());
        assertEquals("RJT 3 abp 3 def 2 hi", Message.makeWiretapRejectMessage("abp", "def", "hi").toString());

    }




    @Test
    void testManual() {
        Message m;
        m = Message.makeMessage("ACK", "tim", null, null);
        assertTrue(m.isAcknowledge());
        m = Message.makeMessage("NAK", null, null, null);
        assertFalse(m.isDisplayMessage());
        assertEquals("NAK 2 -- 2 -- 2 --", m.toString());

        m= Message.makeMessage("LOG", "abc", "def", "hi");
        assertTrue(m.isLoggerMessage());

        m = Message.makeMessage("PCL", "abc", "def", "hi");
        assertTrue(m.isPControlMessage());

        m = Message.makeMessage("RCL", "abc", "def", "hi");
        assertTrue(m.isRecall());

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