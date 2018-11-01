package edu.northeastern.ccs.im;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        Message message7 = Message.makeHelloMessage("heelo");

        message.toString();
        message.getName();
        message.getText();
        message.isBroadcastMessage();
        message.isDisplayMessage();
        message.isAcknowledge();
        message.isInitialization();
        message.terminate();
    }

    @Test
    void makeBroadcastMessage() {
    }

    @Test
    void makeHelloMessage() {
    }

    @Test
    void makeMessage() {
    }

    @Test
    void makeNoAcknowledgeMessage() {
    }

    @Test
    void makeAcknowledgeMessage() {
    }

    @Test
    void makeSimpleLoginMessage() {
    }

    @Test
    void getName() {
    }

    @Test
    void getText() {
    }

    @Test
    void isAcknowledge() {
    }

    @Test
    void isBroadcastMessage() {
    }

    @Test
    void isDisplayMessage() {
    }

    @Test
    void isInitialization() {
    }

    @Test
    void terminate() {
    }

}