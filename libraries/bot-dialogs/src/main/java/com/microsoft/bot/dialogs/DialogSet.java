// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;

/**
 * A collection of Dialog objects that can all call each other.
 */
public class DialogSet {
    private Map<String, Dialog> dialogs = new HashMap<>();
    private StatePropertyAccessor<DialogState> dialogState;
    @JsonIgnore
    private BotTelemetryClient telemetryClient;
    private String version;

    /**
     * Initializes a new instance of the DialogSet class.
     *
     * <p>
     * To start and control the dialogs in this dialog set, create a DialogContext
     * and use its methods to start, continue, or end dialogs. To create a dialog
     * context, call createContext(TurnContext).
     * </p>
     *
     * @param withDialogState The state property accessor with which to manage the
     *                        stack for this dialog set.
     */
    public DialogSet(StatePropertyAccessor<DialogState> withDialogState) {
        if (withDialogState == null) {
            throw new IllegalArgumentException("DialogState is required");
        }
        dialogState = withDialogState;
        telemetryClient = new NullBotTelemetryClient();
    }

    /**
     * Creates a DialogSet without state.
     */
    public DialogSet() {
        dialogState = null;
        telemetryClient = new NullBotTelemetryClient();
    }

    /**
     * Gets the BotTelemetryClient to use for logging.
     *
     * @return The BotTelemetryClient to use for logging.
     */
    public BotTelemetryClient getTelemetryClient() {
        return telemetryClient;
    }

    /**
     * Sets the BotTelemetryClient to use for logging.
     *
     * <p>
     * When this property is set, it sets the Dialog.TelemetryClient of each dialog
     * in the set to the new value.
     * </p>
     *
     * @param withBotTelemetryClient The BotTelemetryClient to use for logging.
     */
    public void setTelemetryClient(BotTelemetryClient withBotTelemetryClient) {
        telemetryClient = withBotTelemetryClient != null ? withBotTelemetryClient : new NullBotTelemetryClient();

        for (Dialog dialog : dialogs.values()) {
            dialog.setTelemetryClient(telemetryClient);
        }
    }

    /**
     * Gets a unique string which represents the combined versions of all dialogs in
     * this dialogset.
     *
     * @return Version will change when any of the child dialogs version changes.
     */
    public String getVersion() {
        if (version == null) {
            StringBuilder sb = new StringBuilder();
            for (Dialog dialog : dialogs.values()) {
                String v = dialog.getVersion();
                if (!StringUtils.isEmpty(v)) {
                    sb.append(v);
                }
            }

            version = Hashing.sha256().hashString(sb.toString(), StandardCharsets.UTF_8).toString();
        }

        return version;
    }

    /**
     * Adds a new dialog to the set and returns the set to allow fluent chaining. If
     * the Dialog.Id being added already exists in the set, the dialogs id will be
     * updated to include a suffix which makes it unique. So adding 2 dialogs named
     * "duplicate" to the set would result in the first one having an id of
     * "duplicate" and the second one having an id of "duplicate2".
     *
     * @param dialog The dialog to add. The added dialog's Dialog.TelemetryClient is
     *               set to the BotTelemetryClient of the dialog set.
     * @return The dialog set after the operation is complete.
     */
    public DialogSet add(Dialog dialog) {
        // Ensure new version hash is computed
        version = null;

        if (dialog == null) {
            throw new IllegalArgumentException("Dialog is required");
        }

        if (dialogs.containsKey(dialog.getId())) {
            // If we are trying to add the same exact instance, it's not a name collision.
            // No operation required since the instance is already in the dialog set.
            if (dialogs.get(dialog.getId()) == dialog) {
                return this;
            }

            // If we are adding a new dialog with a conflicting name, add a suffix to avoid
            // dialog name collisions.
            int nextSuffix = 2;

            while (true) {
                String suffixId = dialog.getId() + nextSuffix;

                if (!dialogs.containsKey(suffixId)) {
                    dialog.setId(suffixId);
                    break;
                }

                nextSuffix++;
            }
        }

        dialog.setTelemetryClient(telemetryClient);
        dialogs.put(dialog.getId(), dialog);

        // Automatically add any dependencies the dialog might have
        if (dialog instanceof DialogDependencies) {
            for (Dialog dependencyDialog : ((DialogDependencies) dialog).getDependencies()) {
                add(dependencyDialog);
            }
        }

        return this;
    }

    /**
     * Creates a DialogContext which can be used to work with the dialogs in the
     * DialogSet.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    public CompletableFuture<DialogContext> createContext(TurnContext turnContext) {
        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "TurnContext is required"
            ));
        }

        if (dialogState == null) {
            // Note: This shouldn't ever trigger, as the _dialogState is set in the
            // constructor
            // and validated there.
            return Async.completeExceptionally(new IllegalStateException(
                "DialogSet.createContext(): DialogSet created with a null StatePropertyAccessor."
            ));
        }

        // Load/initialize dialog state
        return dialogState.get(turnContext, DialogState::new)
                .thenApply(state -> new DialogContext(this, turnContext, state));
    }

    /**
     * Searches the current DialogSet for a Dialog by its ID.
     *
     * @param dialogId ID of the dialog to search for.
     * @return The dialog if found; otherwise null
     */
    public Dialog find(String dialogId) {
        if (StringUtils.isEmpty(dialogId)) {
            throw new IllegalArgumentException("DialogSet.find, dialogId is required");
        }

        return dialogs.get(dialogId);
    }

    /**
     * Returns a collection of Dialogs in this DialogSet.
     *
     * @return The Dialogs in this DialogSet.
     */
    public Collection<Dialog> getDialogs() {
        return dialogs.values();
    }
}
