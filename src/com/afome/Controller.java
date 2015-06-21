package com.afome;

import com.afome.ChatBot.ChatBot;
import com.afome.ChatBot.ChatMessage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.util.ArrayList;

public class Controller {
    ChatBot bot;
    private int lastChatMessageIndex = -1;

    Timeline chatUpdater;
    Timeline labelUpdater;


    @FXML private TextArea chatTextArea;
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


        chatUpdater = new Timeline(new KeyFrame(Duration.millis(500.0), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ArrayList<ChatMessage> chatLog = bot.getChatLog();

                //We have new chat messages
                if (lastChatMessageIndex + 1 < chatLog.size()) {
                    int i;
                    for (i = chatLog.size() - 1; i > lastChatMessageIndex; i--) {
                        System.out.println("Appending line");
                        chatTextArea.appendText(chatLog.get(i).toString() + "\n");
                    }
                    lastChatMessageIndex = i + 1;
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

        chatUpdater.stop();
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
