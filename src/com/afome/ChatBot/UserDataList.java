package com.afome.ChatBot;

import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;

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

    public void assignOP(String op) {
        for (UserData userData : this) {
            if (op.equalsIgnoreCase(userData.getUser())) {
                userData.setUserType(UserType.OPERATOR);
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

    // Returns the user's time rank among all other users in the same channel
    public int getUserTimeRank(UserData user) {
        int numUsersHigher = 0;
        for (UserData userData : this) {
            if (userData.getNumMillis() > user.getNumMillis()) {
                numUsersHigher++;
            }
        }
        return numUsersHigher + 1;
    }

    // Returns the user's chat rank among all other users in the same channel
    public int getUserChatRank(UserData user) {
        int numUsersHigher = 0;
        for (UserData userData : this) {
            if (userData.getChatCount() > user.getChatCount()) {
                numUsersHigher++;
            }
        }
        return numUsersHigher + 1;
    }

    public String getChannel() {
        return channel;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }
}
