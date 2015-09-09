package com.afome.ChatBot;

public class ChatBotUtils {
    public static String millisToReadableFormat(long millis) {
        /*86400000 millis in a day
         *3600000 millis in an hour
         * 60000 millis in a minute
        */

        StringBuilder timeStringBuilder = new StringBuilder();
        long delta = millis;

        long days = (long) Math.floor(delta / 86400000.0);
        delta -= days * 86400000;

        long hours = (long) Math.floor(delta / 3600000.0);
        delta -= hours * 3600000;

        long minutes = (long) Math.floor(delta / 60000.0);
        delta -= minutes * 60000;

        if (days > 0) {
            timeStringBuilder.append(String.valueOf(days));
            String toAppend = (days == 1) ? " day" : " days";
            timeStringBuilder.append(toAppend);
            if (hours > 0 && minutes > 0) {
                timeStringBuilder.append(", ");
            } else if (hours > 0 || minutes > 0) {
                timeStringBuilder.append(" and ");
            }
        }

        if (hours > 0) {
            timeStringBuilder.append(String.valueOf(hours));
            String toAppend = (hours == 1) ? " hour" : " hours";
            timeStringBuilder.append(toAppend);
            if (minutes > 0) {
                timeStringBuilder.append(" and ");
            }
        }

        if (minutes > 0) {
            timeStringBuilder.append(String.valueOf(minutes));
            String toAppend = (minutes == 1) ? " minute" : " minutes";
            timeStringBuilder.append(toAppend);
        }

        if (days == 0 && hours == 0 && minutes == 0) {
            timeStringBuilder.append("less than one minute");
        }

        return timeStringBuilder.toString();
    }
}
