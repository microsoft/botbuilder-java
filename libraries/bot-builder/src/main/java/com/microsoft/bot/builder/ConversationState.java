// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines a state management object for conversation state.
 */
public class ConversationState extends BotState {
    /**
     * Creates a new {@link ConversationState} object.
     *
     * @param withStorage The storage layer to use.
     */
    public ConversationState(Storage withStorage) {
        super(withStorage, ConversationState.class.getSimpleName());
    }

    /**
     * Gets the key to use when reading and writing state to and from storage.
     *
     * @param turnContext The context object for this turn.
     * @return The storage key.
     */
    @Override
    public String getStorageKey(TurnContext turnContext) {
        if (turnContext.getActivity() == null) {
            throw new IllegalArgumentException("invalid activity");
        }

        if (StringUtils.isEmpty(turnContext.getActivity().getChannelId())) {
            throw new IllegalArgumentException("invalid activity-missing channelId");
        }

        if (
            turnContext.getActivity().getConversation() == null
                || StringUtils.isEmpty(turnContext.getActivity().getConversation().getId())
        ) {
            throw new IllegalArgumentException("invalid activity-missing Conversation.Id");
        }

        // {channelId}/conversations/{conversationId}
        return turnContext.getActivity().getChannelId() + "/conversations/"
            + turnContext.getActivity().getConversation().getId();
    }
}
