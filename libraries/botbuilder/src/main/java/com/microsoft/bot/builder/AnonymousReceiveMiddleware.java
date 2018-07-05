package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

/**
 * Helper class for defining middleware by using a delegate or anonymous method.
 */
public class AnonymousReceiveMiddleware implements Middleware
{
    private MiddlewareCall _toCall;

    /**
     * Creates a middleware object that uses the provided method as its
     * process request handler.
     * @param anonymousMethod The method to use as the middleware's process
     * request handler.
     */
    public AnonymousReceiveMiddleware(MiddlewareCall anonymousMethod)
    {
        if (anonymousMethod == null)
            throw new NullPointerException("MiddlewareCall anonymousMethod");
        else
            _toCall = anonymousMethod;
    }

    /**
     * Uses the method provided in the {@link AnonymousReceiveMiddleware} to
     * process an incoming activity.
     * @param context The context object for this turn.
     * @param next The delegate to call to continue the bot middleware pipeline.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws Exception {
        return _toCall.requestHandler(context, next);
    }

}
