package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.ScanNetNB;
import edu.northeastern.ccs.im.SocketNB;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

import static edu.northeastern.ccs.im.server.Prattle.main;
import static org.junit.jupiter.api.Assertions.*;


//class MyClass implements Runnable {
//    public void run(){
//        String[] strings = {};
//        try {
//            main(strings);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}

class ClientRunnableTest {

    private ClientRunnable client;
    private PrintNetNB printer;
    private ScanNetNB scanner;
    private SocketNB socket;
    public static final int PORT = 4545;
    ServerSocketChannel serverSocket;

//    @BeforeAll
//    static void startServer(){
//        Thread t1 = new Thread(new MyClass ());
//        t1.start();
//    }

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = null;
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(PORT));
        // Create the Selector with which our channel is registered.
        Selector selector = SelectorProvider.provider().openSelector();
        // Register to receive any incoming connection messages.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress("localhost", PORT);
        socketChannel.connect(socketAddr);

        String toSend = "HLO 4 temp 2 --";
        List<String> msgs = new ArrayList<>();
        msgs.add(toSend);
        msgs.add("BCT 4 temp 4 test");
        msgs.add("BCT 4 temp 17 What is the date?");
        msgs.add("BCT 4 temp 29 Prattle says everyone log off");
        msgs.add("BCT 4 abcd 2 --");
        msgs.add("BYE 4 temp 2 --");
        for (String s : msgs) {
            ByteBuffer wrapper = ByteBuffer.wrap(s.getBytes());
            int bytesWritten = 0;
            while (bytesWritten != s.length()) {
                System.out.println(wrapper);
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
            cl.run();
            cl.run();
            cl.run();
            cl.run();
            cl.run();
            cl.run();
        } catch (Exception e) {

        }

    }

    /*@AfterEach
    void end() throws IOException {
        socket.close();
    }


    @Test
    void enqueueMessage() {
        Message message = Message.makeQuitMessage("tim");
        this.client.enqueueMessage(message);
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
    void isInitialized() {
        assertFalse(this.client.isInitialized());
    }

    @Test
    void run() throws IOException {

        printer = new PrintNetNB(socket);
        scanner = new ScanNetNB(socket);
        assertFalse(scanner.hasNextMessage());
        assertTrue(printer.print(Message.makeAcknowledgeMessage("Hello")));


        this.client.run();
    }

//    @Test
//    void setFuture() {
//    }
//
    @Test
    void terminateClient() throws IOException {
        try{

        }catch (Exception e) {
            assertEquals("Connection refused: no further information", e.getMessage());
        }
    }*/
}