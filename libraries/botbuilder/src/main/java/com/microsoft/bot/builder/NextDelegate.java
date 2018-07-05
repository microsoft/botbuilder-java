package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface NextDelegate {
     CompletableFuture next() throws Exception;
}
