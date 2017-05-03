package com.afome.ChatBot;

import com.afome.APBotMain;
import java.io.IOException;
import java.util.logging.Logger;

public class DBIterationWriter implements Runnable {

    private static final Logger log = Logger.getLogger(APBotMain.class.getName());
    private TwitchChatConnection chatConn;
    private ConfigHandler config;

    DBIterationWriter(TwitchChatConnection chatConn) {
        this.chatConn = chatConn;
        try {
            config = ConfigHandler.getInstance();
        } catch(IOException e) {
            log.severe(e.getMessage());
        }
    }

    public void run() {
        chatConn.fileIO.writeUserDataToDatabase(chatConn.fullUserDataList);
        chatConn.fileIO.writeChatMessagesToDatabase(chatConn.getChatLog());
        if (config.isQuotesEnabled(chatConn.getChannel())) {
            chatConn.fileIO.writeQuotesToDatabase(chatConn.getQuotes());
        }
    }
}
