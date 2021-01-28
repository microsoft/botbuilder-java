// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.scopes;

import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.ScopePath;

/**
 * MemoryScope represents a named memory scope abstract class.
 */
public class ClassMemoryScope extends MemoryScope {
    /**
     * Initializes a new instance of the TurnMemoryScope class.
     */
    public ClassMemoryScope() {
        super(ScopePath.CLASS, false);
    }

    /**
     * Get the backing memory for this scope.
     */
    @Override
    public final Object getMemory(DialogContext dialogContext) {
        if (dialogContext == null) {
            throw new IllegalArgumentException("dialogContext cannot be null.");
        }

        // if active dialog is a container dialog then "dialog" binds to it.
        if (dialogContext.getActiveDialog() != null) {
            Dialog dialog = dialogContext.findDialog(dialogContext.getActiveDialog().getId());
            if (dialog != null) {
                return new ReadOnlyObject(dialog);
            }
        }
        return null;
}

    /**
     * Changes the backing Object for the memory scope.
     */
    @Override
    public final void setMemory(DialogContext dialogContext, Object memory) {
        throw new UnsupportedOperationException("You can't modify the class scope.");
    }
}
