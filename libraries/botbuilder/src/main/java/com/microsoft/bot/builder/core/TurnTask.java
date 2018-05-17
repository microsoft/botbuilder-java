package com.microsoft.bot.builder.core;

import com.microsoft.bot.builder.core.TurnContext;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface TurnTask {
    CompletableFuture invoke(TurnContext context);
}
