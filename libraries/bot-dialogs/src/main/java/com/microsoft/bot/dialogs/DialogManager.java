// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.bot.builder.BotStateSet;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextStateCollection;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.dialogs.memory.DialogStateManagerConfiguration;

/**
 * Class which runs the dialog system.
 */
public class DialogManager {

    private final String lastAccess = "_lastAccess";
    private String rootDialogId;
    private final String dialogStateProperty;

    /**
     * Initializes a new instance of the
     * {@link com.microsoft.bot.dialogs.DialogManager} class.
     *
     * @param rootDialog          Root dialog to use.
     * @param dialogStateProperty Alternate name for the dialogState property.
     *                            (Default is "DialogState").
     */
    public DialogManager(Dialog rootDialog, String dialogStateProperty) {
        if (rootDialog != null) {
            this.setRootDialog(rootDialog);
        }

        this.dialogStateProperty = dialogStateProperty != null ? dialogStateProperty : "DialogState";
    }

    private ConversationState conversationState;

    /**
     * Sets the ConversationState.
     *
     * @param withConversationState The ConversationState.
     */
    public void setConversationState(ConversationState withConversationState) {
        conversationState = withConversationState;
    }

    /**
     * Gets the ConversationState.
     *
     * @return The ConversationState.
     */
    public ConversationState getConversationState() {
        return conversationState;
    }

    private UserState userState;

    /**
     * Gets the UserState.
     *
     * @return UserState.
     */
    public UserState getUserState() {
        return this.userState;
    }

    /**
     * Sets the UserState.
     *
     * @param userState UserState.
     */
    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    private TurnContextStateCollection initialTurnState = new TurnContextStateCollection();

    /**
     * Gets InitialTurnState collection to copy into the TurnState on every turn.
     *
     * @return TurnState.
     */
    public TurnContextStateCollection getInitialTurnState() {
        return initialTurnState;
    }

    /**
     * Gets the Root Dialog.
     *
     * @return the Root Dialog.
     */
    public Dialog getRootDialog() {
        if (rootDialogId != null) {
            return this.getDialogs().find(rootDialogId);
        } else {
            return null;
        }

    }

    /**
     * Sets root dialog to use to start conversation.
     *
     * @param dialog Root dialog to use to start conversation.
     */
    public void setRootDialog(Dialog dialog) {
        setDialogs(new DialogSet());
        if (dialog != null) {
            rootDialogId = dialog.getId();
            getDialogs().setTelemetryClient(dialog.getTelemetryClient());
            getDialogs().add(dialog);
            registerContainerDialogs(dialog, false);
        } else {
            rootDialogId = null;
        }
    }

    @JsonIgnore
    private DialogSet dialogs = new DialogSet();

    /**
     * Returns the DialogSet.
     *
     * @return The DialogSet.
     */
    public DialogSet getDialogs() {
        return dialogs;
    }

    /**
     * Set the DialogSet.
     *
     * @param withDialogSet The DialogSet being provided.
     */
    public void setDialogs(DialogSet withDialogSet) {
        dialogs = withDialogSet;
    }

    private DialogStateManagerConfiguration stateManagerConfiguration;

    /**
     * Gets the DialogStateManagerConfiguration.
     *
     * @return The DialogStateManagerConfiguration.
     */
    public DialogStateManagerConfiguration getStateManagerConfiguration() {
        return this.stateManagerConfiguration;
    }

    /**
     * Sets the DialogStateManagerConfiguration.
     *
     * @param withStateManagerConfiguration The DialogStateManagerConfiguration to
     *                                      set from.
     */
    public void setStateManagerConfiguration(DialogStateManagerConfiguration withStateManagerConfiguration) {
        this.stateManagerConfiguration = withStateManagerConfiguration;
    }

    private Integer expireAfter;

    /**
     * Gets the (optinal) number of milliseconds to expire the bot's state after.
     *
     * @return Number of milliseconds.
     */
    public Integer getExpireAfter() {
        return this.expireAfter;
    }

    /**
     * Sets the (optional) number of milliseconds to expire the bot's state after.
     *
     * @param withExpireAfter Number of milliseconds.
     */
    public void setExpireAfter(Integer withExpireAfter) {
        this.expireAfter = withExpireAfter;
    }

    /**
     * Runs dialog system in the context of an ITurnContext.
     *
     * @param context Turn Context
     * @return result of runnign the logic against the activity.
     */
    public CompletableFuture<DialogManagerResult> onTurn(TurnContext context) {
        BotStateSet botStateSet = new BotStateSet();

        // Preload TurnState with DM TurnState.
        initialTurnState.getTurnStateServices().forEach((key, value) -> {
            context.getTurnState().add(key, value);
        });

        // register DialogManager with TurnState.
        context.getTurnState().replace(this);

        if (conversationState == null) {
            ConversationState cState = context.getTurnState().get(ConversationState.class);
            if (cState != null) {
                conversationState = cState;
            } else {
                return Async.completeExceptionally(new IllegalStateException(
                    String.format("Unable to get an instance of %s from turnContext.",
                    ConversationState.class.toString())
                ));
            }
        } else {
            context.getTurnState().replace(conversationState);
        }

        botStateSet.add(conversationState);

        if (userState == null) {
            userState = context.getTurnState().get(UserState.class);
        } else {
            context.getTurnState().replace(userState);
        }

        if (userState != null) {
            botStateSet.add(userState);
        }

        // create property accessors
        StatePropertyAccessor<OffsetDateTime> lastAccessProperty = conversationState.createProperty(lastAccess);

        OffsetDateTime lastAccessed = lastAccessProperty.get(context, () -> {
            return OffsetDateTime.now(ZoneId.of("UTC"));
        }).join();

        // Check for expired conversation
        if (expireAfter != null && (OffsetDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli()
                - lastAccessed.toInstant().toEpochMilli()) >= expireAfter) {
            conversationState.clearState(context).join();
        }

        lastAccessed = OffsetDateTime.now(ZoneId.of("UTC"));
        lastAccessProperty.set(context, lastAccessed).join();

        // get dialog stack
        StatePropertyAccessor<DialogState> dialogsProperty = conversationState.createProperty(dialogStateProperty);

        DialogState dialogState = dialogsProperty.get(context, DialogState::new).join();

        // Create DialogContext
        DialogContext dc = new DialogContext(dialogs, context, dialogState);

        return Dialog.innerRun(context, rootDialogId, dc, getStateManagerConfiguration()).thenCompose(turnResult -> {
            return botStateSet.saveAllChanges(dc.getContext(), false).thenCompose(saveResult -> {
                DialogManagerResult result = new DialogManagerResult();
                result.setTurnResult(turnResult);
                return CompletableFuture.completedFuture(result);
            });
        });
    }

    /**
     * Recursively traverses the Dialog tree and registers instances of
     * DialogContainer in the DialogSet for this <see cref="DialogManager"/>
     * instance.
     *
     * @param dialog       Root of the Dialog subtree to iterate and register
     *                     containers from.
     * @param registerRoot Whether to register the root of the subtree.
     */
    private void registerContainerDialogs(Dialog dialog, Boolean registerRoot) {
        if (dialog instanceof DialogContainer) {
            if (registerRoot) {
                getDialogs().add(dialog);
            }

            Collection<Dialog> dlogs = ((DialogContainer) dialog).getDialogs().getDialogs();

            for (Dialog dlg : dlogs) {
                registerContainerDialogs(dlg, true);
            }
        }
    }
}
