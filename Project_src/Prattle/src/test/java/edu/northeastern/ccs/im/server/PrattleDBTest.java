package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the simple "data base"(text-file) PrattleDB
 */
class PrattleDBTest {

    @Test
    void testDB() throws IOException {
        PrattleDB db = PrattleDB.instance();
        db.create("tim", "1234");
        db.delete("tim");
        db.create("tim", "1234");
        db.update("tim", "12345");
        assertEquals("12345", db.retrieve("tim"));
        db.delete("tim", "12345");
        db.create("tim", "timberman_42");
    }
}