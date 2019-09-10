package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

public class CallMeMiddleware implements Middleware {
    private ActionDel callMe;

    public CallMeMiddleware(ActionDel callme) {
        this.callMe = callme;
    }

    @Override
    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
        this.callMe.CallMe();
        return next.next();
    }
}
