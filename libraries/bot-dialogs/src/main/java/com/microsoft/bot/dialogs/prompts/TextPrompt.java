// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogEvent;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

/**
 * Prompts the user for text input.
 */
public class TextPrompt extends Prompt<String> {

    /**
     * Initializes a new instance of the {@link TextPrompt} class.
     *
     * @param dialogId   The ID to assign to this prompt.
     *
     * The value of {@link dialogId} must be unique within the {@link DialogSet} or
     * {@link ComponentDialog} to which the prompt is added.
     */
    public TextPrompt(String dialogId) {
        this(dialogId, null);
    }

    /**
     * Initializes a new instance of the {@link TextPrompt} class.
     *
     * @param dialogId   The ID to assign to this prompt.
     * @param validator  Optional, a {@link PromptValidator{FoundChoice}} that contains
     *                   additional, custom validation for this prompt.
     *
     * The value of {@link dialogId} must be unique within the {@link DialogSet} or
     * {@link ComponentDialog} to which the prompt is added.
     */
    public TextPrompt(String dialogId, PromptValidator<String> validator) {
        super(dialogId, validator);
    }

    /**
     * Prompts the user for input.
     *
     * @param turnContext  Context for the current turn of conversation with the user.
     * @param state        Contains state for the current instance of the prompt on the
     *                     dialog stack.
     * @param options      A prompt options Object constructed from the options initially
     *                     provided in the call to {@link DialogContext#prompt(String, PromptOptions)} .
     * @param isRetry      true if this is the first time this prompt dialog instance on the
     *                     stack is prompting the user for input; otherwise, false.
     *
     * @return   A {@link CompletableFuture} representing the asynchronous operation.
     */
    @Override
    protected CompletableFuture<Void> onPrompt(TurnContext turnContext, Map<String, Object> state,
                                               PromptOptions options, Boolean isRetry) {
        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext cannot be null"
            ));
        }

        if (options == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "options cannot be null"
            ));
        }

        if (isRetry && options.getRetryPrompt() != null) {
             return turnContext.sendActivity(options.getRetryPrompt()).thenApply(result -> null);
        } else if (options.getPrompt() != null) {
             return turnContext.sendActivity(options.getPrompt()).thenApply(result -> null);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Attempts to recognize the user's input.
     *
     * @param turnContext  Context for the current turn of conversation with the user.
     * @param state        Contains state for the current instance of the prompt on the
     *                     dialog stack.
     * @param options      A prompt options Object constructed from the options initially
     *                     provided in the call to {@link DialogContext#prompt(String, PromptOptions)} .
     *
     * @return   A {@link CompletableFuture} representing the asynchronous operation.
     *
     * If the task is successful, the result describes the result of the recognition attempt.
     */
    @Override
    protected CompletableFuture<PromptRecognizerResult<String>> onRecognize(TurnContext turnContext,
                                                                    Map<String, Object> state, PromptOptions options) {

        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext cannot be null"
            ));
        }

        PromptRecognizerResult<String> result = new PromptRecognizerResult<String>();
        if (turnContext.getActivity().isType(ActivityTypes.MESSAGE)) {
            Activity message = turnContext.getActivity();
            if (message.getText() != null) {
                result.setSucceeded(true);
                result.setValue(message.getText());
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * Called before an event is bubbled to its parent.
     *
     * This is a good place to perform interception of an event as returning `true` will prevent
     * any further bubbling of the event to the dialogs parents and will also prevent any child
     * dialogs from performing their default processing.
     *
     * @param dc  The dialog context for the current turn of conversation.
     * @param e   The event being raised.
     *
     * @return   Whether the event is handled by the current dialog and further processing
     *           should stop.
     */
    @Override
    protected CompletableFuture<Boolean> onPreBubbleEvent(DialogContext dc, DialogEvent e) {
        return  CompletableFuture.completedFuture(false);
    }
}
