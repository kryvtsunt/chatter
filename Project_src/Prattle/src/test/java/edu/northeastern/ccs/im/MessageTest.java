package edu.northeastern.ccs.im;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void makeQuitMessage() {
        Message message = Message.makeQuitMessage("a");
        Message messga2 = Message.makeBroadcastMessage("a","b");
        Message messgae3 = Message.makeSimpleLoginMessage("tim");
        Message message4 = Message.makeMessage("a", null, "c");
        Message message5 = Message.makeAcknowledgeMessage("tim");
        Message message6 = Message.makeNoAcknowledgeMessage();
        Message message7 = Message.makeHelloMessage();

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