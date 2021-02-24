// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.scopes;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.BotState;
import com.microsoft.bot.builder.BotState.CachedBotState;
import com.microsoft.bot.dialogs.DialogContext;

/**
 * BotStateMemoryScope represents a BotState scoped memory.
 *
 * @param <T> The BotState type.
 */
public class BotStateMemoryScope<T extends BotState> extends MemoryScope {

    private Class<T> type;

    /**
     * Initializes a new instance of the TurnMemoryScope class.
     *
     * @param type The Type of T that is being created.
     * @param name Name of the property.
     */
    public BotStateMemoryScope(Class<T> type, String name) {
        super(name, true);
        this.type = type;
    }

    /**
     * Get the backing memory for this scope.
     */
    @Override
    public final Object getMemory(DialogContext dialogContext) {
        if (dialogContext == null) {
            throw new IllegalArgumentException("dialogContext cannot be null.");
        }

        T botState = getBotState(dialogContext);
        if (botState != null) {
            CachedBotState cachedState = botState.getCachedState(dialogContext.getContext());
            return cachedState.getState();
        } else {
            return null;
        }
    }

    /**
     * Changes the backing Object for the memory scope.
     */
    @Override
    public final void setMemory(DialogContext dialogContext, Object memory) {
        throw new UnsupportedOperationException("You cannot replace the root BotState Object.");
    }

    /**
     *
     */
    @Override
    public CompletableFuture<Void> load(DialogContext dialogContext, Boolean force) {
        T botState = getBotState(dialogContext);

        if (botState != null) {
            return botState.load(dialogContext.getContext(), force);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * @param dialogContext
     * @param force
     * @return A future that represents the
     */
    @Override
    public CompletableFuture<Void> saveChanges(DialogContext dialogContext, Boolean force) {
        T botState = getBotState(dialogContext);

        if (botState != null) {
            return botState.saveChanges(dialogContext.getContext(), force);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private T getBotState(DialogContext dialogContext) {
        return dialogContext.getContext().getTurnState().get(type);
    }
}
