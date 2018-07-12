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

    public void OnTurn(TurnContext context, NextDelegate next) throws Exception {
        setCalled(true);
        next.next();
    }
}
