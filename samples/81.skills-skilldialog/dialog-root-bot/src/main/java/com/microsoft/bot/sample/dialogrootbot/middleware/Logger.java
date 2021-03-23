package com.microsoft.bot.sample.dialogrootbot.middleware;

import java.util.concurrent.CompletableFuture;

public abstract class Logger {

    public CompletableFuture<Void> logEntry(String entryToLog) {
        System.out.println(entryToLog);
        return CompletableFuture.completedFuture(null);
    }
}
