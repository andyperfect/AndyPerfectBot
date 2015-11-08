package com.afome;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class APBotMain extends Application{
    public static final Logger log = Logger.getLogger(APBotMain.class.getName());

    public static void main(String args[]) throws Exception {
        initializeLogger();
        launch(args);
        teardownLogger();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("MainForm.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1024, 768));

        primaryStage.show();
    }

    public static void initializeLogger() {
        FileHandler fh;
        try {
            fh = new FileHandler("data" + File.separator + "ChatBot.log", true);
            log.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void teardownLogger() {
        log.setUseParentHandlers(false);
    }
}
