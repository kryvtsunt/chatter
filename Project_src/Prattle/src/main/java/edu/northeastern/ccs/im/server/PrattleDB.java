package edu.northeastern.ccs.im.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * A simple "Data Base" implemented (CRUD operations on the text file that has information about all users)
 * Uses singleton design pattern.
 */
public class PrattleDB {

    /**
     * A Data Structure that contatins a mapping of usernames to the user passwords.
     */

    private static PrattleDB instance;

    private Map<String, String> users = new HashMap<>();

    private String name = "db.txt";

    private PrattleDB() {
    }

    /**
     * get the instance of the state
     *
     * @return instance of the state (create new if it does not exist);
     */
    public static PrattleDB instance() {
        if (instance == null) {
            instance = new PrattleDB();
        }
        return instance;
    }

    /**
     * Read DB from the file
     *
     * @throws FileNotFoundException if file is not found
     */
    private boolean readDB() {
        String str = "";
        String key;
        String value;
        File file = new File(name);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            return false;
        }
        while (scanner.hasNextLine()) {
            str = scanner.nextLine();
            String[] args = str.split("-");
            key = args[0];
            value = args[1];
            if (!users.containsKey(key)) {
                users.put(key, value);
            }
        }
        scanner.close();
        return true;
    }

    /**
     * Write DB into the file
     */
    private boolean writeDB() {
        StringBuilder data = new StringBuilder();
        for (Map.Entry<String, String> entry : users.entrySet()) {
            data.append(entry.getKey()).append("-").append(entry.getValue()).append('\n');
        }
        try {
            Files.write(Paths.get(name), data.toString().getBytes());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Retrieves the user's password
     *
     * @param username username of the user to retrieve
     * @return password of the user
     */
    public String retrieve(String username) {
        if (readDB() && (users.containsKey(username))) {
                return users.get(username);
        }
        return null;
    }


    /**
     * Delete entry
     *
     * @param username username of the user to delete
     * @return true if operation is successful, false otherwise
     */
    public boolean delete(String username) {
        if (readDB()) {
            if (users.containsKey(username)) {
                users.remove(username);
            }
            return writeDB();
        }
        return false;

    }

    /**
     * Update entry
     *
     * @param username user's username
     * @param password new user's password
     * @return true if operation is successful, false otherwise
     */
    public boolean update(String username, String password) {
        if (readDB()) {
            if (users.containsKey(username)) {
                users.put(username, password);
            }
            return writeDB();
        }
        return false;
    }

    /**
     * Delete entry
     *
     * @param username user's username
     * @param password user's password
     */
    public boolean delete(String username, String password) {
        if (readDB()) {
            if (users.containsKey(username) && users.get(username).equals(password)) {
                users.remove(username, password);
            }
            return writeDB();
        }
        return false;
    }

    /**
     * Create entry
     *
     * @param username user's username
     * @param password user's password
     * @return true if operation is successful, false otherwise
     */
    public boolean create(String username, String password) {
        if (readDB()) {
            if (!users.containsKey(username)) {
                users.put(username, password);
            }
            return writeDB();
        }
        return false;
    }

    public void reset(String str){
        name = str;
    }
}
