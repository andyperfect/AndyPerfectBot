package com.afome;

import com.afome.ChatBot.ChatBot;
import com.afome.ChatBot.ChatMessage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.util.ArrayList;

public class Controller {
    ChatBot bot;
    private int lastChatMessageIndex = -1;
    Timeline chatUpdater;

    @FXML private TextArea chatTextArea;

    @FXML protected void handleStartBotButtonAction(ActionEvent event) {
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
    }

    @FXML protected void handleStopBotButtonAction(ActionEvent event) {
        chatUpdater.stop();
        bot.stopBot();

    }
}
