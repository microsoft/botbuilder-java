package com.microsoft.bot.builder.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@FunctionalInterface
public interface MiddlewareCall {
    CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException, ServiceKeyAlreadyRegisteredException;
}
