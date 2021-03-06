package com.afome.ChatBot;

public class ChatMessage {

    private String user = "";
    private String url = "";
    private ChatMessageType messageType = null;
    private String channel = "";
    private String message = "";
    private long messageMillis = -1;
    private String[] userList = null;
    private boolean existsInDatabase = false;


    public ChatMessage(String line) {
        messageMillis = System.currentTimeMillis();
        parseMessage(line);

    }

    public void parseMessage(String line) {
        if (line.startsWith("PING")) {
            parsePingMessage(line);
        } else if (line.contains("PRIVMSG")) {
            parseChatMessage(line);
        } else if (line.contains("JOIN")) {
            parseJoinMessage(line);
        } else if (line.contains("PART")) {
            parsePartMessage(line);
        } else if (line.contains("353")) {
            parseUserListMessage(line);
        } else if (line.contains("366")) {
            parseUserListMessageEnd(line);
        }
    }

    private void parseUserListMessage(String line) {
        messageType = ChatMessageType.USERLIST;
        int userIndex = line.indexOf(':', 1) +1;
        String userListString = line.substring(userIndex);
        userList = userListString.split("\\s+");



    }

    private void parseUserListMessageEnd(String line) {
        messageType = ChatMessageType.USERLISTEND;
    }

    private void parsePingMessage(String line) {
        messageType = ChatMessageType.PING;
    }

    private void parseChatMessage(String line) {
        messageType = ChatMessageType.CHAT;
        user = parseUser(line);
        int channelIndex = line.indexOf("PRIVMSG") + 8;
        int channelEndIndex = line.indexOf(' ', channelIndex);
        channel = ChatBotUtils.stripHashtagFromChannel(line.substring(channelIndex, channelEndIndex));
        int messageIndex = line.indexOf(':', channelIndex) + 1;
        message = line.substring(messageIndex);
    }

    private void parseJoinMessage(String line) {
        messageType = ChatMessageType.JOIN;
        user = parseUser(line);
    }

    private void parsePartMessage(String line) {
        messageType = ChatMessageType.PART;
        user = parseUser(line);

    }

    private String parseUser(String line) {
        int lastUserCharacterIndex = line.indexOf('!');
        return line.substring(1, lastUserCharacterIndex).toLowerCase();
    }



    public String getUser() {
        return user;
    }

    public String getUrl() {
        return url;
    }

    public ChatMessageType getMessageType() {
        return messageType;
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public long getMessageMillis() {
        return messageMillis;
    }

    public String[] getUserList() {
        return userList;
    }

    public boolean doesExistInDatabase() {
        return existsInDatabase;
    }

    public void setExistsInDatabase(boolean existsInDatabase) {
        this.existsInDatabase = existsInDatabase;
    }

    public String toString() {
        return user + ": " + message;
    }
}
