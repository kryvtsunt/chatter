package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.SocketNB;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

class PrattleTest {
    ServerRunnable server;
    Thread serverThread;

    @Test
    void broadcastMessage() throws IOException, SecurityException,
            IllegalArgumentException, InterruptedException {
        server = new PrattleTest().new ServerRunnable();
        serverThread = new Thread(server);
        serverThread.start();
        Message login = Message.makeSimpleLoginMessage("tim");
        Message login2 = Message.makeSimpleLoginMessage("tom");
        Message msg = Message.makeBroadcastMessage("tim", "test");
        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress("localhost", ServerConstants.PORT);
        socketChannel.connect(socketAddr);
        PrintNetNB printer = new PrintNetNB(socketChannel);
        printer.print(login);
        printer.print(login2);
        socketChannel.close();
        Thread.sleep(500);
        Prattle.directMessage(msg, "tom");
        Prattle.broadcastMessage(msg);
        serverThread.interrupt();

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