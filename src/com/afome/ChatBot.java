package com.afome;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;

public class ChatBot {
    ConfigHandler config;
    final long TEN_MINUTES_IN_MILLIS = 600000;

    BufferedWriter writer = null;
    BufferedReader reader = null;

    UserDataList fullUserDataList;

    boolean running = true;

    long lastFileWrite = System.currentTimeMillis();
    long startTime = System.currentTimeMillis();

    public ChatBot() {

    }

    public void start() throws Exception {
        config = new ConfigHandler();

        DataFileIO fileIO = new DataFileIO();
        fullUserDataList = fileIO.createDataFromFile();

        Socket socket = new Socket(config.getServerName(), config.getPort());
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        connectToServer();
        joinChannel(config.getChannel());


        // MAIN LOOP
        String line;
        while (running && (line = reader.readLine( )) != null) {
            System.out.println("NEW LINE");
            System.out.println(line);
            ChatMessage message = new ChatMessage(line);
            if (message.getMessageType() == ChatMessageType.PING) {
                replyToPing(line);
            } else if (message.getMessageType() == ChatMessageType.USERLIST) {
                handleJoinMessage(message);
            } else if (message.getMessageType() == ChatMessageType.USERLISTEND) {

            } else if (message.getMessageType() == ChatMessageType.JOIN) {
                handleJoinMessage(message);
            } else if (message.getMessageType() == ChatMessageType.PART) {
                handlePartMessage(message);
            } else if (message.getMessageType() == ChatMessageType.CHAT) {
                handleChatMessage(message);
            }

            if (System.currentTimeMillis() - lastFileWrite >= TEN_MINUTES_IN_MILLIS) {
                fullUserDataList.updateAllUsers();
                fileIO.writeDataToFile(fullUserDataList);
                lastFileWrite = System.currentTimeMillis();
            }
        }
        fullUserDataList.updateAllUsers();
        fileIO.writeDataToFile(fullUserDataList);
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
        userData.parted();
    }

    public void handleChatMessage(ChatMessage message) throws Exception {
        UserData userData = fullUserDataList.findUser(message.getUser());
        if (userData == null) {
            userData = fullUserDataList.createNewUser(message.getUser());
        }
        userData.handleChatMessage();

        //MOD COMMANDS
        if (userData.getUser().equalsIgnoreCase(config.getOp())) {
            if (message.getMessage().equals("!stopbot")) {
                running = false;
            }
        }

        if (message.getMessage().equals("!pp")) {
            if (userData == null) {
                sendChatMessage("User '" + message.getUser() + "' not yet in system");
            } else {
                sendChatMessage("User '" + message.getUser() + "' has been active for " + millisToReadableFormat(userData.getNumMillis()));
            }
        }

        if (message.getMessage().equals("!chat")) {
            if (userData == null) {
                sendChatMessage("User '" + message.getUser() + "' not yet in system");
            } else {
                sendChatMessage("User '" + message.getUser() + "' has sent " + userData.getChatCount() + " chat messages");
            }
        }
        if (message.getMessage().equals("!uptime")) {
            long curTime = System.currentTimeMillis();
            sendChatMessage("The stream has been up for " + millisToReadableFormat(curTime - startTime));
        }
    }

    public void sendChatMessage(String message) throws Exception {
        System.out.println("LOG: Sending a chat message");
        writer.write("PRIVMSG " + config.getChannel() + " :" + message + "\r\n");
        writer.flush();
    }

    public void replyToPing(String pingMessage) throws Exception {
        writer.write("PONG " + pingMessage.substring(5) + "\r\n");
        writer.flush();
    }

    public void joinChannel(String channel) throws Exception {
        writer.write("JOIN " + channel + "\r\n");
        writer.flush();
        System.out.println("LOG: JOINED CHANNEL " + channel);
    }

    public boolean connectToServer() throws IOException {
        writer.write("PASS " + config.getPassword() + "\r\n");
        writer.write("NICK " + config.getNick() + "\r\n");
        writer.flush();

        // Read lines from the server until it tells us we have connected.
        String line = null;
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

    public String millisToReadableFormat(long millis) {
        /*86400000 millis in a day
         *3600000 millis in an hour
         * 60000 millis in a minute
        */
        String timeString = "";
        StringBuilder timeStringBuilder = new StringBuilder();
        long delta = millis;

        long days = (long) Math.floor(delta / 86400000.0);
        delta -= days * 86400000;

        long hours = (long) Math.floor(delta / 3600000.0);
        delta -= hours * 3600000;

        long minutes = (long) Math.floor(delta / 60000.0);
        delta -= minutes * 60000;

        if (days > 0) {
            timeStringBuilder.append(String.valueOf(days));
            String toAppend = (days == 1) ? " day" : " days";
            timeStringBuilder.append(toAppend);
            if (hours > 0 && minutes > 0) {
                timeStringBuilder.append(", ");
            } else if (hours > 0 || minutes > 0) {
                timeStringBuilder.append(" and ");
            }
        }

        if (hours > 0) {
            timeStringBuilder.append(String.valueOf(hours));
            String toAppend = (hours == 1) ? " hour" : " hours";
            timeStringBuilder.append(toAppend);
            if (minutes > 0) {
                timeStringBuilder.append(" and ");
            }
        }

        if (minutes > 0) {
            timeStringBuilder.append(String.valueOf(minutes));
            String toAppend = (minutes == 1) ? " minute" : " minutes";
            timeStringBuilder.append(toAppend);
        }

        if (days == 0 && hours == 0 && minutes == 0) {
            timeStringBuilder.append("less than one minute");
        }

        return timeStringBuilder.toString();
    }
}
