package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.*;

class ClientRunnableTest {

    private ClientRunnable client;

    @BeforeEach
    void setUp() throws IOException {
        assertNull(this.client);
        SocketChannel socket = SocketChannel.open();
        this.client = new ClientRunnable(socket);
        assertNotNull(this.client);
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
    void run() {
        this.client.run();
    }

    @Test
    void setFuture() {
    }

    @Test
    void terminateClient() {
    }
}