package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

public class WasCalledMiddlware implements Middleware {
    boolean called = false;
    public boolean getCalled() {
        return this.called;
    }
    public void setCalled(boolean called) {
        this.called = called;
    }

    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws Exception {
        setCalled(true);
        return next.next();
    }
}
