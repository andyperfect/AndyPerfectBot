package com.afome.ChatBot;

import com.afome.APBotMain;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataFileIO {
    private static final Logger log = Logger.getLogger(APBotMain.class.getName());
    private String userDatafilePath = "data" + File.separator + "userdata.txt";
    private String quotesFilePath = "data" + File.separator + "quotedata.txt";

    private String dateTimeFormatString = "yyyy-MM-dd";
    DateTimeFormatter quoteDateFormatter = null;
    ConfigHandler config;

    DBConnection db = DBConnection.getInstance();

    public DataFileIO() throws IOException {
        config = ConfigHandler.getInstance();
        quoteDateFormatter = DateTimeFormatter.ofPattern(dateTimeFormatString);
    }

    public UserDataList createUserDataFromDatabase(String channel) {
        return db.getAllUserInfo(channel);
    }

    public void writeUserDataToDatabase(UserDataList dataList) {
        db.batchUserUpdate(dataList);
    }

    public void writeChannelToDatabase(String channel) {
        db.createChannel(channel);
    }

    public UserData getUserAtTimeRank(String channel, int rank) {
        if (rank < 1) {
            return null;
        }
        UserDataList users = db.getAllUserInfoRankedByTime(channel, config.getNick());
        if (rank > users.size()) {
            return null;
        }
        return users.get(rank - 1);
    }

    public UserData getUserAtChatRank(String channel, int rank) {
        if (rank < 1) {
            return null;
        }
        UserDataList users = db.getAllUserInfoRankedByChatCount(channel, config.getNick());
        if (rank > users.size()) {
            return null;
        }
        return users.get(rank - 1);
    }

    public Object[] getUserRank(String channel, String username, String rankType) {
        Object[] returnArray = new Object[2];
        UserDataList users;
        if (rankType.equals("time")) {
            users = db.getAllUserInfoRankedByTime(channel, config.getNick());
        } else {
            users = db.getAllUserInfoRankedByChatCount(channel, config.getNick());
        }
        int rank = 1;
        for (UserData user : users) {
            System.out.println(user.getUser());
            System.out.println(username);
            if (user.getUser().equalsIgnoreCase(username)) {
                System.out.println("FOUND");
                returnArray[0] = user;
                returnArray[1] = rank;
                return returnArray;
            } else {
                rank++;
            }
        }
        returnArray[0] = null;
        returnArray[1] = -1;
        return returnArray;
    }

    public ArrayList<Quote> getChannelQuotes(String channel) {
        ArrayList<Quote> quotes = db.getQuotesFromChannel(channel);
        return quotes;
    }

    public void writeQuotesToDatabase(ArrayList<Quote> quotes) {
        boolean success = db.writeQuotesToDatabase(quotes);
        if (success) {
            for (Quote quote : quotes) {
                quote.setExistsInDatabase(true);
            }
        }
    }

    public void writeChatMessagesToDatabase(ArrayList<ChatMessage> chatMessages) {
        boolean success = db.writeChatMessagesToDatabase(chatMessages);
        if (success) {
            for (ChatMessage chatMessage : chatMessages) {
                chatMessage.setExistsInDatabase(true);
            }
        }
    }
}
