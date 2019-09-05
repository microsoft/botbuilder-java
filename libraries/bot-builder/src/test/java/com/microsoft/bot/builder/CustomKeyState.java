package com.microsoft.bot.builder;


public class CustomKeyState extends BotState<CustomState> {
    public static final String PropertyName = "Microsoft.Bot.Builder.Tests.CustomKeyState";

    public CustomKeyState(Storage storage) {
        super(storage, CustomKeyState.PropertyName, (context) -> "CustomKey", CustomState::new);
    }

    public static CustomState Get(TurnContext context) {
        return context.getTurnState().<CustomState>Get(PropertyName);
    }
}

