// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ResourceResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A method that can participate in send activity events for the current turn.
 */
@FunctionalInterface
public interface SendActivitiesHandler {
    /**
     * A method that can participate in send activity events for the current turn.
     *
     * @param context    The context object for the turn.
     * @param activities The activities to send.
     * @param next       The delegate to call to continue event processing.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<ResourceResponse[]> invoke(
        TurnContext context,
        List<Activity> activities,
        Supplier<CompletableFuture<ResourceResponse[]>> next
    );
}
