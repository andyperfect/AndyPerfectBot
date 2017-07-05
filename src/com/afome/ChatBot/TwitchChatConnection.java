package com.afome.ChatBot;

import com.afome.APBotMain;
import com.afome.ChatBot.commands.*;

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
    private long lastPingTime = -1;

    private boolean connected = false;

    private ArrayList<Command> commands;

    public TwitchChatConnection(String channel) throws Exception {
        this.channel = channel;
        config = ConfigHandler.getInstance();
    }

    private void initialize() throws Exception {
        Socket socket = new Socket(
                config.getServerName(),
                config.getPort(this.channel)
        );
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        fileIO = new DataFileIO();
        fileIO.writeChannelToDatabase(channel);
        fullUserDataList = fileIO.createUserDataFromDatabase(channel);
        fullUserDataList.assignModerators(config.getMods(this.channel));
        fullUserDataList.assignOP(config.getOp(this.channel));
        if (config.isQuotesEnabled(this.channel)) {
            quotes = fileIO.getChannelQuotes(this.channel);
        }

        if (chatLog == null) {
            chatLog = new ArrayList<ChatMessage>();
        }
        commands = new ArrayList<Command>();

        if (config.isBotCommandsEnabled(channel)) {
            commands.add(new CommandUptime(this));
            if (config.isUserTrackingCommandsEnabled(channel)) {
                commands.add(new CommandHP(this));
                commands.add(new CommandPP(this));
            }
            if (config.isQuotesEnabled(channel)) {
                commands.add(new CommandQuote(this));
            }
            if (config.isRouletteEnabled(channel)) {
                commands.add(new CommandRoulette(this));
            }
            if (config.isWovCommandEnabled(channel)) {
                commands.add(new CommandWOVRandomizer(this));
            }
        }
    }


    public boolean connect() throws Exception {

        log.info("INITIALIZING channel:" + channel);
        initialize();

        if (connectToServer()) {
            log.info("CONNECTING TO SERVER:" + channel);
            // Join the channel
            writer.write("JOIN " + ChatBotUtils.addHashTagToChannel(channel) + "\r\n");
            writer.flush();
            log.info("JOINED CHANNEL:" + channel);

            initialConnectionTime = System.currentTimeMillis();
            lastTwitchUserQueryTime = System.currentTimeMillis() - ChatBotUtils.ONE_MINUTE_IN_MILLIS;
            lastDbWriteTime = initialConnectionTime;

            //sendChatMessage("Hi there! I'm just here to monitor for the time being. I'll lurk silently. I promise.");
            log.info("successfully connected:" + channel);
            connected = true;
            return true;
        } else {
            log.warning("Failed to connect:" + channel);
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
            log.info("CONNECTING:"+ line);
            if (line.contains("004")) {
                log.info("Logged in");
                return true;
            }
            else if (line.contains("433")) {
                log.warning("Nickname is already in use.");
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
        lastPingTime = System.currentTimeMillis();
        writer.write("PONG " + pingMessage.substring(5) + "\r\n");
        writer.flush();
    }

    public void sendChatMessage(String message) {
        log.info("Sending message in channel " + this.channel + ":" + message);
        try {
            writer.write("PRIVMSG " + ChatBotUtils.addHashTagToChannel(this.channel) + " :" + message + "\r\n");
            writer.flush();
        } catch (Exception e) {
            log.severe(e.getMessage());
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

        for (Command command : commands) {
            if (command.isCommand(message) && command.canUseCommand(userData, this)) {
                userData.handleBotCommand(config.getTimeBetweenUserCommands(channel));
                command.executeCommand(message, userData, this);
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

            DBIterationWriter writer = new DBIterationWriter(this);
            Thread writerThread = new Thread(writer);
            writerThread.start();

            lastDbWriteTime = System.currentTimeMillis();
        }
    }

    public void commandIteration() {
        for (Command command : commands) {
            command.iteration();
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    public String getChannel() {
        return this.channel;
    }

    public DataFileIO getFileIO() {
        return this.fileIO;
    }

    public long getInitialConnectionTime() {
        return this.initialConnectionTime;
    }

    public ArrayList<Quote> getQuotes() {
        return quotes;
    }

    public long getLastPingTime() {
        return lastPingTime;
    }
}
