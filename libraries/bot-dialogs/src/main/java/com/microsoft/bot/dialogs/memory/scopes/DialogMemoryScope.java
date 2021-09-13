// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.scopes;

import java.util.Map;

import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.dialogs.DialogContainer;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.ScopePath;

/**
 * DialogMemoryScope maps "dialog" to dc.Parent?.ActiveDialog.State ?? ActiveDialog.State.
 */
public class DialogMemoryScope extends MemoryScope {
    /**
     * Initializes a new instance of the TurnMemoryScope class.
     */
    public DialogMemoryScope() {
        super(ScopePath.DIALOG, true);
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
            Dialog dialog = dialogContext.findDialog(dialogContext.getActiveDialog().getId());
            if (dialog instanceof DialogContainer) {
                return dialogContext.getActiveDialog().getState();
            }
        }

        if (dialogContext.getParent() != null) {
            if (dialogContext.getParent().getActiveDialog() != null) {
                return dialogContext.getParent().getActiveDialog().getState();
            }
        } else if (dialogContext.getActiveDialog() != null) {
            return dialogContext.getActiveDialog().getState();
        }
        return null;
    }

    /**
     * Changes the backing object for the memory scope.
     */
    @Override
    public final void setMemory(DialogContext dialogContext, Object memory) {
        if (dialogContext == null) {
            throw new IllegalArgumentException("dialogContext cannot be null.");
        }

        if (memory == null) {
            throw new IllegalArgumentException("memory cannot be null.");
        }

        if (!(memory instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("memory must be of type Map<?, ?>.");
        }

        // if active dialog is a container dialog then "dialog" binds to it
        if (dialogContext.getActiveDialog() != null) {
            Dialog dialog = dialogContext.findDialog(dialogContext.getActiveDialog().getId());
            if (dialog instanceof DialogContainer && memory instanceof Map<?, ?>) {
                dialogContext.getActiveDialog().getState().putAll((Map<String, Object>) memory);
                return;
            }
        } else if (dialogContext.getParent().getActiveDialog() != null) {
            dialogContext.getParent().getActiveDialog().getState().putAll((Map<String, Object>) memory);
            return;
        }

        throw new IllegalStateException(
                "Cannot set DialogMemoryScope. There is no active dialog dialog or parent dialog in the context");
    }
}
