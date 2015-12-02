package com.afome.ChatBot.Earthbound;

import com.afome.ChatBot.ChatBotUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EBVoteHandler {
    private static HashMap<String, EBNamingOption> votes = new HashMap<String, EBNamingOption>();
    private static EBNamingOption curWinningEBNamingOption = null;

    public static void addVote(EBNamingOption vote) {
        if (!votes.containsKey(vote.getUser()) && vote.isValid()) {
            votes.put(vote.getUser(), vote);
        }
    }

    public static EBNamingOption pickWinningUser() {
        if (votes.size() == 0) {
            return null;
        }

        List<String> keys = new ArrayList<String>(votes.keySet());
        String winningUser = keys.get(ChatBotUtils.random.nextInt(keys.size()));
        curWinningEBNamingOption = votes.get(winningUser);
        votes.remove(winningUser);

        return curWinningEBNamingOption;
    }

    public static EBNamingOption getUserVote(String user) {
        if (!votes.containsKey(user)) {
            return null;
        }
        return votes.get(user);
    }

    public static void clearVotes() {
        votes.clear();
    }

    public static EBNamingOption getCurWinningEBNamingOption() {
        return curWinningEBNamingOption;
    }
}
