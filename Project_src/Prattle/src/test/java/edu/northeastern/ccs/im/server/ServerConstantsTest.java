package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import org.junit.jupiter.api.Test;

import static edu.northeastern.ccs.im.Message.*;


class ServerConstantsTest {
    ServerConstants serverConstants;

    @Test
    void getBroadcastResponses() {
        Message msg = makeBroadcastMessage("User1", "WTF");
//        System.out.println(ServerConstants.getBroadcastResponses("WTF").get(0));

    }
}