package com.afome.ChatBot;

import com.afome.APBotMain;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class ConfigHandler {
    private static final Logger log = Logger.getLogger(APBotMain.class.getName());
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
        int defaultPort = 6667;
        boolean defaultEnabled = false;
        boolean defaultEnableBotBan = false;
        boolean defaultEnableUserTrackingCommands = false;
        boolean defaultEnableRoulette = false;
        boolean defaultEnableQuotes = false;
        boolean defaultEnableBotCommands = false;
        boolean defaultEnableWovCommand = false;
        long defaultTimeNeededToQuote = 86400000;
        long defaultTimeBetweenUserCommands = 5000;

        log.info("Beginning config read");
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
            channelConfig.put("port",
                    curChannelJson.has("port") ?
                            curChannelJson.getInt("port") :
                            defaultPort
            );
            channelConfig.put("enabled",
                    curChannelJson.has("enabled") ?
                            curChannelJson.getInt("enabled") == 1 :
                            defaultEnabled
            );
            channelConfig.put("time_needed_to_quote",
                    curChannelJson.has("time_needed_to_quote") ?
                            curChannelJson.getLong("time_needed_to_quote") :
                            defaultTimeNeededToQuote
            );
            channelConfig.put("time_between_user_commands",
                    curChannelJson.has("time_between_user_commands") ?
                            curChannelJson.getLong("time_between_user_commands") :
                            defaultTimeBetweenUserCommands
            );
            channelConfig.put("enable_bot_ban",
                    curChannelJson.has("enable_bot_ban") ?
                            curChannelJson.getInt("enable_bot_ban") == 1 :
                            defaultEnableBotBan
            );
            channelConfig.put("enable_user_tracking_commands",
                    curChannelJson.has("enable_user_tracking_commands") ?
                            curChannelJson.getInt("enable_user_tracking_commands") == 1 :
                            defaultEnableUserTrackingCommands
            );
            channelConfig.put("enable_quotes",
                    curChannelJson.has("enable_quotes") ?
                            curChannelJson.getInt("enable_quotes") == 1 :
                            defaultEnableQuotes
            );
            channelConfig.put("enable_roulette",
                    curChannelJson.has("enable_roulette") ?
                            curChannelJson.getInt("enable_roulette") == 1 :
                            defaultEnableRoulette
            );
            channelConfig.put("enable_bot_commands",
                    curChannelJson.has("enable_bot_commands") ?
                            curChannelJson.getInt("enable_bot_commands") == 1 :
                            defaultEnableBotCommands
            );
            channelConfig.put("enable_wov_command",
                    curChannelJson.has("enable_wov_command") ?
                            curChannelJson.getInt("enable_wov_command") == 1 :
                            defaultEnableWovCommand
            );
            channelConfig.put("op", curChannelJson.getString("channel"));

            if (curChannelJson.has("users")) {
                JSONObject users = curChannelJson.getJSONObject("users");

                ArrayList<String> channelMods = new ArrayList<String>();
                JSONArray modJsonList = users.getJSONArray("moderators");
                for (int j = 0; j < modJsonList.length(); j++) {
                    channelMods.add(modJsonList.getString(j));
                }
                channelConfig.put("mods", channelMods);
            } else {
                channelConfig.put("mods", new ArrayList<String>());
            }

            channelConfigs.put(curChannelJson.getString("channel"), channelConfig);
        }
        log.info("Ending config read");
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

    public boolean isWovCommandEnabled(String channel) {
        return (Boolean) channelConfigs.get(channel).get("enable_wov_command");
    }

    public boolean isChannelEnabled(String channel) {
        return (Boolean) channelConfigs.get(channel).get("enabled");
    }

    public HashMap<String, Object> getChannelConfig(String channel) {
        if (channelConfigs.containsKey(channel)) {
            return channelConfigs.get(channel);
        }
        return null;
    }
}
