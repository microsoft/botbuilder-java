// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.schema.Activity;
import org.apache.commons.lang3.StringUtils;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogInstance;
import com.microsoft.bot.dialogs.DialogReason;
import com.microsoft.bot.dialogs.DialogTurnResult;

/**
 * Defines the core behavior of a prompt dialog that waits for an activity to be
 * received.
 *
 * This prompt requires a validator be passed in and is useful when waiting for
 * non-message activities like an event to be received.The validator can ignore
 * received activities until the expected activity type is received.
 */
public class ActivityPrompt extends Dialog {

    private final String persistedOptions = "options";
    private final String persistedState = "state";

    private final PromptValidator<Activity> validator;

    /**
     * Initializes a new instance of the {@link ActivityPrompt} class. Called from
     * constructors in derived classes to initialize the {@link ActivityPrompt}
     * class.
     *
     * @param dialogId  The ID to assign to this prompt.
     * @param validator A {@link PromptValidator} that contains validation
     *                  for this prompt.
     *
     *                  The value of dialogId must be unique within the
     *                  {@link DialogSet} or {@link ComponentDialog} to which the
     *                  prompt is added.
     */
    public ActivityPrompt(String dialogId, PromptValidator<Activity> validator) {
        super(dialogId);
        if (StringUtils.isEmpty(dialogId)) {
            throw new IllegalArgumentException("dialogId cannot be empty");
        }

        if (validator == null) {
            throw new IllegalArgumentException("validator cannot be null");
        }

        this.validator = validator;
    }

    /**
     * Called when a prompt dialog is pushed onto the dialog stack and is being
     * activated.
     *
     * @param dc      The dialog context for the current turn of the conversation.
     * @param options Optional, additional information to pass to the prompt being
     *                started.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     *
     *         If the task is successful, the result indicates whether the prompt is
     *         still active after the turn has been processed by the prompt.
     */

    @Override
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
        if (dc == null) {
            return Async.completeExceptionally(new IllegalArgumentException("dc cannot be null."));
        }

        if (!(options instanceof PromptOptions)) {
            return Async.completeExceptionally(
                    new IllegalArgumentException("Prompt options are required for Prompt dialogs"));
        }

        // Ensure prompts have input hint set
        // For Java this code isn't necessary as InputHint is an enumeration, so it's
        // can't be not set to something.
        // PromptOptions opt = (PromptOptions) options;
        // if (opt.getPrompt() != null &&
        // StringUtils.isBlank(opt.getPrompt().getInputHint().toString())) {
        // opt.getPrompt().setInputHint(InputHints.EXPECTING_INPUT);
        // }

        // if (opt.getRetryPrompt() != null &&
        // StringUtils.isBlank(opt.getRetryPrompt().getInputHint().toString())) {
        // opt.getRetryPrompt().setInputHint(InputHints.EXPECTING_INPUT);
        // }

        // Initialize prompt state
        Map<String, Object> state = dc.getActiveDialog().getState();
        state.put(persistedOptions, options);

        Map<String, Object> persistedStateMap = new HashMap<String, Object>();
        persistedStateMap.put(Prompt.ATTEMPTCOUNTKEY, 0);
        state.put(persistedState, persistedStateMap);

        // Send initial prompt
        onPrompt(dc.getContext(), (Map<String, Object>) state.get(persistedState),
                (PromptOptions) state.get(persistedOptions), false);

