package com.afome.ChatBot;

import java.io.File;
import java.sql.*;

public class DBConnection {
    private String dbURL = "jdbc:sqlite:data" + File.separator + "APTwitchBotDB.db";
    Connection conn = null;

    public DBConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbURL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                        "((SELECT id FROM user WHERE username=?), (SELECT id FROM channel WHERE name=?), ?, ?, ?);";
                PreparedStatement pStatement = conn.prepareStatement(queryString);
                for (UserData userData : dataList) {
                    pStatement.setString(1, userData.getUser());
                    pStatement.setString(2, dataList.getChannel());
                    pStatement.setString(3, userData.getUser());
                    pStatement.setLong(4, userData.getNumMillis());
                    pStatement.setInt(5, userData.getChatCount());
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
}
