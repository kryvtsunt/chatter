package edu.northeastern.ccs.im;


import java.io.*;
import java.util.Arrays;

import static edu.northeastern.ccs.im.server.ClientRunnable.LOGGER;

public class ParentControl {
    /**
     * List containing the bad words
     */
    private String[] words;

    private static ParentControl instance;

    /**
     * Method to read the data from the text file and create a list of bad words
     */
    private ParentControl() {
        try {
            InputStream in = ParentControl.class.getClassLoader().getResourceAsStream("badwords.txt");
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            while (line != null) {
                content.append(line).append(System.lineSeparator());
                line = reader.readLine();
            }
            words = content.toString().replaceAll("\\r", "").split("\n");
        } catch (final IOException e) {
            LOGGER.info("parent control fails to read from the file");

        }
    }

    public static ParentControl getInstance() {
        if (instance == null) {
            instance = new ParentControl();
        }
        return instance;
    }

    /**
     * Filters out the profanities with stars
     *
     * @param message string which is entered by the user
     * @return a filtered message with stars replacing the bad words
     */
    public String filterBadWords(final String message) {
        StringBuilder sb = new StringBuilder();
        String[] words = message.split("\\s");
        for (String word : words) {
            String cuss = word.toLowerCase().replaceAll("\\W+", "");
            checkPatterns(word, cuss, sb);
            sb.append(' ');
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * This method checks for the cuss word in the dictionary and replaces it with *
     *
     * @param word          the word detected in the message
     * @param cuss          the word matched to the cuss word dictionary
     * @param sb StringBuffer for concatenating the message
     */
    private void checkPatterns(final String word, final String cuss, final StringBuilder sb) {
        for (String pattern : words) {
            if (cuss.matches(pattern)) {
                char[] stars = new char[cuss.length()];
                Arrays.fill(stars, '*');
                String rep = new String(stars);
                sb.append(word.replaceAll("\\w+", rep));
                return;
            }
        }
        sb.append(word);
    }
}