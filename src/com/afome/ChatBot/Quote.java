package com.afome.ChatBot;

public class Quote {

    private String quote;
    private String channel;
    private String userWhoAdded;
    private long timeInMillis;
    private boolean existsInDatabase = false;

    public Quote(String quote, String channel, String user, boolean existsInDatabase) {
        this.quote = quote;
        this.channel = channel;
        this.userWhoAdded = user;
        this.timeInMillis = System.currentTimeMillis();
        this.existsInDatabase = existsInDatabase;

    }

    public Quote(String quote, String channel, String user, long timeInMillis, boolean existsInDatabase) {
        this.quote = quote;
        this.channel = channel;
        this.userWhoAdded = user;
        this.timeInMillis = timeInMillis;
        this.existsInDatabase = existsInDatabase;
    }

    public String getQuote() {
        return quote;
    }

    public String getChannel() {
        return channel;
    }

    public String getUserWhoAdded() {
        return userWhoAdded;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public boolean doesExistInDatabase() {
        return existsInDatabase;
    }

    public void setExistsInDatabase(Boolean existsInDatabase) {
        this.existsInDatabase = existsInDatabase;
    }
}
