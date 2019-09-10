package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

public class WasCalledMiddleware implements Middleware {
    boolean called = false;
    public boolean getCalled() {
        return this.called;
    }
    public void setCalled(boolean called) {
        this.called = called;
    }

    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
        setCalled(true);
        return next.next();
    }
}
