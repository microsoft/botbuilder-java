package com.microsoft.bot.builder.core.extensions;

import com.microsoft.bot.builder.core.TurnContext;

import java.util.concurrent.CompletableFuture;

public interface CallOnException {
    <T>  CompletableFuture apply(TurnContext context, T t ) throws Exception;
}
