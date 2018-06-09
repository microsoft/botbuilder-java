package com.microsoft.bot.builder.core.extensions;


import com.microsoft.bot.builder.core.TurnContext;

public class CustomKeyState extends BotState<CustomState> {
    public static final String PropertyName = "Microsoft.Bot.Builder.Tests.CustomKeyState";

    public CustomKeyState(Storage storage) {
        super(storage, CustomKeyState.PropertyName, (context) -> "CustomKey", CustomState::new);
    }

    public static CustomState Get(TurnContext context) {
        return context.getServices().<CustomState>Get(PropertyName);
    }
}

