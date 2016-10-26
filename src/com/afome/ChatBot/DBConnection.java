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
                        "SELECT u.username, u.channel_id, u.timeconnected, u.chatcount " +
                                "FROM user u " +
                                "INNER JOIN channel c " +
                                "ON u.channel_id = c.id " +
                                "WHERE c.name = '%s';",
                        channel);
                ResultSet rs = statement.executeQuery(queryString);
                while (rs.next()) {
                    returnList.add(new UserData(rs.getString("username"), rs.getLong("timeconnected"), rs.getInt("chatcount" )));
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
                        "SELECT u.username, u.channel_id, u.timeconnected, u.chatcount " +
                                "FROM user u " +
                                "INNER JOIN channel c " +
                                "ON u.channel_id = c.id " +
                                "WHERE c.name = '%s' " +
                                "AND u.username != '%s' " +
                                "ORDER BY u.timeconnected DESC;",
                        channel, botUsername);
                ResultSet rs = statement.executeQuery(queryString);
                while (rs.next()) {
                    returnList.add(new UserData(rs.getString("username"), rs.getLong("timeconnected"), rs.getInt("chatcount" )));
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
                        "SELECT u.username, u.channel_id, u.timeconnected, u.chatcount " +
                                "FROM user u " +
                                "INNER JOIN channel c " +
                                "ON u.channel_id = c.id " +
                                "WHERE c.name = '%s' " +
                                "AND u.username != '%s' " +
                                "ORDER BY u.chatcount DESC;",
                        channel, botUsername);
                ResultSet rs = statement.executeQuery(queryString);
                while (rs.next()) {
                    returnList.add(new UserData(rs.getString("username"), rs.getLong("timeconnected"), rs.getInt("chatcount" )));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    public void getUserConnectionInfo(String username) {
        try {
            if (conn != null) {
                Statement statement = conn.createStatement();
                statement.setQueryTimeout(10);
                String queryString = String.format(
                        "SELECT u1.username, u1.timeconnected, u1.chatcount, " +
                                "  (SELECT count(*) FROM user AS u2 WHERE u2.timeconnected > u1.timeconnected) AS userrank " +
                                "FROM user as u1 WHERE u1.username='%s'; ",
                        username);
                System.out.println(queryString);
                ResultSet rs = statement.executeQuery(queryString);
                while (rs.next()) {
                    System.out.println(rs.getString("username"));
                    System.out.println(rs.getLong("timeconnected"));
                    System.out.println(rs.getLong("chatcount"));
                    System.out.println(rs.getLong("userrank") + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserData(String username, long timeConnected, int chatCount, String channel) {
        try {
            if (conn != null) {
                Statement statement = conn.createStatement();
                statement.setQueryTimeout(10);
                String queryString = String.format(
                        "UPDATE user " +
                        "SET timeconnected=%s,chatcount=%s " +
                        "WHERE username='%s';",
                        timeConnected, chatCount, username);
                System.out.println(queryString);
                statement.executeUpdate(queryString);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void batchUserUpdate(UserDataList dataList) {
        try {
            if (conn != null) {
                conn.setAutoCommit(false);
                String queryString =
                        "INSERT OR REPLACE INTO user (id, channel_id, username, timeconnected, chatcount) " +
                        "VALUES " +
                        "( " +
                        "(SELECT u.id FROM user u " +
                        "INNER JOIN channel c ON u.channel_id = c.id " +
                        "WHERE u.username = ? AND c.name = ?), " +
                        "(SELECT id FROM channel WHERE name=?), ?, ?, ?);";
                PreparedStatement pStatement = conn.prepareStatement(queryString);
                for (UserData userData : dataList) {
                    pStatement.setString(1, userData.getUser());
                    pStatement.setString(2, dataList.getChannel());
                    pStatement.setString(3, dataList.getChannel());
                    pStatement.setString(4, userData.getUser());
                    pStatement.setLong(5, userData.getNumMillis());
                    pStatement.setInt(6,  userData.getChatCount());
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
}
