package com.microsoft.bot.builder.classic.dialogs;

public class BotValue<T> {
    public final T value;
    public BotValue(T value)
    {
        this.value = value;
    }
}
