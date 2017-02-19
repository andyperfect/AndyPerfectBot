package com.afome.ChatBot.commands;

import com.afome.ChatBot.*;

import java.io.IOException;

public class CommandUptime implements Command {
    private ConfigHandler config;
    public CommandUptime() throws IOException {
        config = ConfigHandler.getInstance();
    }

    public boolean canUseCommand(UserData user, TwitchChatConnection chatConn) {
        return user.canUseBotCommand(config.getTimeBetweenUserCommands(chatConn.getChannel()));
    }
    public boolean isCommand(ChatMessage message) {
        return message.getMessage().toLowerCase().equals("!uptime");
    }
    public void executeCommand(ChatMessage message, UserData user, TwitchChatConnection chatConn) {
        user.handleBotCommand(config.getTimeBetweenUserCommands(chatConn.getChannel()));
        long curTime = System.currentTimeMillis();
        chatConn.sendChatMessage("The stream has been up for " +
                ChatBotUtils.millisToReadableFormat(curTime - chatConn.getInitialConnectionTime()));
    }
}
