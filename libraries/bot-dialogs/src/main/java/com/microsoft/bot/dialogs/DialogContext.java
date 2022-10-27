// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextStateCollection;
import com.microsoft.bot.dialogs.memory.DialogStateManager;
import com.microsoft.bot.dialogs.prompts.PromptOptions;
import com.microsoft.bot.connector.Async;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;

/**
 *  Provides context for the current state of the dialog stack.
 */
public class DialogContext {
    private DialogSet dialogs;
    private TurnContext context;
    private List<DialogInstance> stack;
    private DialogContext parent;
    private DialogStateManager state;
    private TurnContextStateCollection services;

    /**
     * Initializes a new instance of the DialogContext class from the turn context.
     * @param withDialogs The dialog set to create the dialog context for.
     * @param withTurnContext The current turn context.
     * @param withState The state property from which to retrieve the dialog context.
     */
    public DialogContext(DialogSet withDialogs, TurnContext withTurnContext, DialogState withState) {
        if (withDialogs == null) {
            throw new IllegalArgumentException("DialogContext, DialogSet is required.");
        }

        if (withTurnContext == null) {
            throw new IllegalArgumentException("DialogContext, TurnContext is required.");
        }

        init(withDialogs, withTurnContext, withState);
    }

    /**
     * Initializes a new instance of the DialogContext class from the turn context.
     * @param withDialogs The dialog set to create the dialog context for.
     * @param withParentDialogContext Parent dialog context.
     * @param withState Current dialog state.
     */
    public DialogContext(
        DialogSet withDialogs,
        DialogContext withParentDialogContext,
        DialogState withState
    ) {
        if (withParentDialogContext == null) {
            throw new IllegalArgumentException("DialogContext, DialogContext is required.");
        }

        init(withDialogs, withParentDialogContext.getContext(), withState);

        parent = withParentDialogContext;

        // copy parent services into this DialogContext.
        services.copy(getParent().getServices());
    }


    /**
     * @param withDialogs
     * @param withTurnContext
     * @param withState
     */
    private void init(DialogSet withDialogs, TurnContext withTurnContext, DialogState withState) {
        dialogs = withDialogs;
        context = withTurnContext;
        stack = withState.getDialogStack();
        state = new DialogStateManager(this, null);
        services = new TurnContextStateCollection();

        ObjectPath.setPathValue(context.getTurnState(), TurnPath.ACTIVITY, context.getActivity());
    }

    /**
     * Gets the set of dialogs which are active for the current dialog container.
     * @return The set of dialogs which are active for the current dialog container.
     */
    public DialogSet getDialogs() {
        return dialogs;
    }

    /**
     * Gets the context for the current turn of conversation.
     * @return The context for the current turn of conversation.
     */
    public TurnContext getContext() {
        return context;
    }

    /**
     * Gets the current dialog stack.
     * @return The current dialog stack.
     */
    public List<DialogInstance> getStack() {
        return stack;
    }

    /**
     * Gets the parent DialogContext, if any. Used when searching for the ID of a dialog to start.
     * @return The parent "DialogContext, if any. Used when searching for the ID of a dialog to start.
     */
    public DialogContext getParent() {
        return parent;
    }

    /**
     * Set the parent DialogContext.
     * @param withDialogContext The DialogContext to set the parent to.
     */
    public void setParent(DialogContext withDialogContext) {
        parent = withDialogContext;
    }

    /**
     * Gets dialog context for child if there is an active child.
     * @return Dialog context for child if there is an active child.
     */
    public DialogContext getChild() {
        DialogInstance instance = getActiveDialog();
        if (instance != null) {
            Dialog dialog = findDialog(instance.getId());
            if (dialog instanceof DialogContainer) {
                return ((DialogContainer) dialog).createChildContext(this);
            }
        }

        return null;
    }

    /**
     * Gets the cached instance of the active dialog on the top of the stack or null if the stack is empty.
     * @return The cached instance of the active dialog on the top of the stack or null if the stack is empty.
     */
    public DialogInstance getActiveDialog() {
        if (stack.size() > 0) {
            return stack.get(0);
        }

        return null;
    }

    /**
     * Gets or sets the DialogStateManager which manages view of all memory scopes.
     * @return DialogStateManager with unified memory view of all memory scopes.
     */
    public DialogStateManager getState() {
        return state;
    }

    /**
     * Gets the services collection which is contextual to this dialog context.
     * @return Services collection.
     */
    public TurnContextStateCollection getServices() {
        return services;
    }

