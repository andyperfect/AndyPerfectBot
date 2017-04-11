package com.afome;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import com.afome.ChatBot.ChatBot;
import com.afome.ChatBot.ConfigHandler;
import com.afome.ChatBot.TwitchChatConnection;


public class ConsoleController {
    ChatBot bot;

    public ConsoleController() {

    }

    public void initialize() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            input = input.trim();
            if (input.equals("start")) {
                handleStart();
            }
            if (input.equals("stop")) {
                handleStop();
            }
            if (input.equals("list")) {
                handleList();
            }
            if (input.startsWith("listconfig")) {
                ArrayList<String> params = new ArrayList<String>(Arrays.asList(input.split("\\s+")));
                if (params.size() != 2) {
                    System.out.println("invalid parameters");
                }
                handleListConfig(params.get(1));
            }
            System.out.print("> ");
        }
    }

    private void handleStart() {
        if (bot != null && bot.isRunning()) {
            return;
        }
        bot = new ChatBot();
        Thread chatBotThread = new Thread(bot);
        chatBotThread.start();
        System.out.print("Starting Bot...");
        while (!bot.isRunning()) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                //Do nothing
            }
        }
        System.out.println("Bot started");
    }

    private void handleStop() {
        if (bot == null || !bot.isRunning()) {
            return;
        }
        bot.stopBot();
        System.out.print("Stopping Bot...");
        while (bot.isRunning()) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                //Do nothing
            }
        }
        System.out.println("Bot stopped");
    }

    private void handleList() {
        if (bot == null || !bot.isRunning()) {
            System.out.println("Bot is not running");
            return;
        }
        StringBuilder outputString = new StringBuilder();
        ArrayList<TwitchChatConnection> chatConnections = bot.getChatConnections();
        for (TwitchChatConnection chatConn : chatConnections) {
            if (chatConn.isConnected()) {
                outputString.append(chatConn.getChannel());
                outputString.append(": online\n");
            }
            else {
                outputString.append(chatConn.getChannel());
                outputString.append(": offline\n");
            }
        }
        System.out.print(outputString.toString());
    }

    private void handleListConfig(String channel) {
        try {
            ConfigHandler configHandler = ConfigHandler.getInstance();
            HashMap<String, Object> channelConfig = configHandler.getChannelConfig(channel.toLowerCase());
            if (channelConfig == null) {
                return;
            }
            System.out.println("Config for channel " + channel);
            for (String key : channelConfig.keySet()) {
                System.out.println(key + ":" + channelConfig.get(key).toString());
            }
        } catch (Exception e) {
            System.out.println("Failed in handleListConfig: " + e.getMessage());
        }

    }
}
