package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class MessageTest {

    @Test
    void testMakeMessage() {
        Message message = Message.makeBroadcastMessage("tim", "hello");
        assertEquals("BCT 3 tim 5 hello", message.toString());
        assertEquals("tim", message.getName());
        assertEquals("hello", message.getText());
        assertTrue(message.isBroadcastMessage());
        assertTrue(message.isDisplayMessage());
        assertFalse(message.isAcknowledge());
        assertFalse(message.isInitialization());
        assertFalse(message.terminate());

<<<<<<< HEAD
        Message message2 = Message.makeAcknowledgeMessage("tim");
        assertTrue(message2.isAcknowledge());
        Message message3 = Message.makeNoAcknowledgeMessage();
        assertFalse(message3.isBroadcastMessage());
        assertFalse(message3.isDisplayMessage());
        assertFalse(message3.isInitialization());

        Message message4 = Message.makeMessage("ACK", "tim", null);
        Message message5 = Message.makeMessage("NAK", null, null);
        assertTrue(message4.isAcknowledge());
        assertFalse(message5.isDisplayMessage());

        Message message6 = Message.makeHelloMessage();
        assertFalse(message6.isBroadcastMessage());
        assertEquals("NAK 2 -- 2 --", message5.toString());

=======
        Message msg1 = Message.makeBroadcastMessage("Usr","Hello");
        assertEquals("BCT 3 Usr 5 Hello", msg1.toString());

        Message msg2 = Message.makeNoAcknowledgeMessage();
        assertEquals("NAK 2 -- 2 --",msg2.toString());
        assertFalse(msg2.isDirectMessage());

        Message msg3 = Message.makeAcknowledgeMessage("usr");
        assertEquals("ACK 3 usr 2 --", msg3.toString());
>>>>>>> master
    }

}