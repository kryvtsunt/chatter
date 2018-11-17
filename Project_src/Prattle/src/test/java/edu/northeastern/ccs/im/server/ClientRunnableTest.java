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

/**
 * tests for the class client runnable
 */
class ClientRunnableTest {
    private static final int port = 4548;
    private static final int port2 = 4556;

    // tests user brodcast and dirrect messages
    @Test
    void testClientRunnable() throws IOException {
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


        msgs.add(Message.makeLoginMessage("username"));
        msgs.add(Message.makeBroadcastMessage("username", "password"));
        msgs.add(Message.makeBroadcastMessage("username", "broadcast text"));
        msgs.add(Message.makeDirectMessage("username", "receiverUser", "Hello"));
        msgs.add(Message.makeBroadcastMessage("username", "Hello"));
        msgs.add(Message.makeDeleteMessage("username", null));

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
        for (int i = 0; i < msgs.size(); i++) {
            client.run();
        }
        assertTrue(client.isValidated());
        assertTrue(client.isInitialized());
        assertNotEquals(0, client.getUserId());
        client.enqueueMessage(Message.makeAcknowledgeMessage(client.getName()));
        assertFalse(client.getWaitingList().isEmpty());
        try {
            client.run();
        } catch (ClosedSelectorException e) {
            assertNull(e.getMessage());
        }
        serverSocket.close();
    }


    // tests new user CRUD operations
    @Test
    void testCRUDinteraction() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(port2));
        Selector selector = SelectorProvider.provider().openSelector();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress("localhost", port2);
        socketChannel.connect(socketAddr);

        SocketChannel socketChannel2 = SocketChannel.open();
        socketChannel2.connect(socketAddr);

        List<Message> msgs2 = new ArrayList<>();
        PrintNetNB printer2 = new PrintNetNB(socketChannel);
        msgs2.add(Message.makeLoginMessage("username22"));
        msgs2.add(Message.makeBroadcastMessage("username22", "password22"));
        msgs2.add(Message.makeBroadcastMessage("username22", "broadcast text"));
        msgs2.add(Message.makeDirectMessage("username22", "receiverUser", "Hello"));
        msgs2.add(Message.makeUpdateMessage("username22", "newPassword"));
        msgs2.add(Message.makeRetrieveMessage("username22", "PASSWORD"));
        msgs2.add(Message.makeRetrieveMessage("username22", "EPASSWORD"));
        msgs2.add(Message.makeRetrieveMessage("username22", "GROUPS"));
        msgs2.add(Message.makeRetrieveMessage("username22", "GROUP test_group"));
        msgs2.add(Message.makeRetrieveMessage("username22", "GROUP_MESSAGES test_group"));
        msgs2.add(Message.makeJoinMessage("username22", "test_group"));
        msgs2.add(Message.makeRetrieveMessage("username22", "GROUP test_group"));
        msgs2.add(Message.makeGroupMessage("username22", "test_group", "hello"));
        msgs2.add(Message.makeRetrieveMessage("username22", "MESSAGES"));
        msgs2.add(Message.makeRetrieveMessage("username22", "GROUP_MESSAGES test_group"));
        msgs2.add(Message.makeLeaveMessage("username22", "test_group"));
        msgs2.add(Message.makeRetrieveMessage("username22", "USERS"));
        msgs2.add(Message.makeRetrieveMessage("username22", "ONLINE"));
        msgs2.add(Message.makeUpdateMessage("username22", "password22"));
        msgs2.add(Message.makeDeleteMessage("username22", "username22"));
        msgs2.add(Message.makeQuitMessage("username22"));
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
        for (int i = 0; i < msgs2.size(); i++) {
            try {
                client2.run();
            } catch (ClosedSelectorException e){

            }
        }
        try {
            client2.run();
        } catch (ClosedSelectorException e) {
            assertNull(e.getMessage());
        }

        serverSocket.close();
    }

}