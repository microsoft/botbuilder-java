// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogEvent;
import com.microsoft.bot.dialogs.DialogEvents;
import com.microsoft.bot.dialogs.DialogInstance;
import com.microsoft.bot.dialogs.DialogReason;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.dialogs.choices.Choice;
import com.microsoft.bot.dialogs.choices.ChoiceFactory;
import com.microsoft.bot.dialogs.choices.ChoiceFactoryOptions;
import com.microsoft.bot.dialogs.choices.ListStyle;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.InputHints;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines the core behavior of prompt dialogs.
 *
 * When the prompt ends, it should return a Object that represents the value
 * that was prompted for. Use
 * {@link com.microsoft.bot.dialogs.DialogSet#add(Dialog)} or
 * {@link com.microsoft.bot.dialogs.ComponentDialog#addDialog(Dialog)} to add a
 * prompt to a dialog set or component dialog, respectively. Use
 * {@link DialogContext#prompt(String, PromptOptions)} or
 * {@link DialogContext#beginDialog(String, Object)} to start the prompt. If you
 * start a prompt from a {@link com.microsoft.bot.dialogs.WaterfallStep} in a
 * {@link com.microsoft.bot.dialogs.WaterfallDialog}, then the prompt result
 * will be available in the next step of the waterfall.
 *
 * @param <T> Type the prompt is created for.
 */
public abstract class Prompt<T> extends Dialog {

    public static final String ATTEMPTCOUNTKEY = "AttemptCount";

    private static final String PERSISTED_OPTIONS = "options";
    private static final String PERSISTED_STATE = "state";
    private final PromptValidator<T> validator;

    /**
     * Initializes a new instance of the {@link Prompt{T}} class. Called from
     * constructors in derived classes to initialize the {@link Prompt{T}} class.
     *
     * @param dialogId  The ID to assign to this prompt.
     * @param validator Optional, a {@link PromptValidator{T}} that contains
     *                  additional, custom validation for this prompt.
     *
     *                  The value of dialogId must be unique within the
     *                  {@link com.microsoft.bot.dialogs.DialogSet} or
     *                  {@link com.microsoft.bot.dialogs.ComponentDialog} to which
     *                  the prompt is added.
     */
    public Prompt(String dialogId, PromptValidator<T> validator) {
        super(dialogId);
        if (StringUtils.isBlank(dialogId)) {
            throw new IllegalArgumentException("dialogId cannot be null");
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
        PromptOptions opt = (PromptOptions) options;

        if (opt.getPrompt() != null && opt.getPrompt().getInputHint() == null) {
            opt.getPrompt().setInputHint(InputHints.EXPECTING_INPUT);
        }

        if (opt.getRetryPrompt() != null && opt.getRetryPrompt().getInputHint() == null) {
            opt.getRetryPrompt().setInputHint(InputHints.EXPECTING_INPUT);
        }

        // Initialize prompt state
        Map<String, Object> state = dc.getActiveDialog().getState();
        state.put(PERSISTED_OPTIONS, opt);

        HashMap<String, Object> pState = new HashMap<String, Object>();
        pState.put(ATTEMPTCOUNTKEY, 0);
        state.put(PERSISTED_STATE, pState);

        // Send initial prompt
        onPrompt(dc.getContext(), (Map<String, Object>) state.get(PERSISTED_STATE),
                (PromptOptions) state.get(PERSISTED_OPTIONS), false);
        return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
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

        // Don't do anything for non-message activities
        if (!dc.getContext().getActivity().isType(ActivityTypes.MESSAGE)) {
            return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
        }

        // Perform base recognition
        DialogInstance instance = dc.getActiveDialog();
        Map<String, Object> state = (Map<String, Object>) instance.getState().get(PERSISTED_STATE);
        PromptOptions options = (PromptOptions) instance.getState().get(PERSISTED_OPTIONS);
        return onRecognize(dc.getContext(), state, options).thenCompose(recognized -> {
            state.put(ATTEMPTCOUNTKEY, (int) state.get(ATTEMPTCOUNTKEY) + 1);

            // Validate the return value
            return validateContext(dc, state, options, recognized).thenCompose(isValid -> {
                // Return recognized value or re-prompt
                if (isValid) {
                    return dc.endDialog(recognized.getValue());
                }

                if (!dc.getContext().getResponded()) {
                    return onPrompt(dc.getContext(), state, options, true).thenApply(result -> Dialog.END_OF_TURN);
                }

                return CompletableFuture.completedFuture(Dialog.END_OF_TURN);
            });
        });
    }

    private CompletableFuture<Boolean> validateContext(DialogContext dc, Map<String, Object> state,
            PromptOptions options, PromptRecognizerResult<T> recognized) {
        Boolean isValid = false;
        if (validator != null) {
            PromptValidatorContext<T> promptContext = new PromptValidatorContext<T>(dc.getContext(), recognized, state,
                    options);
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
        return repromptDialog(dc.getContext(), dc.getActiveDialog()).thenApply(finalResult -> Dialog.END_OF_TURN);
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
        Map<String, Object> state = (Map<String, Object>) instance.getState().get(PERSISTED_STATE);
        PromptOptions options = (PromptOptions) instance.getState().get(PERSISTED_OPTIONS);
        return onPrompt(turnContext, state, options, false).thenApply(result -> null);
    }

    /**
     * Called before an event is bubbled to its parent.
     *
     * This is a good place to perform interception of an event as returning `true`
     * will prevent any further bubbling of the event to the dialogs parents and
     * will also prevent any child dialogs from performing their default processing.
     *
     * @param dc The dialog context for the current turn of conversation.
     * @param e  The event being raised.
     *
     * @return Whether the event is handled by the current dialog and further
     *         processing should stop.
     */
    @Override
    protected CompletableFuture<Boolean> onPreBubbleEvent(DialogContext dc, DialogEvent e) {
        if (e.getName().equals(DialogEvents.ACTIVITY_RECEIVED)
                && dc.getContext().getActivity().isType(ActivityTypes.MESSAGE)) {
            // Perform base recognition
            Map<String, Object> state = dc.getActiveDialog().getState();
            return onRecognize(dc.getContext(), (Map<String, Object>) state.get(PERSISTED_STATE),
                    (PromptOptions) state.get(PERSISTED_OPTIONS))
                            .thenCompose(recognized -> CompletableFuture.completedFuture(recognized.getSucceeded()));
        }

        return CompletableFuture.completedFuture(false);
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
     * @param isRetry     true if this is the first time this prompt dialog instance
     *                    is on the stack is prompting the user for input;
     *                    otherwise, false. Determines whether
     *                    {@link PromptOptions#getPrompt()} or
     *                    {@link PromptOptions#getRetryPrompt()} should be used.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     */
    protected abstract CompletableFuture<Void> onPrompt(TurnContext turnContext, Map<String, Object> state,
            PromptOptions options, Boolean isRetry);

    /**
     * When overridden in a derived class, attempts to recognize the user's input.
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
    protected abstract CompletableFuture<PromptRecognizerResult<T>> onRecognize(TurnContext turnContext,
            Map<String, Object> state, PromptOptions options);

    /**
     * When overridden in a derived class, appends choices to the activity when the
     * user is prompted for input.
     *
     * @param prompt    The activity to append the choices to.
     * @param channelId The ID of the user's channel.
     * @param choices   The choices to append.
     * @param style     Indicates how the choices should be presented to the user.
     * @param options   The formatting options to use when presenting the choices.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     *
     *         If the task is successful, the result contains the updated activity.
     */
    protected Activity appendChoices(Activity prompt, String channelId, List<Choice> choices, ListStyle style,
            ChoiceFactoryOptions options) {
        // Get base prompt text (if any)
        String text = "";
        if (prompt != null && prompt.getText() != null && StringUtils.isNotBlank(prompt.getText())) {
            text = prompt.getText();
        }

        // Create temporary msg
        Activity msg;
        switch (style) {
        case INLINE:
            msg = ChoiceFactory.inline(choices, text, null, options);
            break;

        case LIST:
            msg = ChoiceFactory.list(choices, text, null, options);
            break;

        case SUGGESTED_ACTION:
            msg = ChoiceFactory.suggestedAction(choices, text);
            break;

        case HEROCARD:
            msg = ChoiceFactory.heroCard(choices, text);
            break;

        case NONE:
            msg = Activity.createMessageActivity();
            msg.setText(text);
            break;

        default:
            msg = ChoiceFactory.forChannel(channelId, choices, text, null, options);
            break;
        }

        // Update prompt with text, actions and attachments
        if (prompt != null) {
            // clone the prompt the set in the options (note ActivityEx has Properties so
            // this is the safest mechanism)
            // prompt =
            // JsonConvert.DeserializeObject<Activity>(JsonConvert.SerializeObject(prompt));
            prompt = Activity.clone(prompt);

            prompt.setText(msg.getText());

            if (msg.getSuggestedActions() != null && msg.getSuggestedActions().getActions() != null
                    && msg.getSuggestedActions().getActions().size() > 0) {
                prompt.setSuggestedActions(msg.getSuggestedActions());
            }

            if (msg.getAttachments() != null && msg.getAttachments().size() > 0) {
                if (prompt.getAttachments() == null) {
                    prompt.setAttachments(msg.getAttachments());
                } else {
                    List<Attachment> allAttachments = prompt.getAttachments();
                    prompt.getAttachments().addAll(msg.getAttachments());
                    prompt.setAttachments(allAttachments);
                }
            }

            return prompt;
        }

        msg.setInputHint(InputHints.EXPECTING_INPUT);
        return msg;
    }
}
