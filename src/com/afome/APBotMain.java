package com.afome;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class APBotMain {
    public static final Logger log = Logger.getLogger(APBotMain.class.getName());

    public static void main(String args[]) throws Exception {
        initializeLogger();
        ConsoleController controller = new ConsoleController();
        controller.initialize();
    }

    public static void initializeLogger() {
        FileHandler fh;
        try {
            log.setUseParentHandlers(false);
            fh = new FileHandler("data" + File.separator + "ChatBot.log", true);
            log.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            log.severe(e.getMessage());
        } catch (IOException e) {
            log.severe(e.getMessage());
        }
    }
}
