package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.channels.spi.SelectorProvider;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

class ScanNetNBTest {
    /** The port number to listen on. */
    public static final int PORT = 4545;
    ScanNetNB input;
    ServerSocketChannel serverSocket;

    @BeforeEach
    protected void setUp() throws Exception {
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

        ByteBuffer wrapper = ByteBuffer.wrap(toSend.getBytes());
        int bytesWritten = 0;
        while (bytesWritten != toSend.length()) {
            System.out.println(wrapper);
            bytesWritten += socketChannel.write(wrapper);
        }
    }


    @Test
    public void testNextMessage() {
        SocketChannel client = null;
        try {
            client = serverSocket.accept();
            client.configureBlocking(false);
        } catch(Exception e) {

        }
        if (client != null) {
            input = new ScanNetNB(client);
            if (input.hasNextMessage()) {
                Message msg = input.nextMessage();
                System.out.println(msg.toString());
                assertEquals(msg.toString(), "HLO 4 temp 2 --");
            }
        } else {
            System.out.println("socket channel is null");
        }
    }

}