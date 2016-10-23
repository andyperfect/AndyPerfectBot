package test.com.afome;

import com.afome.ChatBot.ChatMessageType;
import com.afome.ChatBot.DataFileIO;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import com.afome.ChatBot.UserData;

public class DataFileIOTest {

    @Before
    public void before() throws Exception {
        System.out.println("Beginning ChatMessageTest");
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testgetUserAtChatRank() throws Exception {
        DataFileIO dataFileIO = new DataFileIO();
        UserData userData = dataFileIO.getUserAtChatRank("andyperfect", 4);
        System.out.println(userData.getUser());
    }
}