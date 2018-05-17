package com.microsoft.bot.builder.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@FunctionalInterface
public interface NextDelegate {
     CompletableFuture next() throws ExecutionException, InterruptedException;
}
