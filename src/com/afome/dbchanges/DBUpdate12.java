package com.afome.dbchanges;

import java.sql.*;

import com.afome.ChatBot.DBConnection;

public class DBUpdate12 {
    public static void main(String args[]) {
        DBConnection db = DBConnection.getInstance();
        Connection conn = db.getDBConnection();

        try {
            String createChatMessageTable =
            "CREATE TABLE \"chat_message\" ( \n" +
            "   `id`                INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, \n" +
            "   `user_channel_id`   INTEGER NOT NULL, \n" +
            "   `message`           TEXT NOT NULL, \n" +
            "   `date`              INTEGER NOT NULL \n" +
            ")";

            Statement statement = conn.createStatement();
            statement.setQueryTimeout(10);
            statement.execute(createChatMessageTable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
