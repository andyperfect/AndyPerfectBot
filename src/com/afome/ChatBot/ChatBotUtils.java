package com.afome.ChatBot;

import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ChatBotUtils {

    public static final String URLRegex = "(https?:\\/\\/)?(www\\.)?([a-zA-Z0-9]+\\.)+([a-z]{3}|[a-z]{2})";
    public static final long FIFTEEN_MINUTES_IN_MILLIS = 900000;
    public static final long TEN_MINUTES_IN_MILLIS = 600000;
    public static final long FIVE_MINUTES_IN_MILLIS = 300000;
    public static final long ONE_MINUTE_IN_MILLIS = 60000;
    public static Random random = new Random();


    public static String millisToReadableFormat(long millis, String format) {
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

        if (format.equals("short")) {
            return shortFormattedTime(days, hours, minutes);
        } else {
            return longFormattedTime(days, hours, minutes);
        }


    }

    private static String longFormattedTime(long days, long hours, long minutes) {
        StringBuilder timeStringBuilder = new StringBuilder();
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

    private static String shortFormattedTime(long days, long hours, long minutes) {
        return String.format("%sd %sh %sm", days, hours, minutes);
    }

    public static String epochToDateString(long epoch) {
        Date date = new Date(epoch);
        final DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
        String formattedDate = format.format(date);

        return formattedDate;
    }

    public static boolean containsLink(String input) {
        Pattern pattern = Pattern.compile(URLRegex);
        Matcher m = pattern.matcher(input);
        return m.find();
    }

    public static String rollValue(HashMap<String, Double> map) {
        double rolledValue = random.nextDouble();

        double curLimit = 0.0;
        for (String key : map.keySet()) {
            curLimit += map.get(key);
            if (rolledValue <= curLimit) {
                return key;
            }
        }
        return null;
    }

    public static long millisToMinutes(long millis) {
        return (long) Math.floor(millis / 60000.0);
    }

    public static String stripHashtagFromChannel(String channel) {
        return channel.replace("#", "");
    }
    public static String addHashTagToChannel(String channel) {
        return "#" + channel;
    }
}