    /**
     * Starts a new dialog and pushes it onto the dialog stack.
     * @param dialogId ID of the dialog to start.
     * @return If the task is successful, the result indicates whether the dialog is still
     * active after the turn has been processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> beginDialog(String dialogId) {
        return beginDialog(dialogId, null);
    }

    /**
     * Starts a new dialog and pushes it onto the dialog stack.
     * @param dialogId ID of the dialog to start.
     * @param options Optional, information to pass to the dialog being started.
     * @return If the task is successful, the result indicates whether the dialog is still
     * active after the turn has been processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> beginDialog(String dialogId, Object options) {
        if (StringUtils.isEmpty(dialogId)) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "DialogContext.beginDialog, dialogId is required"
            ));
        }

        // Look up dialog
        Dialog dialog = findDialog(dialogId);
        if (dialog == null) {
            return Async.completeExceptionally(new Exception(String.format(
                "DialogContext.beginDialog(): A dialog with an id of '%s' wasn't found."
                + " The dialog must be included in the current or parent DialogSet."
                + " For example, if subclassing a ComponentDialog you can call AddDialog()"
                + " within your constructor.",
                dialogId
            )));
        }

        // Push new instance onto stack
        DialogInstance instance = new DialogInstance(dialogId, new HashMap<>());
        stack.add(0, instance);

        // Call dialog's Begin() method
        return dialog.beginDialog(this, options);
    }

    /**
     * Helper function to simplify formatting the options for calling a prompt dialog. This helper will
     * take an PromptOptions argument and then call {@link #beginDialog(String, Object)}
     *
     * @param dialogId ID of the prompt dialog to start.
     * @param options Information to pass to the prompt dialog being started.
     * @return If the task is successful, the result indicates whether the dialog is still
     * active after the turn has been processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> prompt(String dialogId, PromptOptions options) {
        if (StringUtils.isEmpty(dialogId)) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "DialogContext.prompt, dialogId is required"
            ));
        }

        if (options == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "DialogContext.prompt, PromptOptions is required"
            ));
        }

        return beginDialog(dialogId, options);
    }

    /**
     * Continues execution of the active dialog, if there is one, by passing the current
     * DialogContext to the active dialog's {@link Dialog#continueDialog(DialogContext)}
     * method.
     *
     * @return If the task is successful, the result indicates whether the dialog is still
     * active after the turn has been processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> continueDialog() {
        return Async.tryCompletable(() -> {
            // if we are continuing and haven't emitted the activityReceived event, emit it
            // NOTE: This is backward compatible way for activity received to be fired even if
            // you have legacy dialog loop
            if (!getContext().getTurnState().containsKey("activityReceivedEmitted")) {
                getContext().getTurnState().replace("activityReceivedEmitted", true);

                // Dispatch "activityReceived" event
                // - This will queue up any interruptions.
                emitEvent(DialogEvents.ACTIVITY_RECEIVED, getContext().getActivity(), true,
                    true
                );
            }
            return CompletableFuture.completedFuture(null);
        })
        .thenCompose(v -> {
            if (getActiveDialog() != null) {
                // Lookup dialog
                Dialog dialog = this.findDialog(getActiveDialog().getId());
                if (dialog == null) {
                    throw new IllegalStateException(String.format(
                        "Failed to continue dialog. A dialog with id %s could not be found.",
                        getActiveDialog().getId()
                    ));
                }

                // Continue dialog execution
                return dialog.continueDialog(this);
            }

            return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.EMPTY));
        });
    }

    /**
     * Helper method that supplies a null result to {@link #endDialog(Object)}.
     *
     * @return If the task is successful, the result indicates that the dialog ended after the
     * turn was processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> endDialog() {
        return endDialog(null);
    }

    /**
     * Ends a dialog by popping it off the stack and returns an optional result to the dialog's
     * parent. The parent dialog is the dialog the started the on being ended via a call to
     * either {@link #beginDialog(String, Object)} or {@link #prompt(String, PromptOptions)}. The
     * parent dialog will have its {@link Dialog#resumeDialog(DialogContext, DialogReason, Object)}
     * method invoked with any returned result. If the parent dialog hasn't implemented a
     * {@link Dialog#resumeDialog(DialogContext dc, DialogReason reason)} method, then it will be
     * automatically ended as well and the result passed to its parent. If there are no more parent
     * dialogs on the stack then processing of the turn will end.
     *
     * @param result Optional, result to pass to the parent context.
     * @return If the task is successful, the result indicates that the dialog ended after the
     * turn was processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> endDialog(Object result) {
        // End the active dialog
        return endActiveDialog(DialogReason.END_CALLED, result)
            .thenCompose(v -> {
                // Resume parent dialog
                if (getActiveDialog() != null) {
                    // Lookup dialog
                    Dialog dialog = this.findDialog(getActiveDialog().getId());
                    if (dialog == null) {
                        throw new IllegalStateException(String.format(
                            "DialogContext.endDialog(): Can't resume previous dialog. A dialog "
                                + "with an id of '%s' wasn't found.",
                            getActiveDialog().getId())
                        );
                    }

                    // Return result to previous dialog
                    return dialog.resumeDialog(this, DialogReason.END_CALLED, result);
                }

                return CompletableFuture.completedFuture(
                    new DialogTurnResult(DialogTurnStatus.COMPLETE, result)
                );
            });
    }

    /**
     * Helper method for {@link #cancelAllDialogs(boolean, String, Object)} that does not cancel
     * parent dialogs or pass and event.
     *
     * @return If the task is successful, the result indicates that dialogs were canceled after the
     * turn was processed by the dialog or that the stack was already empty.
     */
    public CompletableFuture<DialogTurnResult> cancelAllDialogs() {
        return cancelAllDialogs(false, null, null);
    }

