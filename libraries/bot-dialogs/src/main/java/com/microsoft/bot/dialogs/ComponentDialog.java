// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;

/**
 * A {@link Dialog} that is composed of other dialogs.
 *
 * A component dialog has an inner {@link DialogSet} and {@link DialogContext}
 * ,which provides an inner dialog stack that is hidden from the parent dialog.
 */
public class ComponentDialog extends DialogContainer {

    private String initialDialogId;

    /**
     * The id for the persisted dialog state.
     */
    public static final String PERSISTEDDIALOGSTATE = "dialogs";

    private boolean initialized;

    /**
     * Initializes a new instance of the {@link ComponentDialog} class.
     *
     * @param dialogId The D to assign to the new dialog within the parent dialog
     *                 set.
     */
    public ComponentDialog(String dialogId) {
        super(dialogId);
    }

    /**
     * Called when the dialog is started and pushed onto the parent's dialog stack.
     *
     * @param outerDc The parent {@link DialogContext} for the current turn of
     *                conversation.
     * @param options Optional, initial information to pass to the dialog.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog.
     */
    @Override
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext outerDc, Object options) {

        if (outerDc == null) {
            return Async.completeExceptionally(new IllegalArgumentException("outerDc cannot be null."));
        }

        return ensureInitialized(outerDc).thenCompose(ensureResult -> {
            return this.checkForVersionChange(outerDc).thenCompose(checkResult -> {
                DialogContext innerDc = this.createChildContext(outerDc);
                return onBeginDialog(innerDc, options).thenCompose(turnResult -> {
                    // Check for end of inner dialog
                    if (turnResult.getStatus() != DialogTurnStatus.WAITING) {
                        // Return result to calling dialog
                        return endComponent(outerDc, turnResult.getResult())
                            .thenCompose(result -> CompletableFuture.completedFuture(result));
                    }
                    getTelemetryClient().trackDialogView(getId(), null, null);
                    // Just signal waiting
                    return CompletableFuture.completedFuture(END_OF_TURN);
                });
            });
        });
    }

    /**
     * Called when the dialog is _continued_, where it is the active dialog and the
     * user replies with a new activity.
     *
     * @param outerDc The parent {@link DialogContext} for the current turn of
     *                conversation.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog. The
     *         result may also contain a return value. If this method is *not*
     *         overridden, the component dialog calls the
     *         {@link DialogContext#continueDialog(CancellationToken)} method on its
     *         inner dialog context. If the inner dialog stack is empty, the
     *         component dialog ends, and if a {@link DialogTurnResult#result} is
     *         available, the component dialog uses that as its return value.
     */
    @Override
    public CompletableFuture<DialogTurnResult> continueDialog(DialogContext outerDc) {
        return ensureInitialized(outerDc).thenCompose(ensureResult -> {
            return this.checkForVersionChange(outerDc).thenCompose(checkResult -> {
                // Continue execution of inner dialog
                DialogContext innerDc = this.createChildContext(outerDc);
                return this.onContinueDialog(innerDc).thenCompose(turnResult -> {
                // Check for end of inner dialog
                if (turnResult.getStatus() != DialogTurnStatus.WAITING) {
                    // Return to calling dialog
                    return this.endComponent(outerDc, turnResult.getResult())
                            .thenCompose(result -> CompletableFuture.completedFuture(result));
                }

                // Just signal waiting
                return CompletableFuture.completedFuture(END_OF_TURN);

                });
            });

        });
    }

    /**
     * Called when a child dialog on the parent's dialog stack completed this turn,
     * returning control to this dialog component.
     *
     * @param outerDc The {@link DialogContext} for the current turn of
     *                conversation.
     * @param reason  Reason why the dialog resumed.
     * @param result  Optional, value returned from the dialog that was called. The
     *                type of the value returned is dependent on the child dialog.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     *
     *         If the task is successful, the result indicates whether this dialog
     *         is still active after this dialog turn has been processed. Generally,
     *         the child dialog was started with a call to
     *         BeginDialog(DialogContext, Object) in the parent's context.
     *         However, if the {@link DialogContext#replaceDialog(String, Object)}
     *         method is called, the logical child dialog may be different than the
     *         original. If this method is *not* overridden, the dialog
     *         automatically calls its RepromptDialog(TurnContext,
     *         DialogInstance) when the user replies.
     */
    @Override
    public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext outerDc, DialogReason reason, Object result) {
        return ensureInitialized(outerDc).thenCompose(ensureResult -> {
            return this.checkForVersionChange(outerDc).thenCompose(versionCheckResult -> {
                // Containers are typically leaf nodes on the stack but the dev is free to push
                // other dialogs
                // on top of the stack which will result in the container receiving an
                // unexpected call to
                // dialogResume() when the pushed on dialog ends.
                // To avoid the container prematurely ending we need to implement this method
                // and simply
                // ask our inner dialog stack to re-prompt.
                return repromptDialog(outerDc.getContext(), outerDc.getActiveDialog()).thenCompose(repromptResult -> {
                    return CompletableFuture.completedFuture(END_OF_TURN);
                });
            });
        });
    }

    /**
     * Called when the dialog should re-prompt the user for input.
     *
     * @param turnContext The context Object for this turn.
     * @param instance    State information for this dialog.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     */
    @Override
    public CompletableFuture<Void> repromptDialog(TurnContext turnContext, DialogInstance instance) {
        // Delegate to inner dialog.
        DialogContext innerDc = this.createInnerDc(turnContext, instance);
        return innerDc.repromptDialog().thenCompose(result -> onRepromptDialog(turnContext, instance));
    }

    /**
     * Called when the dialog is ending.
     *
     * @param turnContext The context Object for this turn.
     * @param instance    State information associated with the instance of this
     *                    component dialog on its parent's dialog stack.
     * @param reason      Reason why the dialog ended.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     *
     *         When this method is called from the parent dialog's context, the
     *         component dialog cancels all of the dialogs on its inner dialog stack
     *         before ending.
     */
    @Override
    public CompletableFuture<Void> endDialog(TurnContext turnContext, DialogInstance instance, DialogReason reason) {
        // Forward cancel to inner dialogs
        if (reason == DialogReason.CANCEL_CALLED) {
            DialogContext innerDc = this.createInnerDc(turnContext, instance);
            return innerDc.cancelAllDialogs().thenCompose(result -> onEndDialog(turnContext, instance, reason));
        } else {
            return onEndDialog(turnContext, instance, reason);
        }
    }

    /**
     * Adds a new {@link Dialog} to the component dialog and returns the updated
     * component.
     *
     * @param dialog The dialog to add.
     *
     * @return The {@link ComponentDialog} after the operation is complete.
     *
     *         The added dialog's {@link Dialog#telemetryClient} is set to the
     *         {@link DialogContainer#telemetryClient} of the component dialog.
     */
    public ComponentDialog addDialog(Dialog dialog) {
        this.getDialogs().add(dialog);

        if (this.getInitialDialogId() == null) {
            this.setInitialDialogId(dialog.getId());
        }

        return this;
    }

    /**
     * Creates an inner {@link DialogContext} .
     *
     * @param dc The parent {@link DialogContext} .
     *
     * @return The created Dialog Context.
     */
    @Override
    public DialogContext createChildContext(DialogContext dc) {
        return this.createInnerDc(dc, dc.getActiveDialog());
    }

    /**
     * Ensures the dialog is initialized.
     *
     * @param outerDc The outer {@link DialogContext} .
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     */
    protected CompletableFuture<Void> ensureInitialized(DialogContext outerDc) {
        if (!this.initialized) {
            this.initialized = true;
            return onInitialize(outerDc).thenApply(result -> null);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Initilizes the dialog.
     *
     * @param dc The {@link DialogContext} to initialize.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     */
    protected CompletableFuture<Void> onInitialize(DialogContext dc) {
        if (this.getInitialDialogId() == null) {
            Collection<Dialog> dialogs = getDialogs().getDialogs();
            if (dialogs.size() > 0) {
                this.setInitialDialogId(dialogs.stream().findFirst().get().getId());
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called when the dialog is started and pushed onto the parent's dialog stack.
     *
     * @param innerDc The inner {@link DialogContext} for the current turn of
     *                conversation.
     * @param options Optional, initial information to pass to the dialog.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog. By
     *         default, this calls the
     *         {@link Dialog#beginDialog(DialogContext, Object)} method of the
     *         component dialog's initial dialog, as defined by
     *         InitialDialogId . Override this method in a derived class to
     *         implement interrupt logic.
     */
    protected CompletableFuture<DialogTurnResult> onBeginDialog(DialogContext innerDc, Object options) {
        return innerDc.beginDialog(getInitialDialogId(), options);
    }

    /**
     * Called when the dialog is _continued_, where it is the active dialog and the
     * user replies with a new activity.
     *
     * @param innerDc The inner {@link DialogContext} for the current turn of
     *                conversation.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog. The
     *         result may also contain a return value. By default, this calls the
     *         currently active inner dialog's
     *         {@link Dialog#continueDialog(DialogContext)} method. Override this
     *         method in a derived class to implement interrupt logic.
     */
    protected CompletableFuture<DialogTurnResult> onContinueDialog(DialogContext innerDc) {
        return innerDc.continueDialog();
    }

    /**
     * Called when the dialog is ending.
     *
     * @param context  The context Object for this turn.
     * @param instance State information associated with the inner dialog stack of
     *                 this component dialog.
     * @param reason   Reason why the dialog ended.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     *
     *         Override this method in a derived class to implement any additional
     *         logic that should happen at the component level, after all inner
     *         dialogs have been canceled.
     */
    protected CompletableFuture<Void> onEndDialog(TurnContext context, DialogInstance instance, DialogReason reason) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called when the dialog should re-prompt the user for input.
     *
     * @param turnContext The context Object for this turn.
     * @param instance    State information associated with the inner dialog stack
     *                    of this component dialog.
     *
     * @return A {@link CompletableFuture} representing the hronous operation.
     *
     *         Override this method in a derived class to implement any additional
     *         logic that should happen at the component level, after the re-prompt
     *         operation completes for the inner dialog.
     */
    protected CompletableFuture<Void> onRepromptDialog(TurnContext turnContext, DialogInstance instance) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Ends the component dialog in its parent's context.
     *
     * @param outerDc The parent {@link DialogContext} for the current turn of
     *                conversation.
     * @param result  Optional, value to return from the dialog component to the
     *                parent context.
     *
     * @return A task that represents the work queued to execute.
     *
     *         If the task is successful, the result indicates that the dialog ended
     *         after the turn was processed by the dialog. In general, the parent
     *         context is the dialog or bot turn handler that started the dialog. If
     *         the parent is a dialog, the stack calls the parent's
     *         {@link Dialog#resumeDialog(DialogContext, DialogReason, Object)}
     *         method to return a result to the parent dialog. If the parent dialog
     *         does not implement `ResumeDialog`, then the parent will end, too, and
     *         the result is passed to the next parent context, if one exists. The
     *         returned {@link DialogTurnResult} contains the return value in its
     *         {@link DialogTurnResult#result} property.
     */
    protected CompletableFuture<DialogTurnResult> endComponent(DialogContext outerDc, Object result) {
        return outerDc.endDialog(result);
    }

    private static DialogState buildDialogState(DialogInstance instance) {
        DialogState state;

        if (instance.getState().containsKey(PERSISTEDDIALOGSTATE)) {
            state = (DialogState) instance.getState().get(PERSISTEDDIALOGSTATE);
        } else {
            state = new DialogState();
            instance.getState().put(PERSISTEDDIALOGSTATE, state);
        }

        if (state.getDialogStack() == null) {
            state.setDialogStack(new ArrayList<DialogInstance>());
        }

        return state;
    }

    private DialogContext createInnerDc(DialogContext outerDc, DialogInstance instance) {
        DialogState state = buildDialogState(instance);

        return new DialogContext(this.getDialogs(), outerDc, state);
    }

    // NOTE: You should only call this if you don't have a dc to work with (such as
    // OnResume())
    private DialogContext createInnerDc(TurnContext turnContext, DialogInstance instance) {
        DialogState state = buildDialogState(instance);

        return new DialogContext(this.getDialogs(), turnContext, state);
    }

    /**
     * Gets the id assigned to the initial dialog.
     *
     * @return the InitialDialogId value as a String.
     */
    public String getInitialDialogId() {
        return this.initialDialogId;
    }

    /**
     * Sets the id assigned to the initial dialog.
     *
     * @param withInitialDialogId The InitialDialogId value.
     */
    public void setInitialDialogId(String withInitialDialogId) {
        this.initialDialogId = withInitialDialogId;
    }
}
