package com.microsoft.bot.builder;

@FunctionalInterface
public interface MiddlewareCall {
    void requestHandler(TurnContext tc, NextDelegate nd) throws Exception;
}
