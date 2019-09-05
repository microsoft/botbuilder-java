package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface MiddlewareCall {
    CompletableFuture<Void> requestHandler(TurnContext tc, NextDelegate nd);
}
