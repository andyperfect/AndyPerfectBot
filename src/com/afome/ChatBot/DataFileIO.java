package com.afome.ChatBot;

import com.afome.APBotMain;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataFileIO {
    private static final Logger log = Logger.getLogger(APBotMain.class.getName());
    private String userDatafilePath = "data" + File.separator + "userdata.txt";
    private String quotesFilePath = "data" + File.separator + "quotedata.txt";

    private String dateTimeFormatString = "yyyy-MM-dd";
    DateTimeFormatter quoteDateFormatter = null;

    DBConnection db = new DBConnection();

    public DataFileIO() {
        quoteDateFormatter = DateTimeFormatter.ofPattern(dateTimeFormatString);
    }

    public UserDataList createUserDataFromDatabase(String channel) {
        return db.getAllUserInfo(channel);
    }

    public void writeUserDataToDatabase(UserDataList dataList) {
        db.batchUserUpdate(dataList);
    }

    public void writeChannelToDatabase(String channel) {
        db.createChannel(channel);
    }

    public ArrayList<Quote> createQuoteListFromFile() {
        ArrayList<Quote> quotes = new ArrayList<Quote>();
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(new File(quotesFilePath));
        } catch (Exception e) {
            System.out.println("LOG: FAILED TO READ QUOTES FILE: " + e.getMessage());
        }

        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            if (line.length() > 0) {
                int delimIndex = line.lastIndexOf(":");
                if (delimIndex == -1) {
                    System.out.println("Unable to parse line: " + line);
                } else {
                    String quoteString = line.substring(0, delimIndex);
                    String quoteDate = line.substring(delimIndex + 1, line.length());
                    String[] splitDate = quoteDate.split("-");
                    quotes.add(new Quote(quoteString, LocalDate.of(Integer.parseInt(splitDate[0]), Integer.parseInt(splitDate[1]), Integer.parseInt(splitDate[2]))));
                }
            } else {
                System.out.println("LOG: Skipping line parsing on line: " + line);
            }
        }
        return quotes;

    }

    public void writeQuoteListToFile(ArrayList<Quote> quotes) {
        try {
            PrintWriter writer = new PrintWriter(quotesFilePath);
            for (Quote quote : quotes) {
                writer.println(quote.getQuote() + ":" + quote.getDate().format(quoteDateFormatter));
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("LOG: FAILED TO WRITE QUOTES FILE: " + e.getMessage());
        }
    }
}
