package com.microsoft.bot.builder;

import com.microsoft.bot.builder.TurnContext;

import java.util.concurrent.CompletableFuture;

public interface CallOnException {
    <T>  CompletableFuture apply(TurnContext context, T t ) throws Exception;
}
