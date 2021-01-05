package com.microsoft.bot.dialogs.memory.scopes;

import java.util.Optional;

import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogInstance;
import com.microsoft.bot.dialogs.ScopePath;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * DialogContextMemoryScope maps "dialogcontext" -> properties.
 */
public class DialogContextMemoryScope extends MemoryScope {

    private final String stackKey = "stack";

    private final String activeDialogKey = "activeDialog";

    private final String parentKey = "parent";

    /**
     * Initializes a new instance of the TurnMemoryScope class.
     */
    public DialogContextMemoryScope() {
        super(ScopePath.DIALOG_CONTEXT, false);
    }

    /**
     * Get the backing memory for this scope.
     */
    @Override
    public final Object getMemory(DialogContext dialogContext) {
        if (dialogContext == null) {
            throw new IllegalArgumentException("dialogContext cannot be null.");
        }

        JSONObject memory  = new JSONObject();
        JSONArray stack = new JSONArray();
        DialogContext currentDc = dialogContext;

        // go to leaf node
        while (currentDc.getChild() != null) {
            currentDc = currentDc.getChild();
        }

        while (currentDc != null) {
            // (PORTERS NOTE: javascript stack is reversed with top of stack on end)
            currentDc.getStack().forEach(item -> {
                if (item.getId().startsWith("ActionScope[")) {
                    stack.put(item.getId());
                }

            });

            currentDc = currentDc.getParent();
        }

        // top of stack is stack[0].
        memory.put(stackKey, stack);
        memory.put(activeDialogKey, Optional.ofNullable(dialogContext)
                                    .map(DialogContext::getActiveDialog)
                                    .map(DialogInstance::getId)
                                    .orElse(null));
        memory.put(parentKey, Optional.ofNullable(dialogContext)
                                    .map(DialogContext::getParent)
                                    .map(DialogContext::getActiveDialog)
                                    .map(DialogInstance::getId)
                                    .orElse(null));
        return memory;
    }

    /**
     * Changes the backing Object for the memory scope.
     */
    @Override
    public final void setMemory(DialogContext dialogContext, Object memory) {
        throw new UnsupportedOperationException("You can't modify the dialogcontext scope");
    }
}
