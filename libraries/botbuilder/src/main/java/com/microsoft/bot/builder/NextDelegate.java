package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface NextDelegate {
     void next() throws Exception;
}
