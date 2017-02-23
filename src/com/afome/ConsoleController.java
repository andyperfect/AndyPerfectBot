package com.afome;

import java.util.Scanner;
import java.util.ArrayList;
import com.afome.ChatBot.ChatBot;
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
            input = input.replaceAll("\\s+", "");
            if (input.equals("start")) {
                handleStart();
            }
            if (input.equals("stop")) {
                handleStop();
            }
            if (input.equals("list")) {
                handleList();
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
}
