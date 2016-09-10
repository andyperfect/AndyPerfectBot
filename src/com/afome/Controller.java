package com.afome;

import com.afome.ChatBot.ChatBot;
import com.afome.ChatBot.ChatMessage;
import com.afome.ChatBot.TwitchChatConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;

public class Controller {
    ChatBot bot;

    Timeline chatUpdater;
    Timeline labelUpdater;

    HashMap<String, Integer> channelChatIndex = new HashMap<String, Integer>();

    @FXML private TabPane chatTabPane;
    @FXML private Label labelBotRunning;

    private final String botRunningText = "Bot Running";
    private final String botStoppedText = "Bot Stopped";

    @FXML protected void handleStartBotButtonAction(ActionEvent event) {
        if (bot != null && bot.isRunning()) {
            return;
        }

        bot = new ChatBot();
        Thread chatBotThread = new Thread(bot);
        chatBotThread.start();

        try {
            Thread.sleep(1000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        for (TwitchChatConnection connection : bot.getChatConnections()) {
            channelChatIndex.put(connection.getChannel(), 0);
        }

        chatUpdater = new Timeline(new KeyFrame(Duration.millis(1000.0), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                for (TwitchChatConnection connection : bot.getChatConnections()) {
                    if (connection.isConnected()) {
                        ArrayList<ChatMessage> chatLog = connection.getChatLog();

                        //We have new chat messages
                        int lastChatMessageIndex = channelChatIndex.get(connection.getChannel());
                        if (lastChatMessageIndex < chatLog.size()) {
                            TextArea chatTextArea =
                                    ((TextArea)chatTabPane.lookup("#chat_text_area_" + connection.getChannel()));
                            int i;
                            for (i = lastChatMessageIndex; i < chatLog.size(); i++) {
                                chatTextArea.appendText(chatLog.get(i).toString() + "\n");
                            }
                            channelChatIndex.put(connection.getChannel(), i);
                        }
                    }
                }


            }
        }));
        chatUpdater.setCycleCount(Timeline.INDEFINITE);
        chatUpdater.play();


        labelUpdater = new Timeline(new KeyFrame(Duration.millis(5000.0), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (bot.isRunning()) {
                    setBotRunning(true);
                } else {
                    setBotRunning(false);
                }
            }
        }));
        labelUpdater.setCycleCount(Timeline.INDEFINITE);
        labelUpdater.play();
    }

    @FXML protected void handleStopBotButtonAction(ActionEvent event) {
        if (bot == null || !bot.isRunning()) {
            return;
        }

        //chatUpdater.stop();
        labelUpdater.stop();
        setBotRunning(false);
        bot.stopBot();



    }

    private void setBotRunning(boolean running) {
        if (running) {
            labelBotRunning.setText(botRunningText);
            labelBotRunning.setTextFill(Paint.valueOf("#39D531"));
        } else {
            labelBotRunning.setText(botStoppedText);
            labelBotRunning.setTextFill(Paint.valueOf("#d50b0a"));
        }
    }
}
