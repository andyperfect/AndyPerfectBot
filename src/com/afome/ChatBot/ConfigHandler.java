package com.afome.ChatBot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigHandler {
    private static ConfigHandler instance = null;

    private String serverName = "";
    private String nick = "";
    private String password = "";
    private String channel = "";
    private String op = "";
    private ArrayList<String> mods;
    private boolean botBanEnabled = true;
    private int port = -1;
    private long timeNeededToQuote = -1;
    private long timeBetweenUserCommands = -1;

    public static ConfigHandler getInstance() throws IOException {
        if (instance == null) {
            instance = new ConfigHandler();
        }
        return instance;
    }

    private ConfigHandler() throws IOException {
        String configContent = new String(Files.readAllBytes(Paths.get("data" + File.separator + "config.json")));
        JSONObject jsonConfigObject = new JSONObject(configContent);

        serverName = jsonConfigObject.getString("servername");
        nick = jsonConfigObject.getString("nick");
        password = jsonConfigObject.getString("password");
        channel = jsonConfigObject.getString("channel");
        port = jsonConfigObject.getInt("port");
        timeNeededToQuote = jsonConfigObject.getLong("time_needed_to_quote");
        timeBetweenUserCommands = jsonConfigObject.getLong("time_between_user_commands");
        botBanEnabled = jsonConfigObject.getInt("botbanenabled") == 1 ? true : false;

        JSONObject users = jsonConfigObject.getJSONObject("users");
        op = users.getString("op");

        mods = new ArrayList<String>();
        JSONArray modJsonList = users.getJSONArray("moderators");
        for (int i = 0; i < modJsonList.length(); i++) {
            mods.add(modJsonList.getString(i));
        }
    }

    public String getServerName() {
        return serverName;
    }

    public String getNick() {
        return nick;
    }

    public String getPassword() {
        return password;
    }

    public String getChannel() {
        return channel;
    }

    public String getOp() {
        return op;
    }

    public ArrayList<String> getMods() {
        return mods;
    }

    public int getPort() {
        return port;
    }

    public long getTimeNeededToQuote() {
        return timeNeededToQuote;
    }

    public long getTimeBetweenUserCommands() {
        return timeBetweenUserCommands;
    }

    public boolean isBotBanEnabled() {
        return botBanEnabled;
    }
}
