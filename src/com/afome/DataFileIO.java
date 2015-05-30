package com.afome;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class DataFileIO {
    private String userDatafilePath = "data/userdata.txt";
    private String quotesFilePath = "data/quotedata.txt";

    private String dateTimeFormatString = "yyyy-MM-dd";
    DateTimeFormatter quoteDateFormatter = null;

    public DataFileIO() {
        quoteDateFormatter = DateTimeFormatter.ofPattern(dateTimeFormatString);
    }

    public UserDataList createUserDataFromFile() {
        UserDataList userDataList = new UserDataList();
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(new File(userDatafilePath));
        } catch (Exception e) {
            System.out.println("LOG: FAILED TO READ FILE: " + e.getMessage());
        }

        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            if (line.length() > 0) {
                String[] splitLine = line.split("\\s+");
                if (splitLine.length != 3) {
                    System.out.println("Unable to parse line: " + line);
                } else {
                    userDataList.add(new UserData(splitLine[0], Long.parseLong(splitLine[1]), Integer.parseInt(splitLine[2])));
                }
            } else {
                System.out.println("LOG: Skipping line parsing on line: " + line);
            }
        }
        return userDataList;
    }

    public void writeUserDataToFile(UserDataList dataList) {
        try {
            PrintWriter writer = new PrintWriter(userDatafilePath);
            for (UserData userData : dataList) {
                writer.println(userData.getUser() + " " + String.valueOf(userData.getNumMillis() + " " + String.valueOf(userData.getChatCount())));
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("LOG: FAILED TO WRITE FILE: " + e.getMessage());
        }
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
