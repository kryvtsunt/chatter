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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static edu.northeastern.ccs.im.server.Prattle.main;
import static org.junit.jupiter.api.Assertions.*;


class MyClass implements Runnable {
    public void run(){
        String[] strings = {};
        try {
            main(strings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientRunnableTest {

    private ClientRunnable client;
    private PrintNetNB printer;
    private ScanNetNB scanner;
    private SocketNB socket;

    @BeforeAll
    static void startServer(){
        Thread t1 = new Thread(new MyClass ());
        t1.start();
    }

    @BeforeEach
    void setUp() throws IOException {
        socket = new SocketNB("localhost", 4545);
        this.client = new ClientRunnable(socket.getSocket());
    }

    @AfterEach
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
//    @Test
//    void terminateClient() {
//    }
}