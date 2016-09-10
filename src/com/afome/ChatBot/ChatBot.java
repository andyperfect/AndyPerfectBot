package com.afome.ChatBot;

import com.afome.APBotMain;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatBot implements Runnable {
    private static final Logger log = Logger.getLogger(APBotMain.class.getName());
    private ConfigHandler config;
    private boolean running = false;
    ArrayList<TwitchChatConnection> chatConnections;

    private long lastChannelStatusCheckTime = -1;
    private HashMap<String, Boolean> previousChannelStatuses;
    private HashMap<String, Boolean> channelStatuses;


    public ChatBot() {

    }

    public void run() {
        try {
            log.log(Level.INFO, "Starting bot");
            config = ConfigHandler.getInstance();
            chatConnections = new ArrayList<TwitchChatConnection>();
            channelStatuses = new HashMap<String, Boolean>();
            previousChannelStatuses = new HashMap<String, Boolean>();

            // Iterate over all the channels from the config. If they're enabled in the config, add them to the array
            // Mark their status as disconnected as well
            for (String channel : config.getChannels()) {
                if (config.isChannelEnabled(channel)) {
                    chatConnections.add(new TwitchChatConnection(channel));
                    channelStatuses.put(channel, false);
                    previousChannelStatuses.put(channel, false);
                }
            }

            checkChannelStatuses();
            running = true;

            log.log(Level.INFO, "Startup completed -- beginning main loop");
            // MAIN LOOP
            boolean channelCurrentlyOn = false;
            while (running) {
                channelCurrentlyOn = false;
                for (TwitchChatConnection connection : chatConnections) {
                    // Stream just turned on
                    if (!previousChannelStatuses.get(connection.getChannel()) &&
                            channelStatuses.get(connection.getChannel())) {
                        System.out.println("Stream " + connection.getChannel() + " turned on. Connecting...");
                        previousChannelStatuses.put(connection.getChannel(), true);
                        connection.connect();
                    }
                    // Stream just turned off
                    if (previousChannelStatuses.get(connection.getChannel()) &&
                            !channelStatuses.get(connection.getChannel())) {
                        System.out.println("Stream " + connection.getChannel() + " turned off. Disconnecting...");
                        previousChannelStatuses.put(connection.getChannel(), false);
                        connection.terminateConnection();
                    }

                    if (connection.isConnected()) {
                        channelCurrentlyOn = true;
                        connection.handleQueuedMessages();
                        connection.iteration();
                    }
                }
                if (channelCurrentlyOn) {
                    Thread.sleep(100);
                } else {
                    Thread.sleep(60000);
                }

                if (System.currentTimeMillis() - lastChannelStatusCheckTime >= ChatBotUtils.ONE_MINUTE_IN_MILLIS) {
                    checkChannelStatuses();
                }
            }

            for (TwitchChatConnection connection : chatConnections) {
                if (connection.isConnected()) {
                    connection.terminateConnection();
                }
            }

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

    /* public TwitchChatConnection getChatConnection() {
        return chatConnection;
    } */

    private void checkChannelStatuses() {
        for (TwitchChatConnection connection : chatConnections) {
            previousChannelStatuses.put(connection.getChannel(), channelStatuses.get(connection.getChannel()));
            if (TwitchUtils.isChannelLive(ChatBotUtils.stripHashtagFromChannel(connection.getChannel()))) {
                channelStatuses.put(connection.getChannel(), true);
            } else {
                channelStatuses.put(connection.getChannel(), false);
            }
        }
        lastChannelStatusCheckTime = System.currentTimeMillis();
    }

    private boolean areAnyChannelslive() {
        for (String channel : channelStatuses.keySet()) {
            if (channelStatuses.get(channel)) {
                return true;
            }
        }
        return false;
    }
}