    /**
     * Deletes any existing dialog stack thus canceling all dialogs on the stack.
     *
     * <p>In general, the parent context is the dialog or bot turn handler that started the dialog.
     * If the parent is a dialog, the stack calls the parent's
     * {@link Dialog#resumeDialog(DialogContext, DialogReason, Object)}
     * method to return a result to the parent dialog. If the parent dialog does not implement
     * {@link Dialog#resumeDialog}, then the parent will end, too, and the result is passed to the next
     * parent context.</p>
     *
     * @param cancelParents If true the cancellation will bubble up through any parent dialogs as well.
     * @param eventName The event.  If null, {@link DialogEvents#CANCEL_DIALOG} is used.
     * @param eventValue The event value.  Can be null.
     * @return If the task is successful, the result indicates that dialogs were canceled after the
     * turn was processed by the dialog or that the stack was already empty.
     */
    public CompletableFuture<DialogTurnResult> cancelAllDialogs(
        boolean cancelParents,
        String eventName,
        Object eventValue
    ) {
        eventName = eventName != null ? eventName : DialogEvents.CANCEL_DIALOG;

        if (!stack.isEmpty() || getParent() != null) {
            // Cancel all local and parent dialogs while checking for interception
            boolean notify = false;
            DialogContext dialogContext = this;

            while (dialogContext != null) {
                if (!dialogContext.stack.isEmpty()) {
                    // Check to see if the dialog wants to handle the event
                    if (notify) {
                        Boolean eventHandled = dialogContext.emitEvent(eventName, eventValue, false, false).join();
                        if (eventHandled) {
                            break;
                        }
                    }

                    // End the active dialog
                    dialogContext.endActiveDialog(DialogReason.CANCEL_CALLED).join();
                } else {
                    dialogContext = cancelParents ? dialogContext.getParent() : null;
                }

                notify = true;
            }

            return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.CANCELLED));
        } else {
            // Stack was empty and no parent
            return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.EMPTY));
        }
    }

    /**
     * Helper method for {@link #replaceDialog(String, Object)} that passes null for options.
     * @param dialogId ID of the new dialog to start.
     * @return If the task is successful, the result indicates whether the dialog is still
     * active after the turn has been processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> replaceDialog(String dialogId) {
        return replaceDialog(dialogId, null);
    }

    /**
     * Starts a new dialog and replaces on the stack the currently active dialog with the new one.
     * This is particularly useful for creating loops or redirecting to another dialog.
     * @param dialogId ID of the new dialog to start.
     * @param options Optional, information to pass to the dialog being started.
     * @return If the task is successful, the result indicates whether the dialog is still
     * active after the turn has been processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> replaceDialog(String dialogId, Object options) {
        // End the current dialog and giving the reason.
        return endActiveDialog(DialogReason.REPLACE_CALLED)
            .thenCompose(v -> {
                ObjectPath.setPathValue(getContext().getTurnState(), "turn.__repeatDialogId", dialogId);

                // Start replacement dialog
                return beginDialog(dialogId, options);
            });
    }

    /**
     * Calls the currently active dialog's {@link Dialog#repromptDialog(TurnContext, DialogInstance)}
     * method. Used with dialogs that implement a re-prompt behavior.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> repromptDialog() {
        // Emit 'repromptDialog' event
        return emitEvent(DialogEvents.REPROMPT_DIALOG, null, false, false)
            .thenCompose(handled -> {
                if (!handled && getActiveDialog() != null) {
                        // Lookup dialog
                        Dialog dialog = this.findDialog(getActiveDialog().getId());
                        if (dialog == null) {
                            throw new IllegalStateException(String.format(
                                "DialogSet.repromptDialog: Can't find A dialog with an id of '%s'.",
                                getActiveDialog().getId()
                            ));
                        }

                        // Ask dialog to re-prompt if supported
                        return dialog.repromptDialog(getContext(), getActiveDialog());
                    }
                return CompletableFuture.completedFuture(null);
            });
    }

    /**
     * Find the dialog id for the given context.
     * @param dialogId dialog id to find.
     * @return dialog with that id, or null.
     */
    public Dialog findDialog(String dialogId) {
        if (dialogs != null) {
            Dialog dialog = dialogs.find(dialogId);
            if (dialog != null) {
                return dialog;
            }
        }

        if (getParent() != null) {
            return getParent().findDialog(dialogId);
        }

        return null;
    }


    /**
     * @param name Name of the event to raise.
     * @return emitEvent
     */
    public CompletableFuture<Boolean> emitEvent(String name) {
        return emitEvent(name, null, true, false);
    }

    /**
     * @param name Name of the event to raise.
     * @param value Value to send along with the event.
     * @param bubble Flag to control whether the event should be bubbled to its parent if not handled locally.
     * Defaults to a value of `true`.
     * @param fromLeaf Whether the event is emitted from a leaf node.
     * @return completedFuture
     */
    public CompletableFuture<Boolean> emitEvent(String name, Object value, boolean bubble, boolean fromLeaf) {
        // Initialize event
        DialogEvent dialogEvent = new DialogEvent();
        dialogEvent.setBubble(bubble);
        dialogEvent.setName(name);
        dialogEvent.setValue(value);

        DialogContext dc = this;

        // Find starting dialog
        if (fromLeaf) {
            while (true) {
                DialogContext childDc = dc.getChild();

                if (childDc != null) {
                    dc = childDc;
                } else {
                    break;
                }
            }
        }

        // Dispatch to active dialog first
        DialogInstance instance = dc.getActiveDialog();
        if (instance != null) {
            Dialog dialog = dc.findDialog(instance.getId());

            if (dialog != null) {
                return dialog.onDialogEvent(dc, dialogEvent);
            }
        }

        return CompletableFuture.completedFuture(false);
    }

    /**
     * Obtain the locale in DialogContext.
     * @return A String representing the current locale.
     */
    public String getLocale() {
        // turn.locale is the highest precedence.
        String locale = (String) state.get(TurnContext.STATE_TURN_LOCALE);
        if (!StringUtils.isEmpty(locale)) {
            return locale;
        }

        // If turn.locale was not populated, fall back to activity locale
        locale = getContext().getActivity().getLocale();
        if (!StringUtils.isEmpty(locale)) {
            return locale;
        }

        return Locale.getDefault().toString();
    }

    /**
     * @param reason
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> endActiveDialog(DialogReason reason) {
        return endActiveDialog(reason, null);
    }

    /**
     * @param reason
     * @param result
     * @return CompletableFuture<Void>
     */
    private CompletableFuture<Void> endActiveDialog(DialogReason reason, Object result) {
        DialogInstance instance = getActiveDialog();
        if (instance == null) {
            return CompletableFuture.completedFuture(null);
        }

        return Async.wrapBlock(() -> dialogs.find(instance.getId()))
            .thenCompose(dialog -> {
                if (dialog != null) {
                    // Notify dialog of end
                    return dialog.endDialog(getContext(), instance, reason);
                }

                return CompletableFuture.completedFuture(null);
            })
            .thenCompose(v -> {
                // Pop dialog off stack
                stack.remove(0);

                // set Turn.LastResult to result
                ObjectPath.setPathValue(getContext().getTurnState(), TurnPath.LAST_RESULT, result);

                return CompletableFuture.completedFuture(null);
            });
    }
}
