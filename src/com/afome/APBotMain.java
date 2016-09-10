package com.afome;

import com.afome.ChatBot.ConfigHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
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

        ConfigHandler config = ConfigHandler.getInstance();
        for (String channel : config.getChannels()) {
            if (config.isChannelEnabled(channel)) {
                ((TabPane) ((BorderPane) root).getCenter()).getTabs().add(createNewChannelTab(channel));
            }
        }

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

    private Tab createNewChannelTab(String channel) {
        Tab tab = new Tab(channel);
        tab.setId("tab_" + channel);
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        TextArea textArea = new TextArea();
        textArea.setId("chat_text_area_" + channel);
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(50);
        TextField textField = new TextField();
        textField.setId("chat_text_field_" + channel);
        textField.setPrefColumnCount(50);
        vbox.getChildren().addAll(textArea, textField);
        tab.setContent(vbox);
        return tab;
    }

    public static void teardownLogger() {
        log.setUseParentHandlers(false);
    }
}
