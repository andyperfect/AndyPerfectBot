package com.afome.dbchanges;

import java.sql.*;
import java.util.ArrayList;

import com.afome.ChatBot.DBConnection;


public class DBUpdate11 {

    public static void main(String args[]) {
        DBConnection db = DBConnection.getInstance();
        Connection conn = db.getDBConnection();


        try {
            // Get all the data out of users
            Statement statement = conn.createStatement();
            statement.setQueryTimeout(10);
            String queryString = "SELECT * from user";
            ResultSet rs = statement.executeQuery(queryString);
            ArrayList<OldUser> oldUsers = new ArrayList<OldUser>();
            while (rs.next()) {
                //Inner inner = new MyClass().new Inner();
                OldUser oldUser = new DBUpdate11().new OldUser();
                oldUser.id = rs.getInt("id");
                oldUser.channelId = rs.getInt("channel_id");
                oldUser.username = rs.getString("username");
                oldUser.timeConnected = rs.getLong("timeconnected");
                oldUser.chatCount = rs.getInt("chatcount");
                oldUsers.add(oldUser);
            }
            System.out.println("Done");

            // Scan through all the users and start finding duplicates. Keep one of the ID's, but drop the others
            ArrayList<UserToIds> userToIdsList = new ArrayList<UserToIds>();
            for (OldUser oldUser : oldUsers) {
                boolean found = false;
                for (UserToIds userToIds : userToIdsList) {
                    if (userToIds.username.equals(oldUser.username)) {
                        userToIds.unusedIds.add(oldUser.id);
                        found = true;
                    }
                }
                if (!found) {
                    UserToIds userToIds = new DBUpdate11().new UserToIds();
                    userToIds.username = oldUser.username;
                    userToIds.usedId = oldUser.id;
                    userToIdsList.add(userToIds);
                }
            }

            // Now that we have the id that will stay in the user table, create the user_channel table and dump the
            // data in
            String createUserChannelTableStatement =
            "CREATE TABLE \"user_channel\" ( \n" +
            "   `id`            INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, \n" +
            "   `user_id`       INTEGER NOT NULL, \n" +
            "   `channel_id`    INTEGER NOT NULL, \n" +
            "   `time_connected` INTEGER NOT NULL, \n" +
            "   `chat_count`     INTEGER NOT NULL \n" +
            ")";
            statement = conn.createStatement();
            statement.execute(createUserChannelTableStatement);

            for (OldUser oldUser : oldUsers) {
                for (UserToIds userToIds : userToIdsList) {
                    if (userToIds.username.equals(oldUser.username)) {
                        String insertString = String.format(
                                "INSERT INTO user_channel (user_id, channel_id, time_connected, chat_count) " +
                                "VALUES (%s, %s, %s, %s)",
                                userToIds.usedId, oldUser.channelId, oldUser.timeConnected, oldUser.chatCount
                        );
                        statement = conn.createStatement();
                        statement.execute(insertString);
                    }
                }
            }

            // Go through all quotes and turn the user id's to the new user ID's to use
            statement = conn.createStatement();
            statement.setQueryTimeout(10);
            queryString = "SELECT * from quote";
            rs = statement.executeQuery(queryString);
            ArrayList<Quote> oldQuotes = new ArrayList<Quote>();
            while (rs.next()) {
                //Inner inner = new MyClass().new Inner();
                Quote oldQuote = new DBUpdate11().new Quote();
                oldQuote.id = rs.getInt("id");
                oldQuote.channelId = rs.getInt("channel_id");
                oldQuote.userId = rs.getInt("user_id");
                oldQuote.quote = rs.getString("quote");
                oldQuote.date = rs.getLong("date");
                oldQuotes.add(oldQuote);
            }
            // Scan through the quotes and make updates to any quotes that reference the old user id
            for (Quote oldQuote : oldQuotes) {
                for (UserToIds userToIds : userToIdsList) {
                    if (userToIds.unusedIds.contains(oldQuote.userId)) {
                        System.out.println(String.format("Old user id:%s New user id:%s",
                                oldQuote.userId, userToIds.usedId));

                        String updateString = String.format(
                                "UPDATE quote " +
                                "SET user_id=%s " +
                                "WHERE user_id=%s",
                                userToIds.usedId, oldQuote.userId
                        );
                        statement = conn.createStatement();
                        statement.execute(updateString);
                    }
                }
            }

            // Delete the table and recreate it with the correct columns
            String dropUserTable = "DROP TABLE user";
            statement = conn.createStatement();
            statement.execute(dropUserTable);

            String recreateUserTable =
            "CREATE TABLE \"user\" ( " +
            "   `id`	        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
            "   `username`	    TEXT NOT NULL " +
            ")";
            statement = conn.createStatement();
            statement.execute(recreateUserTable);

            // Only insert the rows that should stay in the db
            for (OldUser oldUser : oldUsers) {
                for (UserToIds userToIds : userToIdsList) {
                    if (userToIds.usedId == oldUser.id) {
                        System.out.println(String.format("INSERT USER %s with id %s", oldUser.username, oldUser.id));
                        String insertString = String.format(
                                "INSERT INTO user (id, username) " +
                                "VALUES (%s, '%s')",
                                oldUser.id, oldUser.username
                        );
                        statement = conn.createStatement();
                        statement.execute(insertString);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class OldUser {
        public int id;
        public int channelId;
        public String username;
        public long timeConnected;
        public int chatCount;
    }

    private class UserToIds {
        public String username;
        public int usedId;
        public ArrayList<Integer> unusedIds= new ArrayList<Integer>();
    }

    private class Quote {
        public int id;
        public int channelId;
        public int userId;
        public String quote;
        public long date;
    }
}
