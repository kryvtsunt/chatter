package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.SocketNB;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

class PrattleTest {
    ServerRunnable server;
    Thread serverThread;

    @Test
    void broadcastMessage() throws IOException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException, InterruptedException {
        server = new PrattleTest().new ServerRunnable();
        serverThread = new Thread(server);
        serverThread.start();
        Message loginmsg = Message.makeSimpleLoginMessage("temp");
        Message msg = Message.makeBroadcastMessage("temp", "test message");
//        SocketNB sc2 = new SocketNB("localhost", 4545);
//
//        PrintNetNB printer = new PrintNetNB(sc2.getSocket());
//        printer.print(loginmsg);
//
//        Thread.sleep(500);
        Prattle.broadcastMessage(msg);

        Queue<Message> waitingList = new ConcurrentLinkedQueue<Message>();
//        waitingList = ClientRunnable.getWaitingList();
//        assertEquals(msg.getText(), waitingList.poll().getText());
        serverThread.interrupt();
//        Prattle.getServerSocket().close();
    }

    private class ServerRunnable implements Runnable {
        public void run()
        {
            try {
                Prattle.main(new String[2]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}