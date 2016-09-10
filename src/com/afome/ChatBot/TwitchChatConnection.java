package com.afome.ChatBot;

import com.afome.APBotMain;
import com.afome.ChatBot.ChatBotUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitchChatConnection {
    private static final Logger log = Logger.getLogger(APBotMain.class.getName());
    private String channel;

    private ConfigHandler config;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;
    DataFileIO fileIO;

    ArrayList<Quote> quotes;
    UserDataList fullUserDataList;

    private ArrayList<ChatMessage> chatLog;

    private long initialConnectionTime = -1;
    private long lastTwitchUserQueryTime = -1;
    private long lastDbWriteTime = -1;

    private boolean connected = false;

    public TwitchChatConnection(String channel) throws Exception {
        this.channel = channel;
        config = ConfigHandler.getInstance();
    }

    private void initialize() throws Exception {
        Socket socket = new Socket(config.getServerName(), config.getPort(this.channel));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        fileIO = new DataFileIO();
        fileIO.writeChannelToDatabase(ChatBotUtils.stripHashtagFromChannel(channel));
        fullUserDataList = fileIO.createUserDataFromDatabase(ChatBotUtils.stripHashtagFromChannel(channel));
        fullUserDataList.assignModerators(config.getMods(this.channel));

        quotes = fileIO.createQuoteListFromFile();

        if (chatLog == null) {
            chatLog = new ArrayList<ChatMessage>();
        }
        }


    public boolean connect() throws Exception {

        initialize();

        if (connectToServer()) {
            // Join the channel
            writer.write("JOIN " + channel + "\r\n");
            writer.flush();
            System.out.println("LOG: JOINED CHANNEL " + channel);

            initialConnectionTime = System.currentTimeMillis();
            lastTwitchUserQueryTime = System.currentTimeMillis() - ChatBotUtils.ONE_MINUTE_IN_MILLIS;
            lastDbWriteTime = initialConnectionTime;

            //sendChatMessage("Hi there! I'm just here to monitor for the time being. I'll lurk silently. I promise.");

            System.out.println("successfully connected to " + channel);
            connected = true;
            return true;
        } else {
            System.out.println("Failed to connect to " + channel);
            connected = false;
            return false;
        }


    }

    private boolean connectToServer() throws IOException {
        writer.write("PASS " + config.getPassword() + "\r\n");
        writer.write("NICK " + config.getNick() + "\r\n");
        writer.flush();

        // Read lines from the server until it tells us we have connected.
        String line;

        while ((line = reader.readLine( )) != null) {
            System.out.println("CONNECTING:"+ line);
            if (line.contains("004")) {
                System.out.println("Logged in");
                return true;
            }
            else if (line.contains("433")) {
                System.out.println("Nickname is already in use.");
                return false;
            }
        }
        return false;
    }

    public void terminateConnection() {
        fullUserDataList.updateAllUsers();
        fileIO.writeUserDataToDatabase(fullUserDataList);
        fileIO.writeQuoteListToFile(quotes);

        writer = null;
        reader = null;
        chatLog.clear();

        initialConnectionTime = -1;
        lastTwitchUserQueryTime = -1;
        lastDbWriteTime = -1;
        connected = false;
    }

    public void handleQueuedMessages() throws Exception {
        while (reader.ready()) {
            String line = reader.readLine();
            //System.out.println("NEW MESSAGE: " + line);
            ChatMessage message = new ChatMessage(line);
            if (message.getMessageType() == ChatMessageType.PING) {
                replyToPing(line);
            } else if (message.getMessageType() == ChatMessageType.USERLIST) {
                handleUserList(message);
            } else if (message.getMessageType() == ChatMessageType.USERLISTEND) {

            } else if (message.getMessageType() == ChatMessageType.JOIN) {
                handleJoinMessage(message);
            } else if (message.getMessageType() == ChatMessageType.PART) {
                handlePartMessage(message);
            } else if (message.getMessageType() == ChatMessageType.CHAT) {
                if (message.getChannel().equalsIgnoreCase(config.getNick())) {
                    handlePrivateMessage(message);
                } else {
                    chatLog.add(message);
                    handleChatMessage(message);
                }
            }
        }
    }

    public void replyToPing(String pingMessage) throws Exception {
        writer.write("PONG " + pingMessage.substring(5) + "\r\n");
        writer.flush();
    }

    public void sendChatMessage(String message) {
        System.out.println("LOG: Sending message in channel " + this.channel + ":" + message);
        try {
            writer.write("PRIVMSG " + this.channel + " :" + message + "\r\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleUserList(ChatMessage message) {
        String[] users = message.getUserList();
        if (users == null) {
            //Log some kind of error that we parsed a message as a userlist incorrectly
            return;
        }
        for (String user : users) {
            UserData userData = fullUserDataList.findUser(user);
            if (userData == null) {
                userData = fullUserDataList.createNewUser(message.getUser());
            }
            userData.joined();
        }
    }

    public UserData handleJoinMessage(ChatMessage message) {
        UserData userData = fullUserDataList.findUser(message.getUser());
        if (userData == null) {
            userData = fullUserDataList.createNewUser(message.getUser());
        }
        userData.joined();
        return userData;
    }

    public void handlePartMessage(ChatMessage message) {
        UserData userData = fullUserDataList.findUser(message.getUser());
        if (userData == null) {
            userData = fullUserDataList.createNewUser(message.getUser());
        }
        userData.parted();
    }

    public void handlePrivateMessage(ChatMessage message) {
        return;
    }

    //TODO Break out the individual commands into something a bit more dynamic (possibly their own methods?)
    public void handleChatMessage(ChatMessage message) throws Exception {
        UserData userData = fullUserDataList.findUser(message.getUser());
        if (userData == null) {
            userData = fullUserDataList.createNewUser(message.getUser());
        }
        userData.handleChatMessage();

        //If bot ban is enabled, the user has sent only 1 message and it's a link, insta-ban
        if (config.isBotBanEnabled(this.channel) && userData.getChatCount() <= 1 && ChatBotUtils.containsLink(message.getMessage())) {
            banUser(message, "First chat message contained a link. Assumed bot");
        }

        if (!userData.canUseBotCommand(config.getTimeBetweenUserCommands(this.channel))) {
            return;
        }

        if (!config.isBotCommandsEnabled(this.channel)) {
            return;
        }

        //USER COMMANDS
        if (message.getMessage().startsWith("!hp") && config.isUserTrackingCommandsEnabled(this.channel)) {
            userData.handleBotCommand(config.getTimeBetweenUserCommands(this.channel));
            String[] splitLine = message.getMessage().split("\\s+");
            if (splitLine.length == 1) {
                if (userData == null) {
                    sendChatMessage(message.getUser() + " has never been in this channel");
                } else {
                    sendChatMessage(message.getUser() + " has been active for " + ChatBotUtils.millisToReadableFormat(userData.getNumMillis()));
                }
            } else if (splitLine.length == 2) {
                UserData otherUserData = fullUserDataList.findUser(splitLine[1].toLowerCase());
                if (otherUserData == null) {
                    sendChatMessage(splitLine[1] + " has never been in this channel");
                } else {
                    if (splitLine[1].equalsIgnoreCase(config.getNick())) {
                        sendChatMessage("You don't need to know about me");
                    } else {
                        sendChatMessage(splitLine[1] + " has been active for " + ChatBotUtils.millisToReadableFormat(otherUserData.getNumMillis()));
                    }
                }
            }
        }

        if (message.getMessage().startsWith("!pp") && config.isUserTrackingCommandsEnabled(this.channel)) {
            userData.handleBotCommand(config.getTimeBetweenUserCommands(this.channel));
            String[] splitLine = message.getMessage().split("\\s+");
            if (splitLine.length == 1) {
                if (userData == null) {
                    sendChatMessage(message.getUser() + " has never been in this channel");
                } else {
                    sendChatMessage(message.getUser() + "' has sent " + userData.getChatCount() + " chat messages");
                }
            } else if (splitLine.length == 2) {
                UserData otherUserData = fullUserDataList.findUser(splitLine[1].toLowerCase());
                if (otherUserData == null) {
                    sendChatMessage(splitLine[1] + " has never been in this channel");
                } else {
                    if (splitLine[1].equalsIgnoreCase(config.getNick())) {
                        sendChatMessage("You don't need to know about me");
                    } else {
                        sendChatMessage(splitLine[1] + " has sent " + otherUserData.getChatCount() + " chat messages");
                    }
                }
            }
        }

        //Only users with that have been in chat for the configured time are allowed to use this option (And mods)
        if (message.getMessage().startsWith("!quote") && config.isQuotesEnabled(this.channel) &&
                (userData.getNumMillis() > config.getTimeNeededToQuote(this.channel) ||
                        userData.getUserType() == UserType.MODERATOR)) {
            userData.handleBotCommand(config.getTimeBetweenUserCommands(this.channel));
            String[] splitLine = message.getMessage().split("\\s+");
            if (splitLine.length == 1) {
                Quote quote = getRandomQuote();
                if (quote == null) {
                    sendChatMessage("There are no quotes available");
                } else {
                    sendChatMessage("\"" + quote.getQuote() + "\" (" + quote.getDate() + ")");
                }
            } else if (splitLine.length >= 3 &&
                    splitLine[1].equalsIgnoreCase("add") &&
                    splitLine[2].startsWith("\"") &&
                    splitLine[splitLine.length - 1].endsWith("\"")) {
                //If There are at least 3 tokens, the first one is "!quote", the second one is "add",
                //the third one starts with a quote and the last one ends with a quote, we're good
                //Example: !quote add "this is a valid quote"

                int firstQuoteIndex = message.getMessage().indexOf('"');
                int lastQuoteIndex = message.getMessage().lastIndexOf('"');
                String quoteString = message.getMessage().substring(firstQuoteIndex + 1, lastQuoteIndex);
                if (quoteString.length() > 1) {
                    quotes.add(new Quote(quoteString));
                    sendChatMessage("Added quote \"" + quoteString + "\"");
                } else {
                    sendChatMessage("Invalid quote length");
                }
            }
        }

        if (message.getMessage().equals("!uptime")) {
            userData.handleBotCommand(config.getTimeBetweenUserCommands(this.channel));
            long curTime = System.currentTimeMillis();
            sendChatMessage("The stream has been up for " + ChatBotUtils.millisToReadableFormat(curTime - initialConnectionTime));
        }

        if (message.getMessage().equals("!roulette") && config.isRouletteEnabled(this.channel)) {
            userData.handleBotCommand(config.getTimeBetweenUserCommands(this.channel));
            // Gets a random number from 1 - 128
            int rolledValue = ChatBotUtils.random.nextInt(128) + 1;
            String responseMessage = message.getUser() + " rolled a " + String.valueOf(rolledValue) + ". ";
            if (rolledValue == 128) {

                responseMessage += "A Gutsy Bat. A bomb drop. A timeout. RIP";
                if (userData.getUserType().equals(UserType.MODERATOR)) {
                    responseMessage += " ... @AndyPerfect, Timeout this guy. This mod's gotta go and I can't do it.";
                } else if (userData.getUserType().equals(UserType.USER)) {
                    sendChatMessage("/timeout " + message.getUser() + " 120");
                }
            } else if (rolledValue > 120) {
                responseMessage += "Mighty close. You're safe for now";
            } else if (rolledValue > 100) {
                responseMessage += "A high roll, but you're safe.";
            } else if (rolledValue > 80) {
                responseMessage += "On the high side, but you're safe.";
            } else if (rolledValue > 60) {
                responseMessage += "You really are quite average.";
            } else if (rolledValue > 40) {
                responseMessage += "Below average. That's ok in this case";
            } else if (rolledValue > 20) {
                responseMessage += "Impressively low. Nowhere near that bomb drop";
            } else if (rolledValue >= 2) {
                responseMessage += "You'd be close if the numbers wrapped. But they don't. So you're not close";
            } else {
                responseMessage += "You literally could not have been further from the 1/128 roll. Congratulations.";
            }
            sendChatMessage(responseMessage);
        }
    }

    public void banUser(String user, String reason) throws Exception {
        log.log(Level.INFO, "Banning user {0}: {1}", new Object[]{user, reason});
        sendChatMessage("/ban" + user);
        sendChatMessage("User " + user + " has been banned: " + reason);
    }

    public void banUser(ChatMessage message, String reason) throws Exception {
        log.log(Level.INFO, "Banning user {0} after message {1}: {2}", new Object[]{message.getUser(), message.getMessage(), reason});
        sendChatMessage("/ban " + message.getUser());
        sendChatMessage("User " + message.getUser() + " has been banned: " + reason);
    }

    public Quote getRandomQuote() {
        if (quotes.size() == 0) {
            return null;
        } else {
            return quotes.get(ChatBotUtils.random.nextInt(quotes.size()));
        }
    }

    public ArrayList<ChatMessage> getChatLog() {
        return chatLog;
    }

    public void iteration() {
        if (System.currentTimeMillis() - lastTwitchUserQueryTime >= ChatBotUtils.ONE_MINUTE_IN_MILLIS) {
            //Query Twitch for users in chat and update user list accordingly
            ArrayList<String> usersInChat = TwitchUtils.getUsersInChat(
                    ChatBotUtils.stripHashtagFromChannel(this.channel));
            fullUserDataList.handleCurrentChatters(usersInChat);
            lastTwitchUserQueryTime = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - lastDbWriteTime >= ChatBotUtils.TEN_MINUTES_IN_MILLIS) {
            fullUserDataList.updateAllUsers();
            fileIO.writeUserDataToDatabase(fullUserDataList);
            fileIO.writeQuoteListToFile(quotes);
            lastDbWriteTime = System.currentTimeMillis();
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    public String getChannel() {
        return this.channel;
    }
}
