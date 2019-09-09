// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Contains an ordered set of {@link Middleware}.
 */
public class MiddlewareSet implements Middleware {
    public NextDelegate Next;
    private final ArrayList<Middleware> middleware = new ArrayList<>();

    /**
     * Adds a middleware object to the end of the set.
     *
     * @param middleware The middleware to add.
     * @return The updated middleware set.
     */
    public MiddlewareSet use(Middleware middleware) {
        BotAssert.middlewareNotNull(middleware);
        this.middleware.add(middleware);
        return this;
    }

    @Override
    public CompletableFuture<Void> onTurnAsync(TurnContext context, NextDelegate next) {
        return receiveActivityInternal((TurnContextImpl) context, null)
            .thenCompose((result) -> next.next());
    }

    /**
     * Processes an activity.
     *
     * @param context The context object for the turn.
     * @param callback The delegate to call when the set finishes processing the activity.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> receiveActivityWithStatusAsync(TurnContext context, BotCallbackHandler callback) {
        return receiveActivityInternal(context, callback);
    }

    private CompletableFuture<Void> receiveActivityInternal(TurnContext context, BotCallbackHandler callback) {
        return receiveActivityInternal(context, callback, 0);
    }

    private CompletableFuture<Void> receiveActivityInternal(TurnContext context,
                                                            BotCallbackHandler callback,
                                                            int nextMiddlewareIndex) {
        // Check if we're at the end of the middleware list yet
        if (nextMiddlewareIndex == middleware.size()) {
            // If all the Middlware ran, the "leading edge" of the tree is now complete.
            // This means it's time to run any developer specified callback.
            // Once this callback is done, the "trailing edge" calls are then completed. This
            // allows code that looks like:
            //      Trace.TraceInformation("before");
            //      await next();
            //      Trace.TraceInformation("after");
            // to run as expected.

            // If a callback was provided invoke it now and return its task, otherwise just return the completed task
            if (callback == null) {
                return CompletableFuture.completedFuture(null);
            } else {
                return callback.invoke(context);
            }
        }

        // Get the next piece of middleware
        Middleware nextMiddleware = middleware.get(nextMiddlewareIndex);

        // Execute the next middleware passing a closure that will recurse back into this method at the
        // next piece of middlware as the NextDelegate
        return nextMiddleware.onTurnAsync(context, () ->
            receiveActivityInternal(context, callback, nextMiddlewareIndex + 1));
    }
}
