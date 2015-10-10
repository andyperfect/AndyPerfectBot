package com.afome.ChatBot;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigHandler {
    private String serverName = "";
    private String nick = "";
    private String password = "";
    private String channel = "";
    private String op = "";
    private int port = -1;
    private long timeNeededToQuote = -1;

    public ConfigHandler() throws IOException {
        String configContent = new String(Files.readAllBytes(Paths.get("data/config.json")));
        JSONObject jsonConfigObject = new JSONObject(configContent);

        serverName = jsonConfigObject.getString("servername");
        nick = jsonConfigObject.getString("nick");
        password = jsonConfigObject.getString("password");
        channel = jsonConfigObject.getString("channel");
        port = jsonConfigObject.getInt("port");
        timeNeededToQuote = jsonConfigObject.getLong("time_needed_to_quote");

        JSONObject users = jsonConfigObject.getJSONObject("users");
        op = users.getString("op");

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

    public int getPort() {
        return port;
    }

    public long getTimeNeededToQuote() {
        return timeNeededToQuote;
    }
}
