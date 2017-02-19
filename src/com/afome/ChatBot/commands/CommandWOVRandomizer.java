package com.afome.ChatBot.commands;

import com.afome.ChatBot.*;

import java.io.IOException;
import java.util.HashMap;

public class CommandWOVRandomizer implements Command {
    private ConfigHandler config;
    public CommandWOVRandomizer() throws IOException {
        config = ConfigHandler.getInstance();
    }

    public boolean canUseCommand(UserData user, TwitchChatConnection chatConn) {
        return user.getUserType() == UserType.OPERATOR;
    }
    public boolean isCommand(ChatMessage message) {
        return message.getMessage().toLowerCase().equals("!wov-randomizer");
    }
    public void executeCommand(ChatMessage message, UserData user, TwitchChatConnection chatConn) {
        String outputMessage = "";
        HashMap<String, Double> gameModes = new HashMap<String, Double>();
        gameModes.put("Mortal", 0.3);
        gameModes.put("Demon", 0.3);
        gameModes.put("Doomed Mortal", 0.2);
        gameModes.put("Doomed Demon", 0.2);

        HashMap<String, Double> areas = new HashMap<String, Double>();
        areas.put("Floating Keep 1", 1.0);
        areas.put("Myougi 1", 1.0);
        areas.put("Floating Keep 2", 1.0);
        areas.put("Prgora", 1.0);
        areas.put("Vale 1", 1.0);
        areas.put("Electram", 1.0);
        areas.put("Vale 2", 1.0);
        areas.put("Amythyst", 1.0);
        areas.put("Grotto 1", 1.0);
        areas.put("Dark Annihlator", 1.0);
        areas.put("Grotto 2", 1.0);
        areas.put("Terravine", 1.0);
        areas.put("Underworld 1", 1.0);
        areas.put("Kraterac", 1.0);
        areas.put("Underworld 2", 1.0);
        areas.put("Ancient Constructs", 1.0);
        areas.put("Library", 1.0);
        areas.put("Myougi 2", 1.0);
        areas.put("Chambers", 1.0);
        areas.put("Twin Orcs", 1.0);
        areas.put("Path of Decay", 1.0);
        areas.put("Azurel", 1.0);
        areas.put("Heart of the Baneful", 1.0);
        areas.put("Jehoul 1", 1.0);
        areas.put("Jehoul 2", 1.0);
        areas.put("Boss Rush", 1.0);

        for (String key : areas.keySet()) {
            areas.put(key, 1.0/areas.size());
        }

        HashMap<String, Double> stipulations = new HashMap<String, Double>();
        stipulations.put("1 Death", 1.0);
        stipulations.put("3 Deaths", 1.0);
        stipulations.put("5 Deaths", 1.0);
        stipulations.put("10 Deaths", 1.0);
        stipulations.put("15 Deaths", 1.0);
        stipulations.put("3 Minutes", 1.0);
        stipulations.put("5 Minutes", 1.0);
        stipulations.put("7 Minutes", 1.0);
        stipulations.put("10 Minutes", 1.0);
        stipulations.put("15 Minutes", 1.0);

        for (String key : stipulations.keySet()) {
            stipulations.put(key, 1.0 / stipulations.size());
        }

        HashMap<String, Double> weapons = new HashMap<String, Double>();
        weapons.put("Purified", 1.0);
        weapons.put("Turbo", 1.0);
        weapons.put("Gluttony", 1.0);
        weapons.put("Wrath", 1.0);
        weapons.put("Lust", 1.0);
        weapons.put("Pride", 1.0);
        weapons.put("Envy", 1.0);
        weapons.put("Greed", 1.0);
        weapons.put("Sloth", 1.0);
        weapons.put("PB", 1.0);

        for (String key : weapons.keySet()) {
            weapons.put(key, 1.0 / weapons.size());
        }

        HashMap<String, Double> hellspawn = new HashMap<String, Double>();
        hellspawn.put("enabled", 0.01);
        hellspawn.put("disabled", 0.99);

        HashMap<String, Double> sevenSins = new HashMap<String, Double>();
        sevenSins.put("enabled", 0.01);
        sevenSins.put("disabled", 0.99);

        if (ChatBotUtils.rollValue(sevenSins).equals("enabled")) {
            String difficulty = ChatBotUtils.rollValue(gameModes);
            outputMessage = "Seven Sins | " + difficulty;
        } else if (ChatBotUtils.rollValue(hellspawn).equals("enabled")) {
            String difficulty = ChatBotUtils.rollValue(gameModes);
            outputMessage = "Hellspawn | " + difficulty;
        } else {
            String gameMode = ChatBotUtils.rollValue(gameModes);
            String area = ChatBotUtils.rollValue(areas);
            String weapon = ChatBotUtils.rollValue(weapons);
            String stipulation = ChatBotUtils.rollValue(stipulations);
            outputMessage = gameMode + " | " + area + " | " + weapon + " | " + stipulation;
        }
        chatConn.sendChatMessage(outputMessage);
    }
}
