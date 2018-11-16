package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SQLDBTest {
    @Test
    void testDBUtility() throws NoSuchProviderException, NoSuchAlgorithmException, SQLException {
        /*DBUtility db = DBUtility.getInstance();
        db.delete("test1");
        assertEquals(true, db.create(1,"test1","test1"));
        assertEquals(false, db.create(1,"test1","test1"));
        assertEquals(false,db.validateCredentials("test1","test2"));
        assertEquals(true, db.update("test1","test2"));
        assertNotEquals("", db.retrieve("test1"));
        assertEquals(true,db.validateCredentials("test1","test2"));
        // Group Test
        db.deleteGroup("group1");
        db.deleteGroup("group2");
        assertEquals(true,db.createGroup("group1"));
        assertEquals(false, db.createGroup("group1"));
        assertEquals(true, db.deleteGroup("group1"));
        assertEquals(true, db.createGroup("group1"));
        assertEquals(true, db.addGroupMember("group1","test1"));
        db.delete("test2");
        assertEquals(true,db.create(2,"test2","test2"));
        assertEquals(true,db.addGroupMember("group1","test2"));
        // test retrieve
        List<String> userList = new ArrayList<>();
        List<String> userListDB = db.retrieveGroup("group1");
        userList.add("test1");
        userList.add("test2");
        for (int i = 0; i <userList.size(); i++) {
            assertEquals(userList.get(i),userListDB.get(i));
        }
        assertEquals(true, db.updateGroup("group1","group2"));*/
    }
}