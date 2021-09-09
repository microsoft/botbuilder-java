// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.scopes;

import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.ScopePath;

/**
 * MemoryScope represents a named memory scope abstract class.
 */
public class ThisMemoryScope extends MemoryScope {
    /**
     * DialogMemoryScope maps "this" to dc.ActiveDialog.State.
     */
    public ThisMemoryScope() {
        super(ScopePath.THIS, true);
    }

    /**
     * Get the backing memory for this scope.
     */
    @Override
    public final Object getMemory(DialogContext dialogContext) {
        if (dialogContext == null) {
            throw new IllegalArgumentException("dialogContext cannot be null.");
        }

        if (dialogContext.getActiveDialog() != null) {
            return dialogContext.getActiveDialog().getState();
        } else {
            return null;
        }

    }

    /**
     * Changes the backing Object for the memory scope.
     */
    @Override
    public final void setMemory(DialogContext dialogContext, Object memory) {
        throw new UnsupportedOperationException("You can't modify the class scope.");
    }
}