        return CompletableFuture.completedFuture(END_OF_TURN);
    }

    /**
     * Called when a prompt dialog is the active dialog and the user replied with a
     * new activity.
     *
     * @param dc The dialog context for the current turn of conversation.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog. The
     *         prompt generally continues to receive the user's replies until it
     *         accepts the user's reply as valid input for the prompt.
     */
    @Override
    public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {
        if (dc == null) {
            return Async.completeExceptionally(new IllegalArgumentException("dc cannot be null."));
        }

        // Perform base recognition
        DialogInstance instance = dc.getActiveDialog();
        Map<String, Object> state = (Map<String, Object>) instance.getState().get(persistedState);
        PromptOptions options = (PromptOptions) instance.getState().get(persistedOptions);
        return onRecognize(dc.getContext(), state, options).thenCompose(recognized -> {
            state.put(Prompt.ATTEMPTCOUNTKEY, (int) state.get(Prompt.ATTEMPTCOUNTKEY) + 1);
            return validateContext(dc, state, options, recognized).thenCompose(isValid -> {
                // Return recognized value or re-prompt
                if (isValid) {
                    return dc.endDialog(recognized.getValue());
                }

                return onPrompt(dc.getContext(), state, options, true)
                        .thenCompose(result -> CompletableFuture.completedFuture(END_OF_TURN));
            });
        });
    }

    private CompletableFuture<Boolean> validateContext(DialogContext dc, Map<String, Object> state,
            PromptOptions options, PromptRecognizerResult<Activity> recognized) {
        // Validate the return value
        boolean isValid = false;
        if (validator != null) {
            PromptValidatorContext<Activity> promptContext = new PromptValidatorContext<Activity>(dc.getContext(),
                    recognized, state, options);
            return validator.promptValidator(promptContext);
        } else if (recognized.getSucceeded()) {
            isValid = true;
        }
        return CompletableFuture.completedFuture(isValid);
    }

    /**
     * Called when a prompt dialog resumes being the active dialog on the dialog
     * stack, such as when the previous active dialog on the stack completes.
     *
     * @param dc     The dialog context for the current turn of the conversation.
     * @param reason An enum indicating why the dialog resumed.
     * @param result Optional, value returned from the previous dialog on the stack.
     *               The type of the value returned is dependent on the previous
     *               dialog.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog.
     */
    @Override
    public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason, Object result) {
        // Prompts are typically leaf nodes on the stack but the dev is free to push
        // other dialogs
        // on top of the stack which will result in the prompt receiving an unexpected
        // call to
        // dialogResume() when the pushed on dialog ends.
        // To avoid the prompt prematurely ending we need to implement this method and
        // simply re-prompt the user.
        repromptDialog(dc.getContext(), dc.getActiveDialog());
        return CompletableFuture.completedFuture(END_OF_TURN);
    }

    /**
     * Called when a prompt dialog has been requested to re-prompt the user for
     * input.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @param instance    The instance of the dialog on the stack.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     */
    @Override
    public CompletableFuture<Void> repromptDialog(TurnContext turnContext, DialogInstance instance) {
        Map<String, Object> state = (Map<String, Object>) instance.getState().get(persistedState);
        PromptOptions options = (PromptOptions) instance.getState().get(persistedOptions);
        onPrompt(turnContext, state, options, false);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * When overridden in a derived class, prompts the user for input.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @param state       Contains state for the current instance of the prompt on
     *                    the dialog stack.
     * @param options     A prompt options Object constructed from the options
     *                    initially provided in the call to
     *                    {@link DialogContext#prompt(String, PromptOptions)} .
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     */
    protected CompletableFuture<Void> onPrompt(TurnContext turnContext, Map<String, Object> state,
            PromptOptions options) {
        return onPrompt(turnContext, state, options, false).thenApply(result -> null);
    }

    /**
     * When overridden in a derived class, prompts the user for input.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @param state       Contains state for the current instance of the prompt on
     *                    the dialog stack.
     * @param options     A prompt options Object constructed from the options
     *                    initially provided in the call to
     *                    {@link DialogContext#prompt(String, PromptOptions)} .
     * @param isRetry     A {@link Boolean} representing if the prompt is a retry.
     *
     * @return A {@link CompletableFuture} representing the result of the
     *         asynchronous operation.
     */
    protected CompletableFuture<Void> onPrompt(TurnContext turnContext, Map<String, Object> state,
            PromptOptions options, Boolean isRetry) {

        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException("turnContext cannot be null"));
        }

        if (options == null) {
            return Async.completeExceptionally(new IllegalArgumentException("options cannot be null"));
        }

        if (isRetry && options.getRetryPrompt() != null) {
            return turnContext.sendActivity(options.getRetryPrompt()).thenApply(result -> null);
        } else if (options.getPrompt() != null) {
            return turnContext.sendActivity(options.getPrompt()).thenApply(result -> null);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * When overridden in a derived class, attempts to recognize the incoming
     * activity.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @param state       Contains state for the current instance of the prompt on
     *                    the dialog stack.
     * @param options     A prompt options Object constructed from the options
     *                    initially provided in the call to
     *                    {@link DialogContext#prompt(String, PromptOptions)} .
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     *
     *         If the task is successful, the result describes the result of the
     *         recognition attempt.
     */
    protected CompletableFuture<PromptRecognizerResult<Activity>> onRecognize(TurnContext turnContext,
            Map<String, Object> state, PromptOptions options) {
        PromptRecognizerResult<Activity> result = new PromptRecognizerResult<Activity>();
        result.setSucceeded(true);
        result.setValue(turnContext.getActivity());

        return CompletableFuture.completedFuture(result);
    }

}
