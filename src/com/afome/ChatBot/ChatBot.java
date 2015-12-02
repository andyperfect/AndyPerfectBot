package com.afome.ChatBot;

import com.afome.APBotMain;
import com.afome.ChatBot.Earthbound.EBNamingOption;
import com.afome.ChatBot.Earthbound.EBVoteHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatBot implements Runnable {
    private static final Logger log = Logger.getLogger(APBotMain.class.getName());
    ConfigHandler config;


    BufferedWriter writer = null;
    BufferedReader reader = null;

    UserDataList fullUserDataList;
    ArrayList<Quote> quotes;

    private ArrayList<ChatMessage> chatLog;

    private boolean running = false;
    private boolean EBVotingOpen = false;

    long lastIterationTime = System.currentTimeMillis();
    long startTime = System.currentTimeMillis();

    DataFileIO fileIO;

    public ChatBot() {
        chatLog = new ArrayList<ChatMessage>();
    }

    public void run() {
        try {
            log.log(Level.INFO, "Starting bot");
            config = new ConfigHandler();

            fileIO = new DataFileIO();
            quotes = fileIO.createQuoteListFromFile();
            fullUserDataList = fileIO.createUserDataFromDatabase();
            fullUserDataList.assignModerators(config.getMods());

            Socket socket = new Socket(config.getServerName(), config.getPort());
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            connectToServer();
            joinChannel(config.getChannel());
            running = true;

            log.log(Level.INFO, "Startup completed -- beginning main loop");
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

                if (System.currentTimeMillis() - lastIterationTime >= ChatBotUtils.TEN_MINUTES_IN_MILLIS) {
                    iteration();
                }
                Thread.sleep(100);
            }

            fullUserDataList.updateAllUsers();
            fileIO.writeUserDataToDatabase(fullUserDataList);
            fileIO.writeQuoteListToFile(quotes);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        log.log(Level.INFO, "Terminating bot");
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

    public void iteration() {
        //Query Twitch for users in chat and update user list accordingly
        ArrayList<String> usersInChat = TwitchUtils.getUsersInChat(config.getChannel());
        fullUserDataList.handleCurrentChatters(usersInChat);

        fullUserDataList.updateAllUsers();
        fileIO.writeUserDataToDatabase(fullUserDataList);
        fileIO.writeQuoteListToFile(quotes);
        lastIterationTime = System.currentTimeMillis();
    }

    //TODO Break out the individual commands into something a bit more dynamic (possibly their own methods?)
    public void handleChatMessage(ChatMessage message) throws Exception {
        UserData userData = fullUserDataList.findUser(message.getUser());
        if (userData == null) {
            userData = fullUserDataList.createNewUser(message.getUser());
        }
        userData.handleChatMessage();

        //If bot ban is enabled, the user has sent only 1 message and it's a link, insta-ban
        if (config.isBotBanEnabled() && userData.getChatCount() <= 1 && ChatBotUtils.containsLink(message.getMessage())) {
            banUser(message, "First chat message contained a link. Assumed bot");
        }

        //OP COMMANDS
        if (userData.getUser().equalsIgnoreCase(config.getOp())) {
            if (message.getMessage().equals("!stopbot")) {
                stopBot();
            }
        }

        //USER COMMANDS
        if (message.getMessage().startsWith("!hp")) {
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

        if (message.getMessage().startsWith("!pp")) {
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
        if (message.getMessage().startsWith("!quote") && (userData.getNumMillis() > config.getTimeNeededToQuote() || userData.getUserType() == UserType.MODERATOR)) {
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
            sendChatMessage("The stream has been up for " + ChatBotUtils.millisToReadableFormat(curTime - startTime));
        }

        if (message.getMessage().startsWith("!ebvote") && EBVotingOpen) {
            String[] splitLine = message.getMessage().split("\\s+");

            //No extra parameters, ignore
            if (splitLine.length == 1) {
                return;
            }

            ArrayList<String> characters = new ArrayList<String>();
            for (int i = 1; i < splitLine.length; i++) {
                characters.add(splitLine[i].toUpperCase());
            }
            EBVoteHandler.addVote(new EBNamingOption(message.getUser(), characters));
        }
    }

    public void handlePrivateMessage(ChatMessage message) {
        return;
    }

    public void sendChatMessage(String message) {
        System.out.println("LOG: Sending a chat message: " + message);
        try {
            writer.write("PRIVMSG " + config.getChannel() + " :" + message + "\r\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            return quotes.get(ChatBotUtils.random.nextInt(quotes.size()));
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

    public void stopBot() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public ArrayList<ChatMessage> getChatLog() {
        return chatLog;
    }



    public boolean isEBVotingOpen() {
        return EBVotingOpen;
    }

    public void setEBVotingOpen(boolean EBVotingOpen) {
        if (EBVotingOpen) {
            EBVoteHandler.clearVotes();
        }
        this.EBVotingOpen = EBVotingOpen;
    }

    public EBNamingOption pickEBVotingWinner() {
        return EBVoteHandler.pickWinningUser();
    }

    public void acceptEBVotingWinner()  {
        EBNamingOption acceptedWinner = EBVoteHandler.getCurWinningEBNamingOption();
        if (acceptedWinner == null) {
            return;
        } else {
            sendChatMessage("Winner! " + acceptedWinner.getUser() + " with naming option: " + String.join(", ", acceptedWinner.getCharacters()));
        }
    }
}
