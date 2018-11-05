package edu.northeastern.ccs.im;

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

class SocketNBTest {


    @Test
    void getSocket() throws IOException {
        ServerSocketChannel serverSocket;
        SocketChannel socketChannel;
        int port = 4517;

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

        SocketNB socketNB= new SocketNB("localhost", port);
        assertNotNull(socketNB.getSocket());
        ScanNetNB scanner = new ScanNetNB(socketNB);
        assertFalse(scanner.hasNextMessage());
        socketNB.close();

    }

}