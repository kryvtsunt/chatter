package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;

import static org.junit.jupiter.api.Assertions.*;

class PrintNetNBTest {


    @Test
    void testException() throws IOException {
        SocketNB socket = new SocketNB("localhost", 1111);
        ScanNetNB scanner = new ScanNetNB(socket.getSocket());
        PrintNetNB printer = new PrintNetNB(socket);
        try {
            printer.print(Message.makeBroadcastMessage("tim", "tam"));
        } catch (Exception e){
            assertEquals(NotYetConnectedException.class, e.getClass());
        }
        try {
            scanner.nextMessage();
        } catch(NextDoesNotExistException e){
            assertEquals("No next line has been typed in at the keyboard", e.getMessage());
        }

        scanner.close();
    }
}