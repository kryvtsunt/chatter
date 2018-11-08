package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.Test;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;


class PrattleTest {


    // Runs the main function in parallel thread to test server communication
    @Test
    void testMain() throws IOException, SecurityException,
            IllegalArgumentException,InterruptedException {
        ServerRunnable server = new PrattleTest().new ServerRunnable();
        Thread serverThread = new Thread(server);
        serverThread.start();
        Thread.sleep(1500);
        Message loginmsg = Message.makeLoginMessage("username");
        Message passwordmsg = Message.makeBroadcastMessage("username", "password");
        Message msg = Message.makeBroadcastMessage("username", "test");
        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress("localhost", ServerConstants.PORT);
        socketChannel.connect(socketAddr);
        PrintNetNB printer = new PrintNetNB(socketChannel);
        printer.print(loginmsg);
        printer.print(passwordmsg);
        socketChannel.close();
        Thread.sleep(10000);
        Prattle.directMessage(msg, "username");
        Prattle.broadcastMessage(msg);
        serverThread.interrupt();
    }

    // Creates a runnable main function
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