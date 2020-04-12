// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Interface which defines methods for how you can get data from a property
 * source such as BotState.
 * 
 * @param <T> type of the property.
 */
public interface StatePropertyAccessor<T> extends StatePropertyInfo {
    /**
     * Get the property value from the source.
     * 
     * @param turnContext TurnContext.
     * @return A task representing the result of the asynchronous operation.
     */
    default CompletableFuture<T> get(TurnContext turnContext) {
        return get(turnContext, null);
    }

    /**
     * Get the property value from the source.
     * 
     * @param turnContext         TurnContext.
     * @param defaultValueFactory Function which defines the property value to be
     *                            returned if no value has been set.
     * @return A task representing the result of the asynchronous operation.
     */
    CompletableFuture<T> get(TurnContext turnContext, Supplier<T> defaultValueFactory);

    /**
     * Delete the property from the source.
     * 
     * @param turnContext TurnContext.
     * @return A task representing the result of the asynchronous operation.
     */
    CompletableFuture<Void> delete(TurnContext turnContext);

    /**
     * Set the property value on the source.
     * 
     * @param turnContext TurnContext.
     * @param value       The value to set.
     * @return A task representing the result of the asynchronous operation.
     */
    CompletableFuture<Void> set(TurnContext turnContext, T value);
}
