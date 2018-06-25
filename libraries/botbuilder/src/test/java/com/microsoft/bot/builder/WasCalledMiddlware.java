package com.microsoft.bot.builder;

import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.NextDelegate;
import com.microsoft.bot.builder.ServiceKeyAlreadyRegisteredException;
import com.microsoft.bot.builder.TurnContext;

import java.util.concurrent.CompletableFuture;

public class WasCalledMiddlware implements Middleware {
    boolean called = false;
    public boolean getCalled() {
        return this.called;
    }
    public void setCalled(boolean called) {
        this.called = called;
    }

    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws Exception, ServiceKeyAlreadyRegisteredException {
        setCalled(true);
        return next.next();
    }
}
