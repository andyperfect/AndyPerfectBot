package com.afome.ChatBot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class UserDataList extends ArrayList<UserData> {

    private String channel;

    public UserData findUser(String username) {
        for (UserData userData : this) {
            if (userData.getUser().equals(username)) {
                return userData;
            }
        }
        return null;
    }

    public void updateAllUsers() {
        for (UserData userData : this) {
            userData.updateTime();
        }
        Collections.sort(this);
    }

    public UserData createNewUser(String username) {
        UserData newUser = new UserData(username, 0);
        add(newUser);
        System.out.println("Adding " + username + " to user list");
        return newUser;
    }

    public void assignModerators(ArrayList<String> mods) {
        for (String mod : mods) {
            for (UserData userData : this) {
                if (mod.equalsIgnoreCase(userData.getUser())) {
                    userData.setUserType(UserType.MODERATOR);
                }
            }
        }
    }

    /* Takes the list of chatters currently in stream and updates
     * all users' times accordingly.
     */
    public void handleCurrentChatters(ArrayList<String> users) {
        if (users == null || users.size() == 0) {
            return;
        }

        //0 for add new user
        //1 for marked as joined
        HashMap<String, Integer> userMap = new HashMap<String, Integer>();
        for (String user : users) {
            userMap.put(user, 0);
        }

        for (UserData user : this) {
            if (userMap.get(user.getUser()) != null) {
                user.joined();
                userMap.put(user.getUser(), 1);
            } else {
                user.parted();
            }
        }

        // Find all users that are still marked as 0. They don't exist in the user list and need to be added
        for (String user : users) {
            if (userMap.get(user) == 0) {
                createNewUser(user).joined();
            }
        }
    }

    public String getChannel() {
        return channel;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }
}
