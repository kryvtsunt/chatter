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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ScanNetNBTest {
    /**
     * The port number to listen on.
     */
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
        List<String> msgs = new ArrayList<>();
        msgs.add(toSend);
        msgs.add("BCT 4 temp 4 test");
        msgs.add("\\n");
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
    public void testNextMessage() {
        SocketChannel client = null;
        try {
            client = serverSocket.accept();
            client.configureBlocking(false);
        } catch (Exception e) {

        }
        List<String> tests = Arrays.asList("HLO 4 temp 2 --", "BCT 4 temp 4 test", "\\n");
        try {
            if (client != null) {
                input = new ScanNetNB(client);

                while (input.hasNextMessage()) {
                    for (String each : tests) {
                        Message msg = input.nextMessage();
                        System.out.println(msg.toString());
                        assertEquals(msg.toString(), each);
                    }
                }
            } else {
                System.out.println("socket channel is null");
            }
        } catch (Exception e) {
            assertEquals("No next line has been typed in at the keyboard", e.getMessage());
        }
    }

}