package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class MessageTest {

    @Test
    void testMakeMessage() {
        Message message = Message.makeBroadcastMessage("tim", "hello");
        assertEquals("BCT 3 tim 5 hello", message.toString());
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
        assertEquals("NAK 2 -- 2 --", message5.toString());

    }

}