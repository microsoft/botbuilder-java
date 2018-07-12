package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface MiddlewareCall {
    void requestHandler(TurnContext tc, NextDelegate nd) throws Exception;
}
