package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the simple "data base"(text-file) FileDB
 */
class FileDBTest {

    @Test
    void testPrattleDB() {
        FileDB db = FileDB.instance();
        assertTrue(db.create("tim", "1234"));
        assertTrue(db.delete("tim"));
        assertTrue(db.delete("bubochka"));
        assertTrue(db.delete("bubochka", "asdf"));
        assertTrue(db.update("bubochka", "bsdbfb"));
        assertTrue(db.create("tim", "1234"));
        assertTrue(db.update("tim", "12345"));
        assertEquals("12345", db.retrieve("tim"));
        assertTrue(db.delete("tim", "12345"));
        assertTrue(db.create("tim", "timberman_42"));

        db.reset("db2.txt");
        assertFalse(db.create("tim", "1234"));
        assertFalse(db.update("tim", "1234"));
        assertFalse(db.delete("tim", "1234"));
        assertFalse(db.delete("tim"));
        assertNull(db.retrieve("tim"));
        assertFalse(db.create("tim", "1234"));

        db.reset("db.txt");

    }
}