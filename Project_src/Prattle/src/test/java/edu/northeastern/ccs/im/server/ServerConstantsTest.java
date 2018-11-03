package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static edu.northeastern.ccs.im.Message.*;
import static org.junit.jupiter.api.Assertions.*;


class ServerConstantsTest {
    ServerConstants serverConstants;
    private GregorianCalendar cal = new GregorianCalendar();

    private String currentTime;
    private String currentDate;

    public void timeAndDate() {
        currentTime = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        currentDate = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE) + "/" + cal.get(Calendar.YEAR);
    }



    @Test
    void testGetBroadcastResponses() {
        timeAndDate();

        Message coolMsg = makeBroadcastMessage("Prattle", "OMG ROFL TTYL");


        List<Message> impatientMsg = new ArrayList<>();
        impatientMsg.add(makeBroadcastMessage("BBC", "The time is now"));
        impatientMsg.add(makeBroadcastMessage("Mr. Fox", currentTime));
        List<Message> helloMsg = new ArrayList<>();
        String NIST_NAME = "NIST";
        String SERVER_NAME = "Prattle";
        helloMsg.add(makeBroadcastMessage(SERVER_NAME, "Hello.  How are you?"));
        helloMsg.add(makeBroadcastMessage(SERVER_NAME, "I can communicate with you to test your code."));

        Message timeMsg = makeBroadcastMessage(NIST_NAME, currentTime);
        Message dateMsg = makeBroadcastMessage(NIST_NAME, currentDate);
        List<Message> queryMsg = new ArrayList<>();

        queryMsg.add(makeBroadcastMessage(SERVER_NAME,"Why are you asking me this?"));
        queryMsg.add(makeBroadcastMessage(SERVER_NAME,"I am a computer program. I run."));

        Message emptyMsg = makeBroadcastMessage(NIST_NAME, "test");

        String IMPATIENT_COMMAND = "What time is it Mr. Fox?";
        String COOL_COMMAND = "WTF";
        assertEquals(coolMsg.toString(), ServerConstants.getBroadcastResponses(COOL_COMMAND).get(0).toString());
        assertEquals(impatientMsg.get(0).toString(), ServerConstants.getBroadcastResponses(IMPATIENT_COMMAND).get(0).toString());
        assertEquals(impatientMsg.get(1).toString(), ServerConstants.getBroadcastResponses(IMPATIENT_COMMAND).get(1).toString());
        String HELLO_COMMAND = "Hello";
        assertEquals(helloMsg.get(0).toString(), ServerConstants.getBroadcastResponses(HELLO_COMMAND).get(0).toString());
        assertEquals(helloMsg.get(1).toString(), ServerConstants.getBroadcastResponses(HELLO_COMMAND).get(1).toString());
        String TIME_COMMAND = "What time is it?";
        assertEquals(timeMsg.toString(),ServerConstants.getBroadcastResponses(TIME_COMMAND).get(0).toString());
        String DATE_COMMAND = "What is the date?";
        assertEquals(dateMsg.toString(),ServerConstants.getBroadcastResponses(DATE_COMMAND).get(0).toString());
        String QUERY_COMMAND = "How are you?";
        assertEquals(queryMsg.get(0).toString(), ServerConstants.getBroadcastResponses(QUERY_COMMAND).get(0).toString());
        assertEquals(queryMsg.get(1).toString(), ServerConstants.getBroadcastResponses(QUERY_COMMAND).get(1).toString());


    }
}