package com.afome.ChatBot;

public class UserData implements Comparable<UserData> {
    private String user = "";
    private UserType userType;
    private long numMillis = 0;
    private int chatCount = 0;
    private long joinTimeMillis = -1;
    private long lastCheckTimeMillis = -1;


    public UserData(String user, long numMillis) {
        this(user, numMillis, 0);

    }

    public UserData(String user, long numMillis, int chatCount) {
        this.user = user;
        this.numMillis = numMillis;
        this.chatCount = chatCount;
        this.userType = UserType.USER;
    }

    public void joined() {
        if (joinTimeMillis == -1) {
            System.out.println("LOG: Marking " + user + " as joined");
            joinTimeMillis = System.currentTimeMillis();
        }
    }

    public void parted() {
        updateTime();
        resetTimes();
    }

    public void handleChatMessage() {
        chatCount++;
        joined();
    }

    public void updateTime() {
        if (joinTimeMillis == -1) {
            return;
        }

        long curTimeMillis = System.currentTimeMillis();
        if (lastCheckTimeMillis == -1) {
            numMillis += (curTimeMillis - joinTimeMillis);
        } else {
            numMillis += (curTimeMillis - lastCheckTimeMillis);
        }
        lastCheckTimeMillis = curTimeMillis;
    }

    private void resetTimes() {
        joinTimeMillis = -1;
        lastCheckTimeMillis = -1;
    }

    public String toString() {
        return user + " " + String.valueOf(numMillis);
    }

    @Override
    public int compareTo(UserData other) {
        return this.getUser().compareTo(other.getUser());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UserData) {
            return ((UserData) o).getUser().equals(this.getUser());
        }
        return false;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getNumMillis() {
        return numMillis;
    }

    public void setNumMillis(long numMillis) {
        this.numMillis = numMillis;
    }

    public int getChatCount() {
        return chatCount;
    }

    public long getConnectionTimeInMinutes() {
        updateTime();
        return this.numMillis / 60000L;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
