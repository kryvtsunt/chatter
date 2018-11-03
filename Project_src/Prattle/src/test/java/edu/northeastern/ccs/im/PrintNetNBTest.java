package edu.northeastern.ccs.im;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PrintNetNBTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void print() {

    }

    @Test
    void exception() throws IOException {
        SocketNB socket = new SocketNB("localhost", 1111);
        ScanNetNB scanner = new ScanNetNB(socket.getSocket());
        try {
            scanner.nextMessage();
        } catch(NextDoesNotExistException e){
            assertEquals("No next line has been typed in at the keyboard", e.getMessage());
        }

        scanner.close();
    }
}