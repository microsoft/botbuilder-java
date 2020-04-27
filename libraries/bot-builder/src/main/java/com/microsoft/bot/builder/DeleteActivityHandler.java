// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.ConversationReference;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A method that can participate in delete activity events for the current turn.
 */
@FunctionalInterface
public interface DeleteActivityHandler {
    /**
     * A method that can participate in delete activity events for the current turn.
     *
     * @param context   The context object for the turn.
     * @param reference The conversation containing the activity.
     * @param next      The delegate to call to continue event processing.
     * @return A task that represents the work queued to execute. A handler calls
     *         the {@code next} delegate to pass control to the next registered
     *         handler. If a handler doesn’t call the next delegate, the adapter
     *         does not call any of the subsequent handlers and does not delete the
     *         activity.
     *         <p>
     *         The conversation reference's
     *         {@link ConversationReference#getActivityId} indicates the activity in
     *         the conversation to replace.
     *         </p>
     */
    CompletableFuture<Void> invoke(
        TurnContext context,
        ConversationReference reference,
        Supplier<CompletableFuture<Void>> next
    );
}
