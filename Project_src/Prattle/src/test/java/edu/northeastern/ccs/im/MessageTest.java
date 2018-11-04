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
    }

}