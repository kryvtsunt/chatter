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
        List<String> msgs = new ArrayList<>();

        String toSend = "HLO 4 temp 2 --";
        Message m = Message.makeNoAcknowledgeMessage();
        msgs.add(m.toString());
        msgs.add(toSend);
        msgs.add("BCT 4 temp 4 test");
        msgs.add("BCT 4 temp 17 What is the date?");
        msgs.add("BCT 4 temp 29 Prattle says everyone log off");
        msgs.add("BCT 4 abcd 2 --");
        msgs.add("HLO 26 TooDumbToEnterRealUsername 2 --");
        msgs.add("HLO 7 BOUNCER 2 --");
        msgs.add("HLO 0" + "\\n" + "2 --");
        msgs.add("BCT 0 " + " 2 --");
        msgs.add("BYE 4 temp 2 --");


        for (String s : msgs) {
            ByteBuffer wrapper = ByteBuffer.wrap(s.getBytes());
            int bytesWritten = 0;
            while (bytesWritten != s.length()) {
                bytesWritten += socketChannel.write(wrapper);
            }
        }
    }

    @Test
    void testBroadcastMessageIsSpecial() {
        SocketChannel client = null;

        try {
            client = serverSocket.accept();
            client.configureBlocking(false);
            ClientRunnable cl = new ClientRunnable(client);
            assertFalse(cl.isInitialized());
            assertFalse(cl.isValidated());
            cl.run();
            assertEquals(-1, cl.getUserId());
            cl.run();
            assertNull(cl.getWaitingList());
            cl.run();
            cl.run();
            cl.run();
            String name = cl.getName();
            assertEquals("TooDumbToEnterRealUsername", name);
            cl.setName("tim");
            name = cl.getName();
            assertEquals("tim", name);
            int id = cl.getUserId();
            id = cl.getUserId();
            assertNotEquals(0, id);
            assertNotEquals(-1, id);
            assertTrue(cl.isInitialized());
            cl.run();
            cl.run();
            cl.run();
            cl.run();
            cl.run();
            cl.run();

            cl.terminateClient();

        } catch (Exception ignored) {
        }

    }

}