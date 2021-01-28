// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

/**
 * Functional interface for the Middleware pipeline.
 */
@FunctionalInterface
public interface NextDelegate {
    /**
     * The delegate to call to continue the bot middleware pipeline.
     * 
     * @return Future task.
     */
    CompletableFuture<Void> next();
}
