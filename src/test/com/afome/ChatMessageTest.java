package test.com.afome; 

import com.afome.ChatMessageType;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import org.junit.Assert;
import com.afome.ChatMessage;

public class ChatMessageTest {

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

@Test
public void testParseMessage() throws Exception { 
   ChatMessage message = new ChatMessage(":andyperfectbot!andyperfectbot@andyperfectbot.tmi.twitch.tv JOIN #andyperfect");
   Assert.assertEquals(ChatMessageType.JOIN, message.getMessageType());

} 

@Test
public void testGetUser() throws Exception { 
//TODO: Test goes here... 
} 

@Test
public void testGetUrl() throws Exception { 
//TODO: Test goes here... 
} 

@Test
public void testGetMessageType() throws Exception { 
//TODO: Test goes here... 
} 

@Test
public void testGetChannel() throws Exception { 
//TODO: Test goes here... 
} 

@Test
public void testGetMessage() throws Exception { 
//TODO: Test goes here... 
} 

@Test
public void testGetMessageMillis() throws Exception { 
//TODO: Test goes here... 
} 

@Test
public void testParseUserListMessage() throws Exception { 
//TODO: Test goes here... 

} 

@Test
public void testParseUserListMessageEnd() throws Exception { 
//TODO: Test goes here... 

} 

@Test
public void testParsePingMessage() throws Exception { 
//TODO: Test goes here... 

} 

@Test
public void testParseChatMessage() throws Exception { 
//TODO: Test goes here... 

} 

@Test
public void testParseJoinMessage() throws Exception { 
//TODO: Test goes here... 

} 

@Test
public void testParsePartMessage() throws Exception { 
//TODO: Test goes here... 

} 

@Test
public void testParseUser() throws Exception { 
//TODO: Test goes here... 

} 

} 
