package com.afome.ChatBot.commands;

import com.afome.ChatBot.*;

import java.io.IOException;

public class CommandHP implements Command {
    private ConfigHandler config;
    public CommandHP() throws IOException {
        config = ConfigHandler.getInstance();
    }

    public boolean canUseCommand(UserData user, TwitchChatConnection chatConn) {
        return user.canUseBotCommand(config.getTimeBetweenUserCommands(chatConn.getChannel()));
    }
    public boolean isCommand(ChatMessage message) {
        return message.getMessage().toLowerCase().startsWith("!hp");
    }
    public void executeCommand(ChatMessage message, UserData user, TwitchChatConnection chatConn) {
        if (message == null || chatConn == null) {
            return;
        }
        String channel = chatConn.getChannel();
        DataFileIO fileIO = chatConn.getFileIO();
        String[] splitLine = message.getMessage().split("\\s+");

        if (splitLine.length == 1) {
            // User is requesting data for themselves
            Object[] userRank = fileIO.getUserRank(channel, user.getUser(), "time");
            if (userRank[0] != null) {
                chatConn.sendChatMessage(message.getUser() + " is ranked " + userRank[1] + " with " +
                        ChatBotUtils.millisToReadableFormat(((UserData) userRank[0]).getNumMillis()) + " in chat");
            }
        } else if (splitLine.length == 2) {
            if (splitLine[1].startsWith("#")) {
                // User is requesting data for a user at a given rank
                try {
                    String number = splitLine[1].substring(1, splitLine[1].length());
                    int rankToFind = Integer.parseInt(number);
                    UserData userAtRank = fileIO.getUserAtTimeRank(channel, rankToFind);
                    if (userAtRank != null) {
                        chatConn.sendChatMessage("Rank " + String.valueOf(rankToFind) + ": " + userAtRank.getUser() + " has " +
                                ChatBotUtils.millisToReadableFormat(userAtRank.getNumMillis()) + " in chat");
                    }
                } catch (Exception e) {
                    // Ignore
                }
            } else {
                // User is requesting data for another user
                if (splitLine[1].equalsIgnoreCase(config.getNick())) {
                    chatConn.sendChatMessage("I have no data");
                } else {
                    Object[] userRank = fileIO.getUserRank(channel, splitLine[1].toLowerCase(), "time");
                    if (userRank[0] != null) {
                        chatConn.sendChatMessage(((UserData)userRank[0]).getUser() + " is ranked " + userRank[1] + " with " +
                                ChatBotUtils.millisToReadableFormat(((UserData) userRank[0]).getNumMillis()) + " in chat");
                    }
                }
            }
        }
    }
}
