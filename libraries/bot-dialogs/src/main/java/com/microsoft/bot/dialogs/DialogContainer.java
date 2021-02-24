// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.Severity;
import com.microsoft.bot.builder.TurnContext;
import java.util.concurrent.CompletableFuture;

/**
 * A container for a set of Dialogs.
 */
public abstract class DialogContainer extends Dialog {
    @JsonIgnore
    private DialogSet dialogs = new DialogSet();

    /**
     * Creates a new instance with the default dialog id.
     */
    public DialogContainer() {
        super(null);
    }

    /**
     * Creates a new instance with the default dialog id.
     * @param dialogId Id of the dialog.
     */
    public DialogContainer(String dialogId) {
        super(dialogId);
    }

    /**
     * Returns the Dialogs as a DialogSet.
     * @return The DialogSet of Dialogs.
     */
    public DialogSet getDialogs() {
        return dialogs;
    }


    /**
     * Sets the BotTelemetryClient to use for logging. When setting this property,
     * all of the contained dialogs' BotTelemetryClient properties are also set.
     *
     * @param withTelemetryClient The BotTelemetryClient to use for logging.
     */
    @Override
    public void setTelemetryClient(BotTelemetryClient withTelemetryClient) {
        super.setTelemetryClient(withTelemetryClient != null ? withTelemetryClient : new NullBotTelemetryClient());
        dialogs.setTelemetryClient(super.getTelemetryClient());
    }

    /**
     *
     * Creates an inner dialog context for the containers active child.
     *
     * @param dc Parents dialog context.
     * @return A new dialog context for the active child.
     */
    public abstract DialogContext createChildContext(DialogContext dc);

    /**
     * Searches the current DialogSet for a Dialog by its ID.
     *
     * @param dialogId ID of the dialog to search for.
     * @return The dialog if found; otherwise null
     */
    public Dialog findDialog(String dialogId) {
        return dialogs.find(dialogId);
    }

    /**
     * Called when an event has been raised, using `DialogContext.emitEvent()`, by
     * either the current dialog or a dialog that the current dialog started.
     *
     * <p>
     * This override will trace version changes.
     * </p>
     *
     * @param dc The dialog context for the current turn of conversation.
     * @param e  The event being raised.
     * @return True if the event is handled by the current dialog and bubbling
     *         should stop.
     */
    @Override
    public CompletableFuture<Boolean> onDialogEvent(DialogContext dc, DialogEvent e) {
        return super.onDialogEvent(dc, e).thenCompose(handled -> {
            // Trace unhandled "versionChanged" events.
            if (!handled && e.getName().equals(DialogEvents.VERSION_CHANGED)) {
                String traceMessage = String.format("Unhandled dialog event: {e.Name}. Active Dialog: %s",
                        dc.getActiveDialog().getId());

                dc.getDialogs().getTelemetryClient().trackTrace(traceMessage, Severity.WARNING, null);

                return TurnContext.traceActivity(dc.getContext(), traceMessage).thenApply(response -> handled);
            }

            return CompletableFuture.completedFuture(handled);
        });
    }

    /**
     * Returns internal version identifier for this container.
     *
     * <p>
     * DialogContainers detect changes of all sub-components in the container and
     * map that to an DialogChanged event. Because they do this, DialogContainers
     * "hide" the internal changes and just have the .id. This isolates changes to
     * the container level unless a container doesn't handle it. To support this
     * DialogContainers define a protected virtual method getInternalVersion() which
     * computes if this dialog or child dialogs have changed which is then examined
     * via calls to checkForVersionChange().
     * </p>
     *
     * @return version which represents the change of the internals of this
     *         container.
     */
    protected String getInternalVersion() {
        return dialogs.getVersion();
    }

    /**
     * Checks to see if a containers child dialogs have changed since the current
     * dialog instance was started.
     *
     * This should be called at the start of `beginDialog()`, `continueDialog()`,
     * and `resumeDialog()`.
     *
     * @param dc dialog context
     * @return CompletableFuture
     */
    protected CompletableFuture<Void> checkForVersionChange(DialogContext dc) {
        String current = dc.getActiveDialog().getVersion();
        dc.getActiveDialog().setVersion(getInternalVersion());

        // Check for change of previously stored hash
        if (current != null && !current.equals(dc.getActiveDialog().getVersion())) {
            // Give bot an opportunity to handle the change.
            // - If bot handles it the changeHash will have been updated as to avoid
            // triggering the
            // change again.
            return dc.emitEvent(DialogEvents.VERSION_CHANGED, getId(), true, false).thenApply(result -> null);
        }

        return CompletableFuture.completedFuture(null);
    }
}
