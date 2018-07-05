package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

/// <summary>
/// Helper class for defining middleware by using a delegate or anonymous method.
/// </summary>
public class AnonymousReceiveMiddleware implements Middleware
{
    private MiddlewareCall _toCall;

    /// <summary>
    /// Creates a middleware object that uses the provided method as its
    /// process request handler.
    /// </summary>
    /// <param name="anonymousMethod">The method to use as the middleware's process
    /// request handler.</param>
    public AnonymousReceiveMiddleware(MiddlewareCall anonymousMethod)
    {
        if (anonymousMethod == null)
            throw new NullPointerException("MiddlewareCall anonymousMethod");
        else
            _toCall = anonymousMethod;
    }

    /// <summary>
    /// Uses the method provided in the <see cref="AnonymousReceiveMiddleware"/> to
    /// process an incoming activity.
    /// </summary>
    /// <param name="context">The context object for this turn.</param>
    /// <param name="next">The delegate to call to continue the bot middleware pipeline.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws Exception {
        return _toCall.requestHandler(context, next);
    }

}
