// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.skills.SkillConversationReference;
import com.microsoft.bot.builder.skills.SkillHandler;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;
import com.microsoft.bot.connector.authentication.SkillValidation;
import com.microsoft.bot.dialogs.memory.DialogStateManager;
import com.microsoft.bot.dialogs.memory.DialogStateManagerConfiguration;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.EndOfConversationCodes;

import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;

/**
 * Base class for all dialogs.
 */
public abstract class Dialog {

    /**
     * A {@link DialogTurnResult} that indicates that the current dialog is still
     * active and waiting for input from the user next turn.
     */
    public static final DialogTurnResult END_OF_TURN = new DialogTurnResult(DialogTurnStatus.WAITING);

    @JsonIgnore
    private BotTelemetryClient telemetryClient;

    @JsonProperty(value = "id")
    private String id;

    /**
     * Initializes a new instance of the Dialog class.
     *
     * @param dialogId The ID to assign to this dialog.
     */
    public Dialog(String dialogId) {
        id = dialogId;
        telemetryClient = new NullBotTelemetryClient();
    }

    /**
     * Gets id for the dialog.
     *
     * @return Id for the dialog.
     */
    public String getId() {
        if (StringUtils.isEmpty(id)) {
            id = onComputeId();
        }
        return id;
    }

    /**
     * Sets id for the dialog.
     *
     * @param withId Id for the dialog.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets the {@link BotTelemetryClient} to use for logging.
     *
     * @return The BotTelemetryClient to use for logging.
     */
    public BotTelemetryClient getTelemetryClient() {
        return telemetryClient;
    }

    /**
     * Sets the {@link BotTelemetryClient} to use for logging.
     *
     * @param withTelemetryClient The BotTelemetryClient to use for logging.
     */
    public void setTelemetryClient(BotTelemetryClient withTelemetryClient) {
        telemetryClient = withTelemetryClient;
    }

