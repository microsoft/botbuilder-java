package com.microsoft.bot.builder;

import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.NextDelegate;
import com.microsoft.bot.builder.ServiceKeyAlreadyRegisteredException;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.core.ActionDel;

import java.util.concurrent.CompletableFuture;

public class CallMeMiddlware implements Middleware
{
    private ActionDel _callMe;

    public CallMeMiddlware(ActionDel callme)
    {
        _callMe = callme;
    }

    @Override
    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws Exception, ServiceKeyAlreadyRegisteredException {
        _callMe.CallMe();
        return next.next();
    }
}
