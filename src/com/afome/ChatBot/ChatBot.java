package com.afome.ChatBot;

import com.afome.APBotMain;

import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatBot implements Runnable {
    private static final Logger log = Logger.getLogger(APBotMain.class.getName());
    private ConfigHandler config;
    private boolean running = false;
    private TwitchChatConnection chatConnection;


    public ChatBot() {

    }

    public void run() {
        try {
            log.log(Level.INFO, "Starting bot");
            config = ConfigHandler.getInstance();

            chatConnection = new TwitchChatConnection(config.getChannel());
            chatConnection.connect();

            running = true;

            log.log(Level.INFO, "Startup completed -- beginning main loop");
            // MAIN LOOP
            while (running) {
                chatConnection.handleQueuedMessages();
                chatConnection.iteration();

                Thread.sleep(100);
            }

            chatConnection.terminateConnection();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        log.log(Level.INFO, "Terminating bot");
    }

    public void stopBot() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public TwitchChatConnection getChatConnection() {
        return chatConnection;
    }
}
