// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for Recognizers.
 */
public interface Recognizer {
    /**
     * Runs an utterance through a recognizer and returns a generic recognizer
     * result.
     *
     * @param turnContext Turn context.
     * @return Analysis of utterance.
     */
    CompletableFuture<RecognizerResult> recognize(TurnContext turnContext);
}
