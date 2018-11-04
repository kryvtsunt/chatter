package edu.northeastern.ccs.im;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void testMakeMessage() {
        Message message = Message.makeQuitMessage("a");
        message.toString();
        message.getName();
        message.getText();
        message.isBroadcastMessage();
        message.isDisplayMessage();
        message.isAcknowledge();
        message.isInitialization();
        message.terminate();
    }

}