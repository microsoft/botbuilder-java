// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * This piece of middleware can be added to allow you to handle exceptions when they are thrown
 * within your bot's code or middleware further down the pipeline. Using this handler you might
 * send an appropriate message to the user to let them know that something has gone wrong.
 * You can specify the type of exception the middleware should catch and this middleware can be added
 * multiple times to allow you to handle different exception types in different ways.
 */
public class CatchExceptionMiddleware<T extends Exception> implements Middleware {
    private final CallOnException _handler;
    private final Class<T> _exceptionType;

    public CatchExceptionMiddleware(CallOnException callOnException, Class<T> exceptionType) {
        _handler = callOnException;
        _exceptionType = exceptionType;
    }

    @Override
    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {

        Class c = _exceptionType.getDeclaringClass();

        // Continue to route the activity through the pipeline
        // any errors further down the pipeline will be caught by
        // this try / catch
        return next.next()
            .exceptionally(exception -> {
                if (_exceptionType.isInstance(exception)) {
                    _handler.apply(context, (T) exception);
                } else {
                    throw new CompletionException(exception);
                }

                return null;
            });
    }
}
