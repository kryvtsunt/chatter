package edu.northeastern.ccs.im.server;


import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.ScanNetNB;
import edu.northeastern.ccs.im.SocketNB;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ClientRunnableTest {

    private ClientRunnable client;
    private PrintNetNB printer;
    private ScanNetNB scanner;
    private SocketNB socket;
    private static final int PORT = 4548;
    private ServerSocketChannel serverSocket;
    private List<Message> msgs;


    @BeforeEach
    void setUp() throws IOException {
        serverSocket = null;
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(PORT));
        Selector selector = SelectorProvider.provider().openSelector();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress("localhost", PORT);
        socketChannel.connect(socketAddr);
        msgs = new ArrayList<>();

        msgs.add(Message.makeSimpleLoginMessage("username"));
        msgs.add(Message.makeBroadcastMessage("username","password"));
        msgs.add(Message.makeBroadcastMessage("username","broadcast text"));
        msgs.add(Message.makeBroadcastMessage("username", "receiverUser>Hello"));
        msgs.add(Message.makeBroadcastMessage("username", "UPDATE newPassword"));
        msgs.add(Message.makeBroadcastMessage("username","pass"));
        msgs.add(Message.makeBroadcastMessage("username","broadcast text"));
        msgs.add(Message.makeBroadcastMessage("username","Hello"));
        msgs.add(Message.makeBroadcastMessage("username", "DELETE"));
        msgs.add(Message.makeAcknowledgeMessage("username"));
        msgs.add(Message.makeNoAcknowledgeMessage());
        msgs.add(Message.makeQuitMessage("username"));

        PrintNetNB printer = new PrintNetNB(socketChannel);
        for(Message msg : msgs) {
            printer.print(msg);
        }
        socketChannel.close();

    }

    @Test
    void testBroadcastMessageIsSpecial() {
        SocketChannel client = null;

        try {
            client = serverSocket.accept();
            client.configureBlocking(false);
            ClientRunnable cl = new ClientRunnable(client);
            int i= 0;
            assertEquals(0,cl.getUserId());
            cl.run();
            cl.run();
            cl.setValidated();
            assertTrue(cl.isValidated());
            while (i<14) {
                cl.run();
                i++;
            }

            cl.terminateClient();

        } catch (Exception ignored) {
        }

    }

}