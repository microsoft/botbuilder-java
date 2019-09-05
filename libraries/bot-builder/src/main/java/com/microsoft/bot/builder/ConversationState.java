package com.microsoft.bot.builder;

import org.apache.commons.lang3.StringUtils;

/**
 * Handles persistence of a conversation state object using the conversation ID as part of the key.
 */
public class ConversationState extends BotState {
    /**
     * Creates a new {@link ConversationState} object.
     */
    public ConversationState(Storage withStorage) {
        super(withStorage, ConversationState.class.getName());
    }

    @Override
    public String getStorageKey(TurnContext turnContext) {
        if (turnContext.getActivity() == null) {
            throw new IllegalArgumentException("invalid activity");
        }

        if (StringUtils.isEmpty(turnContext.getActivity().getChannelId())) {
            throw new IllegalArgumentException("invalid activity-missing channelId");
        }

        if (turnContext.getActivity().getConversation() == null
            || StringUtils.isEmpty(turnContext.getActivity().getConversation().getId())) {
            throw new IllegalArgumentException("invalid activity-missing Conversation.Id");
        }

        // {channelId}/conversations/{conversationId}
        return turnContext.getActivity().getChannelId()
            + "/conversations/"
            + turnContext.getActivity().getConversation().getId();
    }
}