    /**
     * Called when the dialog is started and pushed onto the dialog stack.
     *
     * @param dc The {@link DialogContext} for the current turn of conversation.
     * @return If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc) {
        return beginDialog(dc, null);
    }

    /**
     * Called when the dialog is started and pushed onto the dialog stack.
     *
     * @param dc      The {@link DialogContext} for the current turn of
     *                conversation.
     * @param options Initial information to pass to the dialog.
     * @return If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog.
     */
    public abstract CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options);

    /**
     * Called when the dialog is _continued_, where it is the active dialog and the
     * user replies with a new activity.
     *
     * <p>
     * If this method is *not* overridden, the dialog automatically ends when the
     * user replies.
     * </p>
     *
     * @param dc The {@link DialogContext} for the current turn of conversation.
     * @return If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog. The
     *         result may also contain a return value.
     */
    public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {
        // By default just end the current dialog.
        return dc.endDialog(null);
    }

    /**
     * Called when a child dialog completed this turn, returning control to this
     * dialog.
     *
     * <p>
     * Generally, the child dialog was started with a call to
     * {@link #beginDialog(DialogContext, Object)} However, if the
     * {@link DialogContext#replaceDialog(String)} method is called, the logical
     * child dialog may be different than the original.
     * </p>
     *
     * <p>
     * If this method is *not* overridden, the dialog automatically ends when the
     * user replies.
     * </p>
     *
     * @param dc     The dialog context for the current turn of the conversation.
     * @param reason Reason why the dialog resumed.
     * @return If the task is successful, the result indicates whether this dialog
     *         is still active after this dialog turn has been processed.
     */
    public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason) {
        return resumeDialog(dc, reason, null);
    }

    /**
     * Called when a child dialog completed this turn, returning control to this
     * dialog.
     *
     * <p>
     * Generally, the child dialog was started with a call to
     * {@link #beginDialog(DialogContext, Object)} However, if the
     * {@link DialogContext#replaceDialog(String, Object)} method is called, the
     * logical child dialog may be different than the original.
     * </p>
     *
     * <p>
     * If this method is *not* overridden, the dialog automatically ends when the
     * user replies.
     * </p>
     *
     * @param dc     The dialog context for the current turn of the conversation.
     * @param reason Reason why the dialog resumed.
     * @param result Optional, value returned from the dialog that was called. The
     *               type of the value returned is dependent on the child dialog.
     * @return If the task is successful, the result indicates whether this dialog
     *         is still active after this dialog turn has been processed.
     */
    public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason, Object result) {
        // By default just end the current dialog and return result to parent.
        return dc.endDialog(result);
    }

    /**
     * Called when the dialog should re-prompt the user for input.
     *
     * @param turnContext The context object for this turn.
     * @param instance    State information for this dialog.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    public CompletableFuture<Void> repromptDialog(TurnContext turnContext, DialogInstance instance) {
        // No-op by default
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called when the dialog is ending.
     *
     * @param turnContext The context object for this turn.
     * @param instance    State information associated with the instance of this
     *                    dialog on the dialog stack.
     * @param reason      Reason why the dialog ended.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    public CompletableFuture<Void> endDialog(TurnContext turnContext, DialogInstance instance, DialogReason reason) {
        // No-op by default
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Gets a unique String which represents the version of this dialog. If the
     * version changes between turns the dialog system will emit a DialogChanged
     * event.
     *
     * @return Unique String which should only change when dialog has changed in a
     *         way that should restart the dialog.
     */
    @JsonIgnore
    public String getVersion() {
        return id;
    }

    /**
     * Called when an event has been raised, using `DialogContext.emitEvent()`, by
     * either the current dialog or a dialog that the current dialog started.
     *
     * @param dc The dialog context for the current turn of conversation.
     * @param e  The event being raised.
     * @return True if the event is handled by the current dialog and bubbling
     *         should stop.
     */
    public CompletableFuture<Boolean> onDialogEvent(DialogContext dc, DialogEvent e) {
        // Before bubble
        return onPreBubbleEvent(dc, e).thenCompose(handled -> {
            // Bubble as needed
            if (!handled && e.shouldBubble() && dc.getParent() != null) {
                return dc.getParent().emitEvent(e.getName(), e.getValue(), true, false);
            }

            // just pass the handled value to the next stage
            return CompletableFuture.completedFuture(handled);
        }).thenCompose(handled -> {
            if (!handled) {
                // Post bubble
                return onPostBubbleEvent(dc, e);
            }

            return CompletableFuture.completedFuture(handled);
        });
    }

    /**
     * Called before an event is bubbled to its parent.
     *
     * <p>
     * This is a good place to perform interception of an event as returning `true`
     * will prevent any further bubbling of the event to the dialogs parents and
     * will also prevent any child dialogs from performing their default processing.
     * </p>
     *
     * @param dc The dialog context for the current turn of conversation.
     * @param e  The event being raised.
     * @return Whether the event is handled by the current dialog and further
     *         processing should stop.
     */
    protected CompletableFuture<Boolean> onPreBubbleEvent(DialogContext dc, DialogEvent e) {
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Called after an event was bubbled to all parents and wasn't handled.
     *
     * <p>
     * This is a good place to perform default processing logic for an event.
     * Returning `true` will prevent any processing of the event by child dialogs.
     * </p>
     *
     * @param dc The dialog context for the current turn of conversation.
     * @param e  The event being raised.
     * @return Whether the event is handled by the current dialog and further
     *         processing should stop.
     */
    protected CompletableFuture<Boolean> onPostBubbleEvent(DialogContext dc, DialogEvent e) {
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Computes an id for the Dialog.
     *
     * @return The id.
     */
    protected String onComputeId() {
        return this.getClass().getName();
    }

    /**
     * Creates a dialog stack and starts a dialog, pushing it onto the stack.
     *
     * @param dialog      The dialog to start.
     * @param turnContext The context for the current turn of the conversation.
     * @param accessor    The StatePropertyAccessor accessor with which to manage
     *                    the state of the dialog stack.
     * @return A Task representing the asynchronous operation.
     */
    public static CompletableFuture<Void> run(Dialog dialog, TurnContext turnContext,
            StatePropertyAccessor<DialogState> accessor) {
        DialogSet dialogSet = new DialogSet(accessor);
        if (turnContext.getTurnState().get(BotTelemetryClient.class) != null) {
            dialogSet.setTelemetryClient(turnContext.getTurnState().get(BotTelemetryClient.class));
        } else if (dialog.getTelemetryClient() != null) {
            dialogSet.setTelemetryClient(dialog.getTelemetryClient());
        } else {
            dialogSet.setTelemetryClient(new NullBotTelemetryClient());
        }

        dialogSet.add(dialog);

        return dialogSet.createContext(turnContext)
                .thenCompose(dialogContext -> innerRun(turnContext, dialog.getId(), dialogContext, null))
                .thenAccept(dummy -> {
                });
    }

    /**
     * Shared implementation of run with Dialog and DialogManager.
     *
     * @param turnContext        The turnContext.
     * @param dialogId           The Id of the Dialog.
     * @param dialogContext      The DialogContext.
     * @param stateConfiguration The DialogStateManagerConfiguration.
     * @return A DialogTurnResult.
     */
    protected static CompletableFuture<DialogTurnResult> innerRun(TurnContext turnContext, String dialogId,
            DialogContext dialogContext, DialogStateManagerConfiguration stateConfiguration) {
        for (Entry<String, Object> entry : turnContext.getTurnState().getTurnStateServices().entrySet()) {
            dialogContext.getServices().replace(entry.getKey(), entry.getValue());
        }

        DialogStateManager dialogStateManager = new DialogStateManager(dialogContext, stateConfiguration);
        return dialogStateManager.loadAllScopes().thenCompose(result -> {
            dialogContext.getContext().getTurnState().add(dialogStateManager);
            DialogTurnResult dialogTurnResult = null;
            boolean endOfTurn = false;
            while (!endOfTurn) {
                try {
                    dialogTurnResult = continueOrStart(dialogContext, dialogId, turnContext).join();
                    endOfTurn = true;
                } catch (Exception err) {
                    // fire error event, bubbling from the leaf.
                    boolean handled = dialogContext.emitEvent(DialogEvents.ERROR, err, true, true).join();

                    if (!handled) {
                        // error was NOT handled, return a result that signifies that the call was unsuccssfull
                        // (This will trigger the Adapter.OnError handler and end the entire dialog stack)
                        return Async.completeExceptionally(err);
                    }
                }
            }
            return CompletableFuture.completedFuture(dialogTurnResult);
        });

    }

    private static CompletableFuture<DialogTurnResult> continueOrStart(DialogContext dialogContext, String dialogId,
            TurnContext turnContext) {
        if (DialogCommon.isFromParentToSkill(turnContext)) {
            // Handle remote cancellation request from parent.
            if (turnContext.getActivity().getType().equals(ActivityTypes.END_OF_CONVERSATION)) {
                if (dialogContext.getStack().size() == 0) {
                    // No dialogs to cancel, just return.
                    return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.EMPTY));
                }

                DialogContext activeDialogContext = getActiveDialogContext(dialogContext);

                // Send cancellation message to the top dialog in the stack to ensure all the
                // parents
                // are canceled in the right order.
                return activeDialogContext.cancelAllDialogs(true, null, null);
            }

            // Handle a reprompt event sent from the parent.
            if (turnContext.getActivity().getType().equals(ActivityTypes.EVENT)
                    && turnContext.getActivity().getName().equals(DialogEvents.REPROMPT_DIALOG)) {
                if (dialogContext.getStack().size() == 0) {
                    // No dialogs to reprompt, just return.
                    return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.EMPTY));
                }

                return dialogContext.repromptDialog()
                        .thenApply(result -> new DialogTurnResult(DialogTurnStatus.WAITING));
            }
        }
        return dialogContext.continueDialog().thenCompose(result -> {
            if (result.getStatus() == DialogTurnStatus.EMPTY) {
                return dialogContext.beginDialog(dialogId, null).thenCompose(finalResult -> {
                    return processEOC(dialogContext, finalResult, turnContext);
                });
            }
            return processEOC(dialogContext, result, turnContext);
        });
    }

    private static CompletableFuture<DialogTurnResult> processEOC(DialogContext dialogContext, DialogTurnResult result,
            TurnContext turnContext) {
        return sendStateSnapshotTrace(dialogContext).thenCompose(snapshotResult -> {
            if ((result.getStatus() == DialogTurnStatus.COMPLETE
                    || result.getStatus() == DialogTurnStatus.CANCELLED) && sendEoCToParent(turnContext)) {
                EndOfConversationCodes code = result.getStatus() == DialogTurnStatus.COMPLETE
                        ? EndOfConversationCodes.COMPLETED_SUCCESSFULLY
                        : EndOfConversationCodes.USER_CANCELLED;
                Activity activity = new Activity(ActivityTypes.END_OF_CONVERSATION);
                activity.setValue(result.getResult());
                activity.setLocale(turnContext.getActivity().getLocale());
                activity.setCode(code);
                turnContext.sendActivity(activity).join();
            }
            return CompletableFuture.completedFuture(result);
        });
    }

    private static CompletableFuture<Void> sendStateSnapshotTrace(DialogContext dialogContext) {
        String traceLabel = "";
        Object identity = dialogContext.getContext().getTurnState().get(BotAdapter.BOT_IDENTITY_KEY);
        if (identity instanceof ClaimsIdentity) {
            traceLabel = SkillValidation.isSkillClaim(((ClaimsIdentity) identity).claims()) ? "Skill State"
                    : "Bot State";
        }

        // send trace of memory
        JsonNode snapshot = getActiveDialogContext(dialogContext).getState().getMemorySnapshot();
        Activity traceActivity = Activity.createTraceActivity("BotState",
                "https://www.botframework.com/schemas/botState", snapshot, traceLabel);
        return dialogContext.getContext().sendActivity(traceActivity).thenApply(result -> null);
    }

    /**
     * Helper to determine if we should send an EoC to the parent or not.
     *
     * @param turnContext
     * @return
     */
    private static boolean sendEoCToParent(TurnContext turnContext) {

        ClaimsIdentity claimsIdentity = turnContext.getTurnState().get(BotAdapter.BOT_IDENTITY_KEY);

        if (claimsIdentity != null && SkillValidation.isSkillClaim(claimsIdentity.claims())) {
            // EoC Activities returned by skills are bounced back to the bot by
            // SkillHandler.
            // In those cases we will have a SkillConversationReference instance in state.
            SkillConversationReference skillConversationReference = turnContext.getTurnState()
                    .get(SkillHandler.SKILL_CONVERSATION_REFERENCE_KEY);
            if (skillConversationReference != null) {
                // If the skillConversationReference.OAuthScope is for one of the supported
                // channels,
                // we are at the root and we should not send an EoC.
                return skillConversationReference
                        .getOAuthScope() != AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE
                        && skillConversationReference
                                .getOAuthScope() != GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE;
            }
            return true;
        }
        return false;
    }

    // Recursively walk up the DC stack to find the active DC.
    private static DialogContext getActiveDialogContext(DialogContext dialogContext) {
        DialogContext child = dialogContext.getChild();
        if (child == null) {
            return dialogContext;
        }

        return getActiveDialogContext(child);
    }
}
