package com.afome.ChatBot.models;

public class RouletteModel {
    private String channel;
    private String username;
    private int value = -1;
    private long timeInMillis = -1;
    private boolean existsInDatabase;

    public RouletteModel(String channel, String username, int value, long timeInMillis, boolean existsInDatabase) {
        this.channel = channel;
        this.username = username;
        this.value = value;
        this.timeInMillis = timeInMillis;
        this.existsInDatabase = existsInDatabase;
    }

    public RouletteModel(String channel, String username, int value) {
        this.channel = channel;
        this.username = username;
        this.value = value;
        this.timeInMillis = System.currentTimeMillis();
        this.existsInDatabase = false;
    }

    public String getChannel() {
        return channel;
    }

    public String getUsername() {
        return username;
    }

    public int getValue() {
        return value;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public boolean doesExistInDatabase() {
        return existsInDatabase;
    }

    public void setExistsInDatabase(boolean existsInDatabase) {
        this.existsInDatabase = existsInDatabase;
    }
}

