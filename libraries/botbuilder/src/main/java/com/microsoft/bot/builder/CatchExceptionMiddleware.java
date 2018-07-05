

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * This piece of middleware can be added to allow you to handle exceptions when they are thrown
 * within your bot's code or middleware further down the pipeline. Using this handler you might
 * send an appropriate message to the user to let them know that something has gone wrong.
 * You can specify the type of exception the middleware should catch and this middleware can be added
 * multiple times to allow you to handle different exception types in different ways.
 * @param T 
 * The type of the exception that you want to catch. This can be 'Exception' to
 * catch all or a specific type of exception
 */
public class CatchExceptionMiddleware<T extends Exception> implements Middleware {
    private final CallOnException _handler;
    private final Class<T> _exceptionType;

    public CatchExceptionMiddleware(CallOnException callOnException, Class<T> exceptionType) {
        _handler = callOnException;
        _exceptionType = exceptionType;
    }


    @Override
    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws Exception {

        Class c = _exceptionType.getDeclaringClass();

        try {
            // Continue to route the activity through the pipeline
            // any errors further down the pipeline will be caught by
            // this try / catch
            await(next.next());
        } catch (Exception ex) {

            if (_exceptionType.isInstance(ex))
                // If an error is thrown and the exception is of type T then invoke the handler
                await(_handler.<T>apply(context, (T)ex));
            else
                throw ex;
        }
        return completedFuture(null);
    }

}
