package com.microsoft.bot.dialogs.memory.scopes;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.dialogs.memory.ScopePath;

/**
 * MemoryScope represents a named memory scope abstract class.
 */
public class ConversationMemoryScope extends BotStateMemoryScope<ConversationState> {
    /**
     * DialogMemoryScope maps "this" -> dc.ActiveDialog.State.
     */
    public ConversationMemoryScope() {
        super(ConversationState.class, ScopePath.CONVERSATION);
    }
}
