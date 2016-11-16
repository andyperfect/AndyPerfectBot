package com.afome.ChatBot;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class DBConnection {
    private static DBConnection instance = null;
    private String dbURL = "jdbc:sqlite:data" + File.separator + "APTwitchBotDB.db";
    Connection conn = null;

    private DBConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbURL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getDBConnection() {
        return conn;
    }

    public boolean createChannel(String channel) {
        try {
            if (conn != null) {
                Statement statement = conn.createStatement();
                statement.setQueryTimeout(10);
                String queryString = String.format(
                        "INSERT INTO channel(name) " +
                        "SELECT '%s' " +
                        "WHERE NOT EXISTS " +
                        "(SELECT 1 FROM channel WHERE name = '%s');",
                        channel, channel);
                statement.executeUpdate(queryString);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public UserDataList getAllUserInfo(String channel) {
        UserDataList returnList = new UserDataList();
        returnList.setChannel(channel);
        try {
            if (conn != null) {
                Statement statement = conn.createStatement();
                statement.setQueryTimeout(10);
                String queryString = String.format(
                        "SELECT u.username, uc.channel_id, uc.time_connected, uc.chat_count " +
                        "FROM user u " +
                        "INNER JOIN user_channel uc ON u.id = uc.user_id " +
                        "INNER JOIN channel c ON uc.channel_id = c.id " +
                        "WHERE c.name = '%s'",
                        channel);
                ResultSet rs = statement.executeQuery(queryString);
                while (rs.next()) {
                    returnList.add(new UserData(rs.getString("username"), rs.getLong("time_connected"), rs.getInt("chat_count" )));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    public UserDataList getAllUserInfoRankedByTime(String channel, String botUsername) {
        UserDataList returnList = new UserDataList();
        returnList.setChannel(channel);
        try {
            if (conn != null) {
                Statement statement = conn.createStatement();
                statement.setQueryTimeout(10);
                String queryString = String.format(
                        "SELECT u.username, uc.channel_id, uc.time_connected, uc.chat_count " +
                        "FROM user u " +
                        "INNER JOIN user_channel uc ON u.id = uc.user_id " +
                        "INNER JOIN channel c ON uc.channel_id = c.id " +
                        "WHERE c.name = '%s' " +
                        "AND u.username != '%s' " +
                        "ORDER BY uc.time_connected DESC;",
                        channel, botUsername);
                ResultSet rs = statement.executeQuery(queryString);
                while (rs.next()) {
                    returnList.add(new UserData(
                            rs.getString("username"),
                            rs.getLong("time_connected"),
                            rs.getInt("chat_count" ))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    public UserDataList getAllUserInfoRankedByChatCount(String channel, String botUsername) {
        UserDataList returnList = new UserDataList();
        returnList.setChannel(channel);
        try {
            if (conn != null) {
                Statement statement = conn.createStatement();
                statement.setQueryTimeout(10);
                String queryString = String.format(
                        "SELECT u.username, uc.channel_id, uc.time_connected, uc.chat_count " +
                        "FROM user u " +
                        "INNER JOIN user_channel uc ON u.id = uc.user_id " +
                        "INNER JOIN channel c ON uc.channel_id = c.id " +
                        "WHERE c.name = '%s' " +
                        "AND u.username != '%s' " +
                         "ORDER BY uc.chat_count DESC;",
                        channel, botUsername);
                ResultSet rs = statement.executeQuery(queryString);
                while (rs.next()) {
                    returnList.add(new UserData(
                            rs.getString("username"),
                            rs.getLong("time_connected"),
                            rs.getInt("chat_count" ))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    public void batchUserUpdate(UserDataList dataList) {
        try {
            if (conn != null) {
                conn.setAutoCommit(false);
                String queryString =
                    "INSERT OR REPLACE INTO user (id, username) " +
                    "VALUES ((SELECT u.id FROM USER u WHERE u.username = ?), ?);";
                PreparedStatement pStatement = conn.prepareStatement(queryString);
                for (UserData userData : dataList) {
                    pStatement.setString(1, userData.getUser());
                    pStatement.setString(2, userData.getUser());
                    pStatement.addBatch();
                }
                pStatement.executeBatch();
                pStatement.close();

                queryString =
                    "INSERT OR REPLACE INTO user_channel (id, user_id, channel_id, time_connected, chat_count) " +
                    "VALUES ( " +
                    "   (SELECT uc.id FROM user_channel uc " +
                    "       INNER JOIN user u ON u.id = uc.user_id " +
                    "       INNER JOIN channel c ON c.id = uc.channel_id " +
                    "       WHERE u.username = ? AND c.name=?), " +
                    "   (SELECT u.id FROM user u WHERE u.username = ?), " +
                    "   (SELECT c.id from channel c WHERE c.name = ?), " +
                    "   ?, ? " +
                    ");";
                pStatement = conn.prepareStatement(queryString);
                for (UserData userData : dataList) {
                    pStatement.setString(1, userData.getUser());
                    pStatement.setString(2, dataList.getChannel());
                    pStatement.setString(3, userData.getUser());
                    pStatement.setString(4, dataList.getChannel());
                    pStatement.setLong(5, userData.getNumMillis());
                    pStatement.setInt(6, userData.getChatCount());
                    pStatement.addBatch();
                }
                pStatement.executeBatch();
                pStatement.close();
                conn.commit();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Quote> getQuotesFromChannel(String channel) {
        ArrayList<Quote> quotes = new ArrayList<Quote>();
        try {
            if (conn != null) {
                String queryString =
                        "SELECT q.id, c.name AS channel_name, u.username, q.quote, q.date " +
                        "FROM quote q " +
                        "   INNER JOIN channel c " +
                        "   ON q.channel_id = c.id " +
                        "   INNER JOIN user u " +
                        "   ON q.user_id = u.id " +
                        "WHERE c.name = ?;";
                PreparedStatement pStatement = conn.prepareStatement(queryString);
                pStatement.setString(1, channel);
                ResultSet rs = pStatement.executeQuery();
                while (rs.next()) {
                    quotes.add(new Quote(rs.getString("quote"), rs.getString("channel_name"),
                            rs.getString("username"), rs.getLong("date"), true));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quotes;
    }

    public boolean writeQuotesToDatabase(ArrayList<Quote> quotes) {
        try {
            if (conn != null) {
                conn.setAutoCommit(false);
                String queryString =
                        "INSERT INTO quote (channel_id, user_id, quote, date) " +
                        "VALUES ( " +
                        "(SELECT id FROM channel WHERE name=?), (SELECT id from user where username=?), ?, ?);";
                PreparedStatement pStatement = conn.prepareStatement(queryString);
                for (Quote quote : quotes) {
                    if (!quote.doesExistInDatabase()) {
                        pStatement.setString(1, quote.getChannel());
                        pStatement.setString(2, quote.getUserWhoAdded());
                        pStatement.setString(3, quote.getQuote());
                        pStatement.setLong(4, quote.getTimeInMillis());
                        pStatement.addBatch();
                    }
                }
                pStatement.executeBatch();
                pStatement.close();
                conn.commit();
                conn.setAutoCommit(true);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean writeChatMessagesToDatabase(ArrayList<ChatMessage> chatMessages) {
        try {
            if (conn != null) {
                conn.setAutoCommit(false);
                String queryString =
                        "INSERT INTO chat_message (user_channel_id, message, date) " +
                        "VALUES ( " +
                        "   ( " +
                        "       SELECT uc.id FROM user_channel uc " +
                        "       INNER JOIN user u on uc.user_id = u.id " +
                        "       INNER JOIN channel c on uc.channel_id = c.id " +
                        "       WHERE u.username=? " +
                        "       AND c.name =? " +
                        "   ), ?, ? " +
                        ")";
                PreparedStatement pStatement = conn.prepareStatement(queryString);
                for (ChatMessage chatMessage : chatMessages) {
                    if (!chatMessage.doesExistInDatabase()) {
                        pStatement.setString(1, chatMessage.getUser());
                        pStatement.setString(2, chatMessage.getChannel());
                        pStatement.setString(3, chatMessage.getMessage());
                        pStatement.setLong(4, chatMessage.getMessageMillis());
                        pStatement.addBatch();
                    }
                }
                pStatement.executeBatch();
                pStatement.close();
                conn.commit();
                conn.setAutoCommit(true);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
