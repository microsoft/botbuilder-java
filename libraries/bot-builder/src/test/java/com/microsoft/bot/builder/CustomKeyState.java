package com.microsoft.bot.builder;

public class CustomKeyState extends BotState {
    public static final String PROPERTY_NAME = "Microsoft.Bot.Builder.Tests.CustomKeyState";

    public CustomKeyState(Storage storage) {
        super(storage, PROPERTY_NAME);
    }

    @Override
    public String getStorageKey(TurnContext turnContext) {
        return "CustomKey";
    }
}

