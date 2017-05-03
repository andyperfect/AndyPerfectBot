package com.afome.ChatBot.commands;

import com.afome.ChatBot.*;

import java.io.IOException;

public class CommandQuote implements Command {
    private ConfigHandler config;
    private TwitchChatConnection chatConn;
    public CommandQuote(TwitchChatConnection chatConn) throws IOException {
        this.chatConn = chatConn;
        config = ConfigHandler.getInstance();
    }

    public boolean canUseCommand(UserData user, TwitchChatConnection chatConn) {
        boolean withinChannelTimeout = user.canUseBotCommand(config.getTimeBetweenUserCommands(chatConn.getChannel()));
        boolean withinQuoteTimeRequired = user.getNumMillis() > config.getTimeNeededToQuote(chatConn.getChannel());
        boolean isModerator = user.getUserType() == UserType.MODERATOR;

        return (withinChannelTimeout && (withinQuoteTimeRequired || isModerator));
    }
    public boolean isCommand(ChatMessage message) {
        return message.getMessage().startsWith("!quote");
    }
    public void executeCommand(ChatMessage message, UserData user, TwitchChatConnection chatConn) {
        user.handleBotCommand(config.getTimeBetweenUserCommands(chatConn.getChannel()));
        String[] splitLine = message.getMessage().split("\\s+");
        if (splitLine.length == 1) {
            Quote quote = getRandomQuote(chatConn);
            if (quote == null) {
                chatConn.sendChatMessage("There are no quotes available");
            } else {
                chatConn.sendChatMessage("\"" + quote.getQuote() + "\" (" +
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
                chatConn.getQuotes().add(new Quote(quoteString, chatConn.getChannel(),
                        user.getUser().toLowerCase(), false));
                chatConn.sendChatMessage("Added quote \"" + quoteString + "\"");
            } else {
                chatConn.sendChatMessage("Invalid quote length");
            }
        }
    }

    public Quote getRandomQuote(TwitchChatConnection chatConn) {
        if (chatConn.getQuotes().size() == 0) {
            return null;
        } else {
            return chatConn.getQuotes().get(ChatBotUtils.random.nextInt(chatConn.getQuotes().size()));
        }
    }

    public void iteration() {
        chatConn.getFileIO().writeQuotesToDatabase(chatConn.getQuotes());
    }
}
