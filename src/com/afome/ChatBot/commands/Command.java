package com.afome.ChatBot.commands;

import com.afome.ChatBot.ConfigHandler;
import com.afome.ChatBot.TwitchChatConnection;
import com.afome.ChatBot.UserData;
import com.afome.ChatBot.ChatMessage;

import java.io.IOException;

public interface Command {
    boolean canUseCommand(UserData user, TwitchChatConnection chatConn);
    boolean isCommand(ChatMessage message);
    void executeCommand(ChatMessage message, UserData user, TwitchChatConnection chatConnection);
}
