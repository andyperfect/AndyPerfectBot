package com.afome;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class DataFileIO {
    private String filePath = "data/userdata.txt";

    public DataFileIO() {

    }

    public UserDataList createDataFromFile() {
        UserDataList userDataList = new UserDataList();
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(new File(filePath));
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

    public void writeDataToFile(UserDataList dataList) {
        try {
            PrintWriter writer = new PrintWriter(filePath);
            for (UserData userData : dataList) {
                writer.println(userData.getUser() + " " + String.valueOf(userData.getNumMillis() + " " + String.valueOf(userData.getChatCount())));
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("LOG: FAILED TO WRITE FILE: " + e.getMessage());
        }
    }
}
