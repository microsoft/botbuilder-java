package com.microsoft.bot.builder.core;

@FunctionalInterface
public interface Func<T> {
    public void invoke(T arg);
}
