package com.afome;

import com.afome.ChatBot.ChatBot;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class Controller {
    ChatBot bot;

    @FXML protected void handleStartBotButtonAction(ActionEvent event) {
        bot = new ChatBot();
        Thread chatBotThread = new Thread(bot);
        chatBotThread.start();
    }

    @FXML protected void handleStopBotButtonAction(ActionEvent event) {
        bot.stopBot();
    }
}
