// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Attachment;

/**
 * Prompts a user to upload attachments, like images.
 */
public class AttachmentPrompt extends Prompt<List<Attachment>> {

    /**
     * Initializes a new instance of the {@link AttachmentPrompt} class.
     *
     * @param dialogId   The ID to assign to this prompt.
     *
     * The value of dialogId must be unique within the {@link DialogSet} or
     * {@link ComponentDialog} to which the prompt is added.
     */
    public AttachmentPrompt(String dialogId) {
        this(dialogId, null);
    }

    /**
     * Initializes a new instance of the {@link AttachmentPrompt} class.
     *
     * @param dialogId   The ID to assign to this prompt.
     * @param validator  Optional, a {@link PromptValidator} that contains additional,
     *                   custom validation for this prompt.
     *
     * The value of dialogId must be unique within the {@link DialogSet} or
     * {@link ComponentDialog} to which the prompt is added.
     */
    public AttachmentPrompt(String dialogId, PromptValidator<List<Attachment>> validator) {
        super(dialogId, validator);
    }

    /**
     * Prompts the user for input.
     *
     * @param turnContext  Context for the current turn of conversation with the user.
     * @param state        Contains state for the current instance of the prompt on the
     *                     dialog stack.
     * @param options      A prompt options Object constructed from the options initially
     *                     provided in the call to {@link DialogContext(String, PromptOptions)} .
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
     *                     provided in the call to {@link DialogContext(String, PromptOptions)} .
     *
     * @return   A {@link CompletableFuture} representing the asynchronous operation.
     *
     * If the task is successful, the result describes the result of the recognition attempt.
     */
    @Override
    protected CompletableFuture<PromptRecognizerResult<List<Attachment>>> onRecognize(TurnContext turnContext,
                    Map<String, Object> state, PromptOptions options) {

        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext cannot be null"
            ));
        }

        PromptRecognizerResult<List<Attachment>> result = new PromptRecognizerResult<List<Attachment>>();
        if (turnContext.getActivity().isType(ActivityTypes.MESSAGE)) {
            Activity message = turnContext.getActivity();
            if (message.getAttachments() != null && message.getAttachments().size() > 0) {
                result.setSucceeded(true);
                result.setValue(message.getAttachments());
            }
        }

        return CompletableFuture.completedFuture(result);
    }
}
