// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.concurrent.CompletableFuture;

/**
 * The interface definition for custom prompt validators. Implement this
 * function to add custom validation to a prompt.
 *
 * @param <T> Type the PromptValidator is created for.
 */

public interface PromptValidator<T> {

    /**
     * The delegate definition for custom prompt validators. Implement this function to add custom
     * validation to a prompt.
     *
     * @param promptContext  The prompt validation context.
     *
     * @return   A {@link CompletableFuture} of bool representing the asynchronous operation
     *           indicating validation success or failure.
     */
    CompletableFuture<Boolean> promptValidator(PromptValidatorContext<T> promptContext);

}
