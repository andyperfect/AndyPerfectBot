package com.afome.ChatBot.commands;

import com.afome.ChatBot.*;

import java.io.IOException;

public class CommandRoulette implements Command {
    private ConfigHandler config;
    public CommandRoulette() throws IOException {
        config = ConfigHandler.getInstance();
    }

    public boolean canUseCommand(UserData user, TwitchChatConnection chatConn) {
        return user.canUseBotCommand(config.getTimeBetweenUserCommands(chatConn.getChannel()));

    }
    public boolean isCommand(ChatMessage message) {
        return message.getMessage().toLowerCase().equals("!roulette");
    }
    public void executeCommand(ChatMessage message, UserData user, TwitchChatConnection chatConn) {
        user.handleBotCommand(config.getTimeBetweenUserCommands(chatConn.getChannel()));
        // Gets a random number from 1 - 128
        int rolledValue = ChatBotUtils.random.nextInt(128) + 1;
        String responseMessage = message.getUser() + " rolled a " + String.valueOf(rolledValue) + ". ";
        if (rolledValue == 128) {

            responseMessage += "A Gutsy Bat. A bomb drop. A timeout. RIP";
            if (user.getUserType().equals(UserType.MODERATOR)) {
                responseMessage += " ... @AndyPerfect, Timeout this guy. This mod's gotta go and I can't do it.";
            } else if (user.getUserType().equals(UserType.USER)) {
                chatConn.sendChatMessage("/timeout " + message.getUser() + " 120");
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
        chatConn.sendChatMessage(responseMessage);
    }
}
