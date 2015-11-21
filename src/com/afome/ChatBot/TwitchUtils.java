package com.afome.ChatBot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.net.URL;
import java.net.HttpURLConnection;

public class TwitchUtils {

    public static ArrayList<String> getUsersInChat(String channel) {
        ArrayList<String> users = new ArrayList<String>();
        try {
            URL url = new URL("http://tmi.twitch.tv/group/user/" + channel + "/chatters");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String tmp;
            while (null != (tmp = br.readLine())) {
                responseBuilder.append(tmp);
            }
            JSONObject fullJson = new JSONObject(responseBuilder.toString());
            JSONObject chattersJson;
            if (fullJson.has("chatters")) {
                chattersJson = fullJson.getJSONObject("chatters");
                for (String key : chattersJson.keySet()) {
                    JSONArray userList = chattersJson.getJSONArray(key);
                    for (int i = 0; i < userList.length(); i++) {
                        users.add(userList.getString(i));
                    }
                }
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
            return users;
        } catch (java.net.SocketTimeoutException e) {
            e.printStackTrace();
            return users;
        } catch (IOException e) {
            e.printStackTrace();
            return users;
        }
        return users;
    }
}
