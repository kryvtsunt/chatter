package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

class ClientRunnableTest {
    private static final int port = 4548;
    private static final int port2 = 4549;


    @Test
    void test1() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(port));
        Selector selector = SelectorProvider.provider().openSelector();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress("localhost", port);
        socketChannel.connect(socketAddr);
        List<Message> msgs = new ArrayList<>();
        PrintNetNB printer = new PrintNetNB(socketChannel);


        msgs.add(Message.makeSimpleLoginMessage("username"));
        msgs.add(Message.makeBroadcastMessage("username", "password"));
        msgs.add(Message.makeBroadcastMessage("username", "broadcast text"));
        msgs.add(Message.makeBroadcastMessage("username", "receiverUser> Hello"));
        msgs.add(Message.makeBroadcastMessage("username", "Hello"));

        for (Message msg : msgs) {
            printer.print(msg);
        }
        printer.print(Message.makeQuitMessage("username"));
        socketChannel.close();

        SocketChannel channel = serverSocket.accept();
        ClientRunnable client = new ClientRunnable(channel);
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(20);
        ScheduledFuture clientFuture = threadPool.scheduleAtFixedRate(client, 200,
                200, TimeUnit.MILLISECONDS);
        client.setFuture(clientFuture);

        assertFalse(client.isValidated());
        assertFalse(client.isInitialized());
        assertEquals(0, client.getUserId());
        assertTrue(client.getWaitingList().isEmpty());
        for (Message msg : msgs) {
            client.run();
        }
        assertTrue(client.isValidated());
        assertTrue(client.isInitialized());
        assertNotEquals(0, client.getUserId());
        client.enqueueMessage(Message.makeAcknowledgeMessage(client.getName()));
        assertFalse(client.getWaitingList().isEmpty());
        client.run();

        serverSocket.close();
    }


    @Test
    void test2() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(port2));
        Selector selector = SelectorProvider.provider().openSelector();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress("localhost", port2);
        socketChannel.connect(socketAddr);
        List<Message> msgs = new ArrayList<>();
        PrintNetNB printer = new PrintNetNB(socketChannel);

        SocketChannel socketChannel2 = SocketChannel.open();
        socketChannel2.connect(socketAddr);

        List<Message> msgs2 = new ArrayList<>();
        PrintNetNB printer2 = new PrintNetNB(socketChannel);
        msgs2.add(Message.makeSimpleLoginMessage("username2"));
        msgs2.add(Message.makeBroadcastMessage("username2", "password"));
        msgs2.add(Message.makeBroadcastMessage("username2", "broadcast text"));
        msgs2.add(Message.makeBroadcastMessage("username2", "receiverUser1,recieverUser2> Hello"));
        msgs2.add(Message.makeBroadcastMessage("username2", "UPDATE newPassword"));
        msgs2.add(Message.makeBroadcastMessage("username2", "RETRIEVE"));
        msgs2.add(Message.makeBroadcastMessage("username2", "DELETE"));
        for (Message msg : msgs2) {
            printer2.print(msg);
        }
        socketChannel2.close();

        SocketChannel channel2 = serverSocket.accept();
        ClientRunnable client2 = new ClientRunnable(channel2);
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(20);
        ScheduledFuture clientFuture = threadPool.scheduleAtFixedRate(client2, 200,
                200, TimeUnit.MILLISECONDS);
        client2.setFuture(clientFuture);
        for (Message msg : msgs2) {
            client2.run();
        }
        try {
            client2.run();
        } catch (ClosedSelectorException e){
            assertNull(e.getMessage());
        }

        serverSocket.close();
    }

}