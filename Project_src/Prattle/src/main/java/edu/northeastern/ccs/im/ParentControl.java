package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.server.FileDB;

import java.io.*;
import java.util.Arrays;

public class ParentControl {
    /**
     * List containing the bad words
     */
    private String[] list_bad_words;

    private static ParentControl _instance;

    /**
     * Method to read the data from the text file and create a list of bad words
     */
    private ParentControl() {
        try {
//            File file = new File("badwords.txt");
//            FileInputStream in = new FileInputStream(file);
            InputStream in = ParentControl.class.getClassLoader().getResourceAsStream("badwords.txt");
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            while (line != null) {
                content.append(line).append(System.lineSeparator());
                line = reader.readLine();
            }
            list_bad_words = content.toString().replaceAll("\\r", "").split("\n");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static ParentControl getInstance() {
        if (_instance == null) {
            _instance = new ParentControl();
        }
        return _instance;
    }

    /**
     * Filters out the profanities with stars
     *
     * @param message string which is entered by the user
     * @return a filtered message with stars replacing the bad words
     */
    public String filterBadWords(final String message) {
        StringBuffer StringBuffer = new StringBuffer();
        String[] words = message.split("\\s");
        for (String word : words) {
            String cuss = word.toLowerCase().replaceAll("\\W+", "");
            checkPatterns(word, cuss, StringBuffer);
            StringBuffer.append(' ');
        }
        return StringBuffer.substring(0, StringBuffer.length() - 1);
    }

    /**
     * This method checks for the cuss word in the dictionary and replaces it with *
     *
     * @param word          the word detected in the message
     * @param cuss          the word matched to the cuss word dictionary
     * @param StringBuffer StringBuffer for concatenating the message
     */
    private void checkPatterns(final String word, final String cuss, final StringBuffer StringBuffer) {
        for (String pattern : list_bad_words) {
            if (cuss.matches(pattern)) {
                char[] stars = new char[cuss.length()];
                Arrays.fill(stars, '*');
                String rep = new String(stars);
                StringBuffer.append(word.replaceAll("\\w+", rep));
                return;
            }
        }
        StringBuffer.append(word);
    }
}