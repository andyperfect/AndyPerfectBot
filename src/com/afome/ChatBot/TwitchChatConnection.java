package com.afome.ChatBot;

import com.afome.APBotMain;

import java.io.*;
import java.net.Socket;
import java.util.*;
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
        Socket socket = new Socket(
                config.getServerName(),
                config.getPort(this.channel)
        );
        System.out.println("1");
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        fileIO = new DataFileIO();
        fileIO.writeChannelToDatabase(channel);
        fullUserDataList = fileIO.createUserDataFromDatabase(channel);
        fullUserDataList.assignModerators(config.getMods(this.channel));
        fullUserDataList.assignOP(config.getOp(this.channel));
        System.out.println("2");
        if (config.isQuotesEnabled(this.channel)) {
            quotes = fileIO.getChannelQuotes(this.channel);
        }

        if (chatLog == null) {
            chatLog = new ArrayList<ChatMessage>();
        }
    }


    public boolean connect() throws Exception {

        System.out.println("INITIALIZING");
        initialize();

        if (connectToServer()) {
            System.out.println("CONNECTING TO SERVER");
            // Join the channel
            writer.write("JOIN " + ChatBotUtils.addHashTagToChannel(channel) + "\r\n");
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
        fileIO.writeChatMessagesToDatabase(this.chatLog);
        if (config.isQuotesEnabled(this.channel)) {
            fileIO.writeQuotesToDatabase(quotes);
        }

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
            writer.write("PRIVMSG " + ChatBotUtils.addHashTagToChannel(this.channel) + " :" + message + "\r\n");
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

    }

    //TODO Break out the individual commands into something a bit more dynamic (possibly their own methods?)
    public void handleChatMessage(ChatMessage message) throws Exception {
        UserData userData = fullUserDataList.findUser(message.getUser());
        if (userData == null) {
            userData = fullUserDataList.createNewUser(message.getUser());
        }
        userData.handleChatMessage();

        //If bot ban is enabled, the user has sent only 1 message and it's a link, instant ban
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
        if (message.getMessage().startsWith("!hp")) {
            userData.handleBotCommand(config.getTimeBetweenUserCommands(this.channel));
            handleHPCommand(message, userData);
        }

        if (message.getMessage().startsWith("!pp")) {
            userData.handleBotCommand(config.getTimeBetweenUserCommands(this.channel));
            handlePPCommand(message, userData);
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
                    sendChatMessage("\"" + quote.getQuote() + "\" (" +
                            ChatBotUtils.epochToDateString(quote.getTimeInMillis()) + ")");
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
                    quotes.add(new Quote(quoteString, this.channel,
                                    userData.getUser().toLowerCase(), false));
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

        if (message.getMessage().equals("!wov-randomizer") && userData.getUserType() == UserType.OPERATOR) {
            handleWOVRandomizerCommand();
        }
    }

    public void handleWOVRandomizerCommand() {
        String outputMessage = "";
        HashMap<String, Double> gameModes = new HashMap<String, Double>();
        gameModes.put("Mortal", 0.3);
        gameModes.put("Demon", 0.3);
        gameModes.put("Doomed Mortal", 0.2);
        gameModes.put("Doomed Demon", 0.2);

        HashMap<String, Double> areas = new HashMap<String, Double>();
        areas.put("Floating Keep 1", 1.0);
        areas.put("Myougi 1", 1.0);
        areas.put("Floating Keep 2", 1.0);
        areas.put("Prgora", 1.0);
        areas.put("Vale 1", 1.0);
        areas.put("Electram", 1.0);
        areas.put("Vale 2", 1.0);
        areas.put("Amythyst", 1.0);
        areas.put("Grotto 1", 1.0);
        areas.put("Dark Annihlator", 1.0);
        areas.put("Grotto 2", 1.0);
        areas.put("Terravine", 1.0);
        areas.put("Underworld 1", 1.0);
        areas.put("Kraterac", 1.0);
        areas.put("Underworld 2", 1.0);
        areas.put("Ancient Constructs", 1.0);
        areas.put("Library", 1.0);
        areas.put("Myougi 2", 1.0);
        areas.put("Chambers", 1.0);
        areas.put("Twin Orcs", 1.0);
        areas.put("Path of Decay", 1.0);
        areas.put("Azurel", 1.0);
        areas.put("Heart of the Baneful", 1.0);
        areas.put("Jehoul 1", 1.0);
        areas.put("Jehoul 2", 1.0);
        areas.put("Boss Rush", 1.0);

        for (String key : areas.keySet()) {
            areas.put(key, 1.0/areas.size());
        }

        HashMap<String, Double> stipulations = new HashMap<String, Double>();
        stipulations.put("1 Death", 1.0);
        stipulations.put("3 Deaths", 1.0);
        stipulations.put("5 Deaths", 1.0);
        stipulations.put("10 Deaths", 1.0);
        stipulations.put("15 Deaths", 1.0);
        stipulations.put("3 Minutes", 1.0);
        stipulations.put("5 Minutes", 1.0);
        stipulations.put("7 Minutes", 1.0);
        stipulations.put("10 Minutes", 1.0);
        stipulations.put("15 Minutes", 1.0);

        for (String key : stipulations.keySet()) {
            stipulations.put(key, 1.0 / stipulations.size());
        }

        HashMap<String, Double> weapons = new HashMap<String, Double>();
        weapons.put("Purified", 1.0);
        weapons.put("Turbo", 1.0);
        weapons.put("Gluttony", 1.0);
        weapons.put("Wrath", 1.0);
        weapons.put("Lust", 1.0);
        weapons.put("Pride", 1.0);
        weapons.put("Envy", 1.0);
        weapons.put("Greed", 1.0);
        weapons.put("Sloth", 1.0);
        weapons.put("PB", 1.0);

        for (String key : weapons.keySet()) {
            weapons.put(key, 1.0 / weapons.size());
        }

        HashMap<String, Double> hellspawn = new HashMap<String, Double>();
        hellspawn.put("enabled", 0.01);
        hellspawn.put("disabled", 0.99);

        HashMap<String, Double> sevenSins = new HashMap<String, Double>();
        sevenSins.put("enabled", 0.01);
        sevenSins.put("disabled", 0.99);

        if (ChatBotUtils.rollValue(sevenSins).equals("enabled")) {
            String difficulty = ChatBotUtils.rollValue(gameModes);
            outputMessage = "Seven Sins | " + difficulty;
        } else if (ChatBotUtils.rollValue(hellspawn).equals("enabled")) {
            String difficulty = ChatBotUtils.rollValue(gameModes);
            outputMessage = "Hellspawn | " + difficulty;
        } else {
            String gameMode = ChatBotUtils.rollValue(gameModes);
            String area = ChatBotUtils.rollValue(areas);
            String weapon = ChatBotUtils.rollValue(weapons);
            String stipulation = ChatBotUtils.rollValue(stipulations);
            outputMessage = gameMode + " | " + area + " | " + weapon + " | " + stipulation;
        }
        sendChatMessage(outputMessage);
    }

    public void handleHPCommand(ChatMessage message, UserData userData) {
        if (!config.isUserTrackingCommandsEnabled(this.channel)) {
            return;
        }
        if (message == null || userData == null) {
            return;
        }
        String[] splitLine = message.getMessage().split("\\s+");

        if (!splitLine[0].equals("!hp")) {
            return;
        }
        System.out.println("hp request valid");
        if (splitLine.length == 1) {
            // User is requesting data for themselves
            Object[] userRank = fileIO.getUserRank(this.channel, userData.getUser(), "time");
            if (userRank[0] != null) {
                sendChatMessage(message.getUser() + " is ranked " + userRank[1] + " with " +
                        ChatBotUtils.millisToReadableFormat(((UserData)userRank[0]).getNumMillis()) + " in chat");
            }
        } else if (splitLine.length == 2) {
            if (splitLine[1].startsWith("#")) {
                // User is requesting data for a user at a given rank
                try {
                    String number = splitLine[1].substring(1, splitLine[1].length());
                    int rankToFind = Integer.parseInt(number);
                    UserData userAtRank = fileIO.getUserAtTimeRank(this.channel, rankToFind);
                    if (userAtRank != null) {
                        sendChatMessage("Rank " + String.valueOf(rankToFind) + ": " + userAtRank.getUser() + " has " +
                                ChatBotUtils.millisToReadableFormat(userAtRank.getNumMillis()) + " in chat");
                    }
                } catch (Exception e) {
                    // Ignore
                }
            } else {
                // USer is requesting data for another user
                if (splitLine[1].toLowerCase().equals(config.getNick())) {
                    sendChatMessage("I have no data");
                } else {
                    Object[] userRank = fileIO.getUserRank(this.channel, splitLine[1].toLowerCase(), "time");
                    if (userRank[0] != null) {
                        sendChatMessage(((UserData)userRank[0]).getUser() + " is ranked " + userRank[1] + " with " +
                                ChatBotUtils.millisToReadableFormat(((UserData) userRank[0]).getNumMillis()) + " in chat");
                    }
                }
            }
        }
    }

    public void handlePPCommand(ChatMessage message, UserData userData) {
        if (!config.isUserTrackingCommandsEnabled(this.channel)) {
            return;
        }
        if (message == null || userData == null) {
            return;
        }
        String[] splitLine = message.getMessage().split("\\s+");

        if (!splitLine[0].equals("!pp")) {
            return;
        }
        if (splitLine.length == 1) {
            // User is requesting data for themselves
            Object[] userRank = fileIO.getUserRank(this.channel, userData.getUser(), "chat");
            if (userRank[0] != null) {
                sendChatMessage(message.getUser() + " is ranked " + userRank[1] + " with " +
                        String.valueOf(((UserData) userRank[0]).getChatCount()) + " messages in chat");
            }
        } else if (splitLine.length == 2) {
            if (splitLine[1].startsWith("#")) {
                // User is requesting data for a user at a given rank
                try {
                    String number = splitLine[1].substring(1, splitLine[1].length());
                    int rankToFind = Integer.parseInt(number);
                    UserData userAtRank = fileIO.getUserAtChatRank(this.channel, rankToFind);
                    if (userAtRank != null) {
                        sendChatMessage("Rank " + String.valueOf(rankToFind) + ": " + userAtRank.getUser() + " has " +
                                userAtRank.getChatCount() + " messages in chat");
                    }
                } catch (Exception e) {
                    //Ignore
                }
            } else {
                // USer is requesting data for another user
                if (splitLine[1].toLowerCase().equals(config.getNick())) {
                    sendChatMessage("I have no data");
                } else {
                    Object[] userRank = fileIO.getUserRank(this.channel, splitLine[1].toLowerCase(), "chat");
                    if (userRank[0] != null) {
                        sendChatMessage(((UserData)userRank[0]).getUser() + " is ranked " + userRank[1] + " with " +
                                String.valueOf(((UserData) userRank[0]).getChatCount()) + " messages in chat");
                    }
                }
            }
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
        if (System.currentTimeMillis() - lastTwitchUserQueryTime >= ChatBotUtils.FIVE_MINUTES_IN_MILLIS) {
            //Query Twitch for users in chat and update user list accordingly
            ArrayList<String> usersInChat = TwitchUtils.getUsersInChat(this.channel);
            if (usersInChat != null) {
                fullUserDataList.handleCurrentChatters(usersInChat);
            }
            lastTwitchUserQueryTime = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - lastDbWriteTime >= ChatBotUtils.TEN_MINUTES_IN_MILLIS) {
            fullUserDataList.updateAllUsers();
            fileIO.writeUserDataToDatabase(fullUserDataList);
            fileIO.writeChatMessagesToDatabase(this.chatLog);
            if (config.isQuotesEnabled(this.channel)) {
                fileIO.writeQuotesToDatabase(quotes);
            }
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
