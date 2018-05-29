package com.microsoft.bot.builder.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class WasCalledMiddlware implements Middleware {
    boolean called = false;
    public boolean getCalled() {
        return this.called;
    }
    public void setCalled(boolean called) {
        this.called = called;
    }

    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws ExecutionException, InterruptedException, ServiceKeyAlreadyRegisteredException {
        setCalled(true);
        return next.next();
    }
}
