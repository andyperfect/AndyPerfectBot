package test.com.afome;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import com.afome.ChatBot.ChatBotUtils;

public class ChatBotUtilsTest {

    @Before
    public void before() throws Exception {
        System.out.println("Beginning ChatBotUtilsTest");
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testContainsLink() throws Exception {
        String[] validLinkTests = new String[]{
                "http://www.rpgspeedruns.com",
                "www.evillink.po",
                "ev.il.lin",
                "This is my string with a bad url! uu.fr",
                "dlsjhf ef wfisdl wief bit.ly/hello ajh efh aelk;f",
                "i lied theres more cards here http://www.hearthpwn.com/news/1082-every-card-from-the-league-of-explorers-adventure",
                "fw.gg/N0X0BA",
        };

        String[] invalidLinkTests = new String[]{
                "This has no url.",
                "I don't even know what this is. I dont think this has a link.",
                "Raka: Sorry, missed your question for a bit. Yeah, the general idea is that the current \"Japanese\" population/ethnic group came over from Korea during an iron age time known as the Yayoi Period",
                "Belly is way easier to hit though",
                "Yeah, he's almost never facing away from you",
                "NerdyGerman, that's really interesting! Thank you for your insight!",
                "lol",
                "no prob, always happy to geek out on history. Used to do it during my streams too...which is probably why my viewer count was always lousy, lol",
                "yeah the 12th. um 700 gold or 6.99$ @thatwonkybrow47",
        };

        for (String testString : validLinkTests) {
            Assert.assertTrue(ChatBotUtils.containsLink(testString));
        }

        for (String testString : invalidLinkTests) {
            Assert.assertFalse(ChatBotUtils.containsLink(testString));
        }


    }
}