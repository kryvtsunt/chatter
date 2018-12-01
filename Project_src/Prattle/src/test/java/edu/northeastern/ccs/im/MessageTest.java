package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class MessageTest {

    @Test
    void testMakeMessage() {
        Message message = Message.makeBroadcastMessage("tim", "hello");
        assertEquals("BCT 3 tim 2 -- 5 hello", message.toString());
        assertEquals("tim", message.getSender());
        assertEquals("hello", message.getText());
        assertTrue(message.isBroadcastMessage());
        assertTrue(message.isDisplayMessage());
        assertFalse(message.isAcknowledge());
        assertFalse(message.isInitialization());
        assertFalse(message.terminate());

        Message message2 = Message.makeAcknowledgeMessage("tim");
        assertTrue(message2.isAcknowledge());
        Message message3 = Message.makeNoAcknowledgeMessage();
        assertFalse(message3.isBroadcastMessage());
        assertFalse(message3.isDisplayMessage());
        assertFalse(message3.isInitialization());

        Message message4 = Message.makeMessage("ACK", "tim", null, null);
        Message message5 = Message.makeMessage("NAK", null,null,  null);
        assertTrue(message4.isAcknowledge());
        assertFalse(message5.isDisplayMessage());

        Message message6 = Message.makeHelloMessage("Hello");
        assertFalse(message6.isBroadcastMessage());
        assertEquals("NAK 2 -- 2 -- 2 --", message5.toString());

        assertEquals("LOG 3 abc 2 -- 2 --", Message.makeLoggerMessage("abc").toString());
        assertEquals("PCL 3 abc 3 tim 2 --", Message.makePControlMessage("abc", "tim").toString());
        assertEquals("RCL 3 abc 2 -- 2 hi", Message.makeRecallMessage("abc","hi").toString());
        assertEquals("RLE 3 abc 3 def 2 hi", Message.makeSetRoleMessage("abc","def","hi").toString());
        assertEquals("WTU 3 abc 3 def 2 hi", Message.makeWiretapUserMessage("abc","def","hi").toString());
        assertEquals("WTG 3 abc 3 def 2 hi", Message.makeWiretapGroupMessage("abc","def","hi").toString());

        assertEquals("APR 3 abc 3 def 2 hi", Message.makeWiretapApproveMessage("abc","def","hi").toString());
        assertEquals("RJT 3 abp 3 def 2 hi", Message.makeWiretapRejectMessage("abp","def","hi").toString());

        Message m = Message.makeMessage("LOG","abc","def","hi");
        assertTrue(m.isLoggerMessage());

        m = Message.makeMessage("PCL","abc","def","hi");
        assertTrue(m.isPControlMessage());

        m = Message.makeMessage("RCL","abc","def","hi");
        assertTrue(m.isRecall());

        m = Message.makeMessage("WTU","abc","def","hi");
        assertTrue(m.isWiretapUserMessage());
        assertFalse(m.isWiretapGroupMessage());


        m = Message.makeMessage("APR","abc","def","hi");
        assertTrue(m.isApproveMessage());

        m = Message.makeMessage("RJT","abc","def","hi");
        assertTrue(m.isRejectMessage());

        m = Message.makeMessage("RLE","abc","def","hi");
        assertTrue(m.isSetRoleMessage());
    }

}