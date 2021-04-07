// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.scopes;

import java.util.TreeMap;

import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.ScopePath;

/**
 * TurnMemoryScope represents memory scoped to the current turn.
 */
public class TurnMemoryScope extends MemoryScope {
    /**
     * Initializes a new instance of the TurnMemoryScope class.
     */
    public TurnMemoryScope() {
        super(ScopePath.TURN, false);
    }

    /**
     * Get the backing memory for this scope.
     */
    @Override
    public final Object getMemory(DialogContext dialogContext) {
        if (dialogContext == null) {
            throw new IllegalArgumentException("dialogContext cannot be null.");
        }

        Object returnValue;

        returnValue = dialogContext.getContext().getTurnState().get(ScopePath.TURN);
        if (returnValue == null) {
            returnValue = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
            dialogContext.getContext().getTurnState().add(ScopePath.TURN, returnValue);
        }

        return returnValue;
    }

    /**
     * Changes the backing object for the memory scope.
     */
    @Override
    public final void setMemory(DialogContext dialogContext, Object memory) {
        if (dialogContext == null) {
            throw new IllegalArgumentException("dialogContext cannot be null.");
        }

        if (dialogContext.getContext().getTurnState().containsKey(ScopePath.TURN)) {
            dialogContext.getContext().getTurnState().replace(ScopePath.TURN, memory);
        } else {
            dialogContext.getContext().getTurnState().add(ScopePath.TURN, memory);
        }
    }
}
