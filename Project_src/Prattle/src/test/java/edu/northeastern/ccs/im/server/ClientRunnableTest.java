package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.ScanNetNB;
import edu.northeastern.ccs.im.SocketNB;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;


class ClientRunnableTest {

    private ClientRunnable client;
    ServerSocketChannel serverSocket;
    SocketChannel socketChannel;
    int port = 4545;


    @BeforeEach
    void setUp() throws IOException {
        client = new ClientRunnable(SocketChannel.open());
    }


    @Test
    void testBroadcastMessageIsSpecial() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(port));
        Selector selector = SelectorProvider.provider().openSelector();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);


        socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress("localhost", port);
        socketChannel.connect(socketAddr);

        List<String> msgs = new ArrayList<>();
        msgs.add("HLO 4 temp 2 --");
        msgs.add("BCT 4 temp 4 test");
        msgs.add("BCT 4 temp 17 What is the date?");
        msgs.add("BCT 4 temp 29 Prattle says everyone log off");
        msgs.add("BYE 4 abcd 2 --");
        msgs.add("BYE 4 abcd 2 --");
        for (String s : msgs) {
            ByteBuffer wrapper = ByteBuffer.wrap(s.getBytes());
            int bytesWritten = 0;
            while (bytesWritten != s.length()) {
                System.out.println(wrapper);
                bytesWritten += socketChannel.write(wrapper);
            }
        }

        SocketChannel channel = serverSocket.accept();
        channel.configureBlocking(false);
        client = new ClientRunnable(channel);

        client.run();
        client.run();
        client.run();
        client.run();
        client.run();

        String name;
        name = this.client.getName();
        assertEquals("abcd", name);
        this.client.setName("tim");
        name = this.client.getName();
        assertEquals("tim", name);
        int id = this.client.getUserId();
        id = this.client.getUserId();
        assertNotEquals(0, id);
        assertNotEquals(-1, id);
        assertTrue(client.isInitialized());
        serverSocket.close();
    }

    @Test
    void getName() {
        String name;
        name = this.client.getName();
        assertNull(name);
    }

    @Test
    void setName() {
        String name;
        name = this.client.getName();
        assertNull(name);
        this.client.setName("tim");
        name = this.client.getName();
        assertEquals("tim", name);
    }

    @Test
    void getUserId() {
        int id;
        id = this.client.getUserId();
        assertEquals(0, id);
        this.client.setName("tim");
        id = this.client.getUserId();
        assertEquals(0, id);

    }


    @Test
    void enqueueMessage() {
        Message message = Message.makeSimpleLoginMessage("tim");
        this.client.enqueueMessage(message);
    }


    @Test
    void isInitialized() {
        assertFalse(this.client.isInitialized());
    }

    @Test
    void run(){
        this.client.run();
    }

    @Test
    void terminateClient() throws IOException {
        try {
            this.client.terminateClient();
        } catch (Exception e) {
            assertEquals(null, e.getMessage());
        }

    }
}