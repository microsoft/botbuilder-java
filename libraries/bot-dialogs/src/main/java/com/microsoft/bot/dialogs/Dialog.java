package com.microsoft.bot.dialogs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.TurnContext;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;

/**
 * Base class for all dialogs.
 */
public abstract class Dialog {
    /**
     * A {@link DialogTurnResult} that indicates that the current dialog is
     * still active and waiting for input from the user next turn.
     */
    public static final DialogTurnResult END_OF_TURN = new DialogTurnResult(DialogTurnStatus.WAITING);

    @JsonIgnore
    private BotTelemetryClient telemetryClient;

    @JsonProperty(value = "id")
    private String id;

    /**
     * Initializes a new instance of the Dialog class.
     * @param dialogId The ID to assign to this dialog.
     */
    public Dialog(String dialogId) {

        if (StringUtils.isBlank(dialogId)) {
            throw new IllegalArgumentException("dialogId cannot be null");
        }

        id = dialogId;
        telemetryClient = new NullBotTelemetryClient();
    }

    /**
     * Gets id for the dialog.
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
     * @param withId Id for the dialog.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets the {@link BotTelemetryClient} to use for logging.
     * @return The BotTelemetryClient to use for logging.
     */
    public BotTelemetryClient getTelemetryClient() {
        return telemetryClient;
    }

    /**
     * Sets the {@link BotTelemetryClient} to use for logging.
     * @param withTelemetryClient The BotTelemetryClient to use for logging.
     */
    public void setTelemetryClient(BotTelemetryClient withTelemetryClient) {
        telemetryClient = withTelemetryClient;
    }

    /**
     * Called when the dialog is started and pushed onto the dialog stack.
     *
     * @param dc The {@link DialogContext} for the current turn of
     *           conversation.
     * @return If the task is successful, the result indicates whether the dialog is still
     * active after the turn has been processed by the dialog.
     */
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc) {
        return beginDialog(dc, null);
    }

    /**
     * Called when the dialog is started and pushed onto the dialog stack.
     *
     * @param dc The {@link DialogContext} for the current turn of
     *           conversation.
     * @param options Initial information to pass to the dialog.
     * @return If the task is successful, the result indicates whether the dialog is still
     * active after the turn has been processed by the dialog.
     */
    public abstract CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options);

    /**
     * Called when the dialog is _continued_, where it is the active dialog and the
     * user replies with a new activity.
     *
     * <p>If this method is *not* overridden, the dialog automatically ends when the user replies.</p>
     *
     * @param dc The {@link DialogContext} for the current turn of
     *           conversation.
     * @return If the task is successful, the result indicates whether the dialog is still
     * active after the turn has been processed by the dialog. The result may also contain a
     * return value.
     */
    public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {
        // By default just end the current dialog.
        return dc.endDialog(null);
    }

    /**
     * Called when a child dialog completed this turn, returning control to this dialog.
     *
     * <p>Generally, the child dialog was started with a call to
     * {@link #beginDialog(DialogContext, Object)} However, if the
     * {@link DialogContext#replaceDialog(String)} method
     * is called, the logical child dialog may be different than the original.</p>
     *
     * <p>If this method is *not* overridden, the dialog automatically ends when the user replies.</p>
     *
     * @param dc The dialog context for the current turn of the conversation.
     * @param reason Reason why the dialog resumed.
     * @return If the task is successful, the result indicates whether this dialog is still
     * active after this dialog turn has been processed.
     */
    public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason) {
        return resumeDialog(dc, reason, null);
    }

    /**
     * Called when a child dialog completed this turn, returning control to this dialog.
     *
     * <p>Generally, the child dialog was started with a call to
     * {@link #beginDialog(DialogContext, Object)} However, if the
     * {@link DialogContext#replaceDialog(String, Object)} method
     * is called, the logical child dialog may be different than the original.</p>
     *
     * <p>If this method is *not* overridden, the dialog automatically ends when the user replies.</p>
     *
     * @param dc The dialog context for the current turn of the conversation.
     * @param reason Reason why the dialog resumed.
     * @param result Optional, value returned from the dialog that was called. The type of the
     *               value returned is dependent on the child dialog.
     * @return If the task is successful, the result indicates whether this dialog is still
     * active after this dialog turn has been processed.
     */
    public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason, Object result) {
        // By default just end the current dialog and return result to parent.
        return dc.endDialog(result);
    }

    /**
     * Called when the dialog should re-prompt the user for input.
     * @param turnContext The context object for this turn.
     * @param instance State information for this dialog.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    public CompletableFuture<Void> repromptDialog(TurnContext turnContext, DialogInstance instance) {
        // No-op by default
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called when the dialog is ending.
     * @param turnContext The context object for this turn.
     * @param instance State information associated with the instance of this dialog on the dialog stack.
     * @param reason Reason why the dialog ended.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    public CompletableFuture<Void> endDialog(TurnContext turnContext, DialogInstance instance, DialogReason reason) {
        // No-op by default
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Gets a unique String which represents the version of this dialog.  If the version changes
     * between turns the dialog system will emit a DialogChanged event.
     * @return Unique String which should only change when dialog has changed in a way that should restart the dialog.
     */
    @JsonIgnore
    public String getVersion() {
        return id;
    }

    /**
     * Called when an event has been raised, using `DialogContext.emitEvent()`, by either the
     * current dialog or a dialog that the current dialog started.
     * @param dc The dialog context for the current turn of conversation.
     * @param e The event being raised.
     * @return True if the event is handled by the current dialog and bubbling should stop.
     */
    public CompletableFuture<Boolean> onDialogEvent(DialogContext dc, DialogEvent e) {
        // Before bubble
        return onPreBubbleEvent(dc, e)
            .thenCompose(handled -> {
                // Bubble as needed
                if (!handled && e.shouldBubble() && dc.getParent() != null) {
                    return dc.getParent().emitEvent(e.getName(), e.getValue(), true, false);
                }

                // just pass the handled value to the next stage
                return CompletableFuture.completedFuture(handled);
            })
            .thenCompose(handled -> {
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
     * <p>This is a good place to perform interception of an event as returning `true` will prevent
     * any further bubbling of the event to the dialogs parents and will also prevent any child
     * dialogs from performing their default processing.</p>
     *
     * @param dc The dialog context for the current turn of conversation.
     * @param e The event being raised.
     * @return Whether the event is handled by the current dialog and further processing should stop.
     */
    protected CompletableFuture<Boolean> onPreBubbleEvent(DialogContext dc, DialogEvent e) {
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Called after an event was bubbled to all parents and wasn't handled.
     *
     * <p>This is a good place to perform default processing logic for an event. Returning `true` will
     * prevent any processing of the event by child dialogs.</p>
     *
     * @param dc The dialog context for the current turn of conversation.
     * @param e The event being raised.
     * @return Whether the event is handled by the current dialog and further processing should stop.
     */
    protected CompletableFuture<Boolean> onPostBubbleEvent(DialogContext dc, DialogEvent e) {
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Computes an id for the Dialog.
     * @return The id.
     */
    protected String onComputeId() {
        return this.getClass().getName();
    }
}
