package test.com.afome;

import com.afome.ChatBot.TwitchUtils;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import com.afome.ChatBot.ChatBotUtils;

import java.util.ArrayList;

public class TwitchUtilsTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testGetUsersInChat() throws Exception {
        ArrayList<String> users = TwitchUtils.getUsersInChat("andyperfect");
        for (String user : users) {
            System.out.println(user);
        }
        Assert.assertTrue(users.size() > 0);

    }
}