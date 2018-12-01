package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SlackMessageTest {
    @Test
    void message() throws IOException {
        SlackMessage slackMessage = new SlackMessage();
        slackMessage.slackMessage("Test");
    }

}