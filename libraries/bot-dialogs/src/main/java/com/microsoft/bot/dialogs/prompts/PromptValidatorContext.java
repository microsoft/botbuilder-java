// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.Map;

import com.microsoft.bot.builder.TurnContext;

/**
 * Contains context information for a {@link PromptValidator{T}} .
 *
 * @param <T> Type for this Context
 */

public class PromptValidatorContext<T> {

    private Map<String, Object> state;
    private PromptOptions options;
    private TurnContext context;
    private PromptRecognizerResult<T> recognized;

    /**
     * Create a PromptValidatorContext Instance.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @param recognized  The recognition results from the prompt's recognition
     *                    attempt.
     * @param state       State for the associated prompt instance.
     * @param options     The prompt options used for this recognition attempt.
     */
    public PromptValidatorContext(TurnContext turnContext, PromptRecognizerResult<T> recognized,
            Map<String, Object> state, PromptOptions options) {
        this.context = turnContext;
        this.options = options;
        this.recognized = recognized;
        this.state = state;
    }

    /**
     * Gets state for the associated prompt instance.
     *
     * @return State for the associated prompt instance.
     */
    public Map<String, Object> getState() {
        return this.state;
    }

    /**
     * Gets the {@link PromptOptions} used for this recognition attempt.
     *
     * @return The prompt options used for this recognition attempt.
     */
    public PromptOptions getOptions() {
        return this.options;
    }

    /**
     * Gets the {@link TurnContext} for the current turn of conversation with the
     * user.
     *
     * @return Context for the current turn of conversation with the user.
     */
    public TurnContext getContext() {
        return this.context;
    }

    /**
     * Gets the {@link PromptRecognizerResult{T}} returned from the prompt's
     * recognition attempt.
     *
     * @return The recognition results from the prompt's recognition attempt.
     */
    public PromptRecognizerResult<T> getRecognized() {
        return this.recognized;
    }

    /**
     * Gets the number of times this instance of the prompt has been executed.
     *
     * This count is set when the prompt is added to the dialog stack.
     *
     * @return the attempt count.
     */
    public int getAttemptCount() {
        if (!state.containsKey(Prompt.ATTEMPTCOUNTKEY)) {
            return 0;
        }

        return (int) state.get(Prompt.ATTEMPTCOUNTKEY);
    }
}
