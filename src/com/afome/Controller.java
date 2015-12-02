package com.afome;

import com.afome.ChatBot.ChatBot;
import com.afome.ChatBot.ChatMessage;
import com.afome.ChatBot.Earthbound.EBNamingOption;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
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
    @FXML private Label labelCurEBWinnerUsername;
    @FXML private Label labelCurEBWinnerChoice;
    @FXML private Label labelCurEBVoteStatus;

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

    @FXML protected void handleEBVoteOpenButton(ActionEvent event) {
        if (bot == null || !bot.isRunning()) {
            return;
        }

        if (!bot.isEBVotingOpen()) {
            bot.setEBVotingOpen(true);
            labelCurEBVoteStatus.setText("Voting is open");
            labelCurEBWinnerUsername.setText("None available");
            labelCurEBWinnerChoice.setText("None Available");
        }
    }

    @FXML protected void handleEBVoteCloseButton(ActionEvent event) {
        if (bot == null || !bot.isRunning()) {
            return;
        }

        if (bot.isEBVotingOpen()) {
            bot.setEBVotingOpen(false);
            labelCurEBVoteStatus.setText("Voting is closed");
        }
    }

    @FXML protected void handleEBVotePickButton(ActionEvent event) {
        if (bot == null || !bot.isRunning()) {
            return;
        }

        EBNamingOption curWinningOption = bot.pickEBVotingWinner();
        if (curWinningOption == null) {
            labelCurEBWinnerUsername.setText("None available");
            labelCurEBWinnerChoice.setText("None Available");
        } else {
            labelCurEBWinnerUsername.setText(curWinningOption.getUser());
            labelCurEBWinnerChoice.setText(String.join(", ", curWinningOption.getCharacters()));
        }

    }

    @FXML protected void handleEBVoteAcceptButton(ActionEvent event) {
        if (bot == null || !bot.isRunning()) {
            return;
        }

        bot.acceptEBVotingWinner();
    }
}
