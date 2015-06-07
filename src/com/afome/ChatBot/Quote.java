package com.afome.ChatBot;

import java.time.LocalDate;

public class Quote {

    private String quote;
    private LocalDate date;

    public Quote(String quote) {
        this.quote = quote;
        date = LocalDate.now();
    }

    public Quote(String quote, LocalDate date) {
        this.quote = quote;
        this.date = date;
    }

    public String getQuote() {
        return quote;
    }

    public LocalDate getDate() {
        return date;
    }
}
