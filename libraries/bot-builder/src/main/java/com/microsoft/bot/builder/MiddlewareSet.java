// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Contains an ordered set of {@link Middleware}.
 */
public class MiddlewareSet implements Middleware {
    /**
     * List of {@link Middleware} objects this class manages.
     */
    private final List<Middleware> middlewareList = new ArrayList<>();

    /**
     * Adds a middleware object to the end of the set.
     *
     * @param middleware The middleware to add.
     * @return The updated middleware set.
     */
    public MiddlewareSet use(Middleware middleware) {
        BotAssert.middlewareNotNull(middleware);
        this.middlewareList.add(middleware);
        return this;
    }

    /**
     * Processes an incoming activity.
     *
     * @param turnContext The context object for this turn.
     * @param next        The delegate to call to continue the bot middleware
     *                    pipeline.
     * @return A task that represents the work queued to execute. Middleware calls
     *         the {@code next} delegate to pass control to the next middleware in
     *         the pipeline. If middleware doesn’t call the next delegate, the
     *         adapter does not call any of the subsequent middleware’s request
     *         handlers or the bot’s receive handler, and the pipeline short
     *         circuits.
     *         <p>
     *         The {@code context} provides information about the incoming activity,
     *         and other data needed to process the activity.
     *         </p>
     *         <p>
     *         {@link TurnContext} {@link com.microsoft.bot.schema.Activity}
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
        return receiveActivityInternal(turnContext, null).thenCompose((result) -> next.next());
    }

    /**
     * Processes an activity.
     *
     * @param context  The context object for the turn.
     * @param callback The delegate to call when the set finishes processing the
     *                 activity.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> receiveActivityWithStatus(
        TurnContext context,
        BotCallbackHandler callback
    ) {
        return receiveActivityInternal(context, callback);
    }

    private CompletableFuture<Void> receiveActivityInternal(
        TurnContext context,
        BotCallbackHandler callback
    ) {
        return receiveActivityInternal(context, callback, 0);
    }

    private CompletableFuture<Void> receiveActivityInternal(
        TurnContext context,
        BotCallbackHandler callback,
        int nextMiddlewareIndex
    ) {
        // Check if we're at the end of the middleware list yet
        if (nextMiddlewareIndex == middlewareList.size()) {
            // If all the Middleware ran, the "leading edge" of the tree is now complete.
            // This means it's time to run any developer specified callback.
            // Once this callback is done, the "trailing edge" calls are then completed.
            // This
            // allows code that looks like:
            // Trace.TraceInformation("before");
            // await next();
            // Trace.TraceInformation("after");
            // to run as expected.

            // If a callback was provided invoke it now and return its task, otherwise just
            // return the completed task
            if (callback == null) {
                return CompletableFuture.completedFuture(null);
            } else {
                return callback.invoke(context);
            }
        }

        // Get the next piece of middleware
        Middleware nextMiddleware = middlewareList.get(nextMiddlewareIndex);

        // Execute the next middleware passing a closure that will recurse back into
        // this method at the
        // next piece of middleware as the NextDelegate
        return nextMiddleware.onTurn(
            context, () -> receiveActivityInternal(context, callback, nextMiddlewareIndex + 1)
        );
    }
}
