package com.afome.ChatBot;

import java.util.ArrayList;
import java.util.Collections;

public class UserDataList extends ArrayList<UserData> {

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
}
