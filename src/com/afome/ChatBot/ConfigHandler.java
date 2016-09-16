package com.afome.ChatBot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class ConfigHandler {
    private static ConfigHandler instance = null;
    private HashMap<String, HashMap<String, Object>> channelConfigs;

    private String serverName = "";
    private String nick = "";
    private String password = "";
    private String clientId = "";

    public static ConfigHandler getInstance() throws IOException {
        if (instance == null) {
            instance = new ConfigHandler();
        }
        return instance;
    }

    private ConfigHandler() throws IOException {
        System.out.println("Beginning config read");
        channelConfigs = new HashMap<String, HashMap<String, Object>>();

        String configContent = new String(Files.readAllBytes(Paths.get("data" + File.separator + "config.json")));
        JSONObject jsonConfigObject = new JSONObject(configContent);

        serverName = jsonConfigObject.getString("servername");
        nick = jsonConfigObject.getString("nick");
        password = jsonConfigObject.getString("password");
        clientId = jsonConfigObject.getString("client_id");

        JSONArray channels = jsonConfigObject.getJSONArray("channels");
        for (int i = 0; i < channels.length(); i++) {
            JSONObject curChannelJson = channels.getJSONObject(i);
            HashMap<String, Object> channelConfig = new HashMap<String, Object>();
            channelConfig.put("port", curChannelJson.getInt("port"));
            channelConfig.put("enabled", curChannelJson.getInt("enabled") == 1);
            channelConfig.put("time_needed_to_quote", curChannelJson.getLong("time_needed_to_quote"));
            channelConfig.put("time_between_user_commands", curChannelJson.getLong("time_between_user_commands"));
            channelConfig.put("enable_bot_ban", curChannelJson.getInt("enable_bot_ban") == 1);
            channelConfig.put("enable_user_tracking_commands", curChannelJson.getInt("enable_user_tracking_commands") == 1);
            channelConfig.put("enable_quotes", curChannelJson.getInt("enable_quotes") == 1);
            channelConfig.put("enable_roulette", curChannelJson.getInt("enable_roulette") == 1);
            channelConfig.put("enable_bot_commands", curChannelJson.getInt("enable_bot_commands") == 1);

            JSONObject users = curChannelJson.getJSONObject("users");
            channelConfig.put("op", users.getString("op"));

            ArrayList<String> channelMods = new ArrayList<String>();
            JSONArray modJsonList = users.getJSONArray("moderators");
            for (int j = 0; j < modJsonList.length(); j++) {
                channelMods.add(modJsonList.getString(j));
            }
            channelConfig.put("mods", channelMods);
            channelConfigs.put(curChannelJson.getString("channel"), channelConfig);
        }
        System.out.println("Ending config read");
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

    public String getClientId() {
        return clientId;
    }

    public ArrayList<String> getChannels() {
        return new ArrayList<String>(channelConfigs.keySet());
    }

    public String getOp(String channel) {
        return (String) channelConfigs.get(channel).get("op");
    }

    public ArrayList<String> getMods(String channel) {
        return (ArrayList<String>) channelConfigs.get(channel).get("mods");
    }

    public int getPort(String channel) {
        return (Integer) channelConfigs.get(channel).get("port");
    }

    public long getTimeNeededToQuote(String channel) {
        return (Long) channelConfigs.get(channel).get("time_needed_to_quote");
    }

    public long getTimeBetweenUserCommands(String channel) {
        return (Long) channelConfigs.get(channel).get("time_between_user_commands");
    }

    public boolean isBotBanEnabled(String channel) {
        return (Boolean) channelConfigs.get(channel).get("enable_bot_ban");
    }

    public boolean isUserTrackingCommandsEnabled(String channel) {
        return (Boolean) channelConfigs.get(channel).get("enable_user_tracking_commands");
    }

    public boolean isQuotesEnabled(String channel) {
        return (Boolean) channelConfigs.get(channel).get("enable_quotes");
    }

    public boolean isRouletteEnabled(String channel) {
        return (Boolean) channelConfigs.get(channel).get("enable_roulette");
    }

    public boolean isBotCommandsEnabled(String channel) {
        return (Boolean) channelConfigs.get(channel).get("enable_bot_commands");
    }

    public boolean isChannelEnabled(String channel) {
        return (Boolean) channelConfigs.get(channel).get("enabled");
    }
}
