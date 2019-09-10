package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

public interface CallOnException {
    <T>  CompletableFuture<Void> apply(TurnContext context, T t );
}
