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
    private static Map<String, String> users = new HashMap<>();

    private static PrattleDB instance;

    private PrattleDB(){ }

    /**
     * get the instance of the state
     * @return instance of the state (create new if it does not exist);
     */
    public static PrattleDB  instance(){
        if (instance == null) {
            instance = new  PrattleDB();
        }
        return instance;
    }

    /**
     * Read DB from the file
     * @throws FileNotFoundException if file is not found
     */
    private void readDB() throws FileNotFoundException {
        String str = "";
        String key;
        String value;
        File file = new File("db.txt");
        Scanner scanner = new Scanner(file);
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
    }

    /**
     * Write DB into the file
     * @throws IOException when not able to write
     */
    private void writeDB() throws IOException {
        StringBuilder data = new StringBuilder();
        for (Map.Entry<String, String> entry : users.entrySet()) {
            data.append(entry.getKey()).append("-").append(entry.getValue()).append('\n');
        }
        Files.write(Paths.get("db.txt"), data.toString().getBytes());
    }

    /**
     * Retrieves the user's password
     * @param username username of the user to retrieve
     * @return password of the user
     * @throws FileNotFoundException when file to read from is not found
     */
    public String retrieve(String username) throws FileNotFoundException {
        readDB();
        if (users.containsKey(username)){
            return users.get(username);
        }
        return null;
    }


    /**
     * Delete entry
     * @param username username of the user to delete
     * @throws IOException when not able to write
     */
    public void delete(String username) throws IOException {
        readDB();
        if (users.containsKey(username)) {
            users.remove(username);
        }
        writeDB();

    }

    /**
     * Update entry
     * @param username user's username
     * @param password new user's password
     * @throws IOException when not able to write
     */
    public void update(String username, String password) throws IOException {
        readDB();
        if (users.containsKey(username)) {
            users.put(username, password);
        }
        writeDB();
    }

    /**
     * Delete entry
     * @param username user's username
     * @param password user's password
     * @throws IOException when not able to write
     */
    public void delete(String username, String password) throws IOException {
        readDB();
        if (users.containsKey(username) && users.get(username).equals(password)) {
            users.remove(username, password);
        }
        writeDB();
    }

    /**
     *
     * Create entry
     * @param username user's username
     * @param password user's password
     * @throws IOException when not able to write
     */
    public void create(String username, String password) throws IOException {
        readDB();
        if (!users.containsKey(username)) {
            users.put(username, password);
        }
        writeDB();
    }
}
