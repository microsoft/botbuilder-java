package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface TurnTask {
    CompletableFuture<Void> invoke(TurnContext context);
}
