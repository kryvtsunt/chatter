package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.server.Prattle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class is similar to the java.io.PrintWriter class, but this class's
 * methods work with our non-blocking Socket classes. This class could easily be
 * made to wait for network output (e.g., be made &quot;non-blocking&quot; in
 * technical parlance), but I have not worried about it yet.
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class PrintNetNB {
    /**
     * Channel over which we will write out any messages.
     */
    private final SocketChannel channel;

    /**
     * Number of times to try sending a message before we give up in frustration.
     */
    private static final int MAXIMUM_TRIES_SENDING = 100;

    private static final Logger LOGGER = Logger.getLogger(Prattle.class.getName());

    /**
     * Creates a new instance of this class. Since, by definition, this class sends
     * output over the network, we need to supply the non-blocking Socket instance
     * to which we will write.
     *
     * @param sockChan Non-blocking SocketChannel instance to which we will send all
     *                 communication.
     */
    public PrintNetNB(SocketChannel sockChan) {
        // Remember the channel that we will be using.
        channel = sockChan;
    }

    /**
     * Creates a new instance of this class. Since, by definition, this class sends
     * output over the network, we need to supply the non-blocking Socket instance
     * to which we will write.
     *
     * @param connection Non-blocking Socket instance to which we will send all
     *                   communication.
     */
    public PrintNetNB(SocketNB connection) {
        // Remember the channel that we will be using.
        channel = connection.getSocket();
    }

    /**
     * Send a Message over the network. This method performs its actions by printing
     * the given Message over the SocketNB instance with which the PrintNetNB was
     * instantiated. This returns whether our attempt to send the message was
     * successful.
     *
     * @param msg Message to be sent out over the network.
     * @return True if we successfully send this message; false otherwise.
     */
    public boolean print(Message msg) {
        String str = msg.toString();
        ByteBuffer wrapper = ByteBuffer.wrap(str.getBytes());
        int bytesWritten = 0;
        int attemptsRemaining = MAXIMUM_TRIES_SENDING;
        while (wrapper.hasRemaining() && (attemptsRemaining > 0)) {
            try {
                attemptsRemaining--;
                bytesWritten += channel.write(wrapper);
            } catch (IOException e) {
                return false;
            }
        }
        // Check to see if we were successful in our attempt to write the message
        if (wrapper.hasRemaining()) {
            LOGGER.log(Level.WARNING, "WARNING: Not all bytes were sent -- dropping this user. ");
            assert bytesWritten != 0;
            return false;
        }
        return true;
    }
}
