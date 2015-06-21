package com.afome.ChatBot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ChatBot implements Runnable {
    ConfigHandler config;
    final long TEN_MINUTES_IN_MILLIS = 600000;

    BufferedWriter writer = null;
    BufferedReader reader = null;

    UserDataList fullUserDataList;
    ArrayList<String> moderators;
    ArrayList<Quote> quotes;

    private ArrayList<ChatMessage> chatLog;

    boolean running = false;

    long lastFileWrite = System.currentTimeMillis();
    long startTime = System.currentTimeMillis();

    public ChatBot() {
        chatLog = new ArrayList<ChatMessage>();
    }

    public void run() {
        System.out.println("Bot starting");
        try {
            config = new ConfigHandler();

            DataFileIO fileIO = new DataFileIO();
            quotes = fileIO.createQuoteListFromFile();
            fullUserDataList = fileIO.createUserDataFromFile();

            Socket socket = new Socket(config.getServerName(), config.getPort());
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            connectToServer();
            joinChannel(config.getChannel());
            running = true;
            sendChatMessage("/mods");


            // MAIN LOOP
            String line;
            while (running) {
                while (reader.ready()) {
                    line = reader.readLine();
                    System.out.println("NEW LINE");
                    System.out.println(line);
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

                if (System.currentTimeMillis() - lastFileWrite >= TEN_MINUTES_IN_MILLIS) {
                    fullUserDataList.updateAllUsers();
                    fileIO.writeUserDataToFile(fullUserDataList);
                    fileIO.writeQuoteListToFile(quotes);
                    lastFileWrite = System.currentTimeMillis();
                }
                Thread.sleep(100);
            }

            fullUserDataList.updateAllUsers();
            fileIO.writeUserDataToFile(fullUserDataList);
            fileIO.writeQuoteListToFile(quotes);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("Bot terminating");
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

    //TODO Break out the individual commands into something a bit more dynamic (possibly their own methods?)
    public void handleChatMessage(ChatMessage message) throws Exception {
        UserData userData = fullUserDataList.findUser(message.getUser());
        if (userData == null) {
            userData = fullUserDataList.createNewUser(message.getUser());
        }
        userData.handleChatMessage();

        //MOD COMMANDS
        if (userData.getUser().equalsIgnoreCase(config.getOp())) {
            if (message.getMessage().equals("!stopbot")) {
                stopBot();
            }
        }

        if (message.getMessage().startsWith("!pp")) {
            String[] splitLine = message.getMessage().split("\\s+");
            if (splitLine.length == 1) {
                if (userData == null) {
                    sendChatMessage(message.getUser() + " has never been in this channel");
                } else {
                    sendChatMessage(message.getUser() + " has been active for " + millisToReadableFormat(userData.getNumMillis()));
                }
            } else if (splitLine.length == 2) {
                UserData otherUserData = fullUserDataList.findUser(splitLine[1].toLowerCase());
                if (otherUserData == null) {
                    sendChatMessage(splitLine[1] + " has never been in this channel");
                } else {
                    if (splitLine[1].equalsIgnoreCase(config.getNick())) {
                        sendChatMessage("You don't need to know about me");
                    } else {
                        sendChatMessage(splitLine[1] + " has been active for " + millisToReadableFormat(otherUserData.getNumMillis()));
                    }
                }
            }
        }

        if (message.getMessage().startsWith("!chat")) {
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

        if (message.getMessage().startsWith("!quote")) {
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
            long curTime = System.currentTimeMillis();
            sendChatMessage("The stream has been up for " + millisToReadableFormat(curTime - startTime));
        }
    }

    public void handlePrivateMessage(ChatMessage message) {
        if (message.getUser().equalsIgnoreCase(("jtv"))) {
            if (message.getMessage().startsWith("The moderators of this room are:")) {
                String moderatorList = message.getMessage().substring(message.getMessage().indexOf(":") + 1);
                moderators = new ArrayList<String>(Arrays.asList(moderatorList.split(", ")));
            }
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

    public Quote getRandomQuote() {
        if (quotes.size() == 0) {
            return null;
        } else {
            Random rand = new Random();
            return quotes.get(rand.nextInt(quotes.size()));
        }

    }

    public void stopBot() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public String millisToReadableFormat(long millis) {
        /*86400000 millis in a day
         *3600000 millis in an hour
         * 60000 millis in a minute
        */

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

    public ArrayList<ChatMessage> getChatLog() {
        return chatLog;
    }
}
