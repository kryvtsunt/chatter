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
    private Thread serverThread;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        ServerRunnable server = new PrattleTest().new ServerRunnable();
        serverThread = new Thread(server);
        serverThread.start();
        Thread.sleep(1000);
    }
    @AfterEach
    void tearDown() throws IOException {
        serverThread.interrupt();
        if(serverThread.isAlive()) {
            serverThread.stop();
        }
        Prattle.getServerSocket().socket().close();
        Prattle.getServerSocket().close();

    }
    @Test
    void broadcastMessage() throws IOException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException, InterruptedException {
        Message loginmsg = Message.makeSimpleLoginMessage("temp");
        Message msg = Message.makeBroadcastMessage("test message", "temp");
        Message quitMsg = Message.makeQuitMessage("temp");

        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress("localhost", ServerConstants.PORT);
        socketChannel.connect(socketAddr);
        //SocketNB sc2 = new SocketNB("localhost",ServerConstants.PORT);
        PrintNetNB printer = new PrintNetNB(socketChannel);
        printer.print(loginmsg);

        Thread.sleep(1000);
        Prattle.broadcastMessage(msg);

        Queue<Message> waitingList = new ConcurrentLinkedQueue<Message>();
        waitingList = ClientRunnable.getWaitingList();
        assertEquals(msg.getText(), Objects.requireNonNull(waitingList.poll()).getText());

        printer.print(quitMsg);
        socketChannel.close();
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