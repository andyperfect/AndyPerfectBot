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
    UserDataList queueUserDataList;

    boolean running = true;
    boolean queueOpen = false;

    long lastFileWrite = System.currentTimeMillis();

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
            if (message.getMessage().equals("!openqueue") && !queueOpen) {
                queueUserDataList = new UserDataList();
                queueOpen = true;
            }
            if (message.getMessage().equals("!dequeue") && queueOpen) {
                if (queueUserDataList.size() > 0) {
                    UserData userDataQueue = queueUserDataList.remove(0);
                    if (userDataQueue != null) {

                    }
                }
            }
            if (message.getMessage().equals("!closequeue") && queueOpen) {
                queueUserDataList = new UserDataList();
                queueOpen = false;
            }
            if (message.getMessage().startsWith("!removefromqueue") && queueOpen) {
                String userToRemove = message.getMessage().substring(22);
                UserData userDataToRemove = queueUserDataList.findUser(userToRemove);
                if (userDataToRemove != null) {
                    queueUserDataList.remove(userDataToRemove);
                    sendChatMessage("Removed " + userToRemove + " from queue");
                } else {
                    sendChatMessage("User " + userToRemove + " is not in the queue");
                }
            }
            if (message.getMessage().equals("!showqueue") && queueOpen) {
                int orderNumber = 1;
                StringBuilder stringBuilder = new StringBuilder();
                for (UserData userDataQueue : queueUserDataList) {
                    stringBuilder.append(String.valueOf(orderNumber + ". " + userDataQueue.getUser() + " " ));
                    orderNumber++;
                }
                if (orderNumber == 1) {//Nobody in queue
                    sendChatMessage("The queue is empty");
                }
                sendChatMessage(stringBuilder.toString());
            }
        }

        if (message.getMessage().equals("!pp")) {
            if (userData == null) {
                sendChatMessage("User '" + message.getUser() + "' not yet in system");
            } else {
                sendChatMessage("User '" + message.getUser() + "' has been active for " + userData.getConnectionTimeInMinutes() + " minutes");
            }
        }

        if (message.getMessage().equals("!chat")) {
            if (userData == null) {
                sendChatMessage("User '" + message.getUser() + "' not yet in system");
            } else {
                sendChatMessage("User '" + message.getUser() + "' has sent " + userData.getChatCount() + " chat messages");
            }
        }
        if (message.getMessage().equals("!enterqueue") && queueOpen) {
            boolean isInQueue = queueUserDataList.findUser(userData.getUser()) != null;
            if (!isInQueue) {
                queueUserDataList.add(userData);
                sendChatMessage("Added user " + userData.getUser() + " to queue");
            }
        }
        if (message.getMessage().equals("!leavequeue") && queueOpen) {
            boolean isInQueue = queueUserDataList.findUser(userData.getUser()) != null;
            if (isInQueue) {
                queueUserDataList.remove(userData);
                sendChatMessage("Removed user " + userData.getUser() + " from queue");
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
}
