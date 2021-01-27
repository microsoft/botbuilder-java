// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import org.apache.commons.lang3.StringUtils;

/**
 * Handles persistence of a conversation state object using the conversation.Id
 * and from.Id part of an activity.
 */
public class PrivateConversationState extends BotState {
    /**
     * Initializes a new instance of the PrivateConversationState class.
     *
     * @param storage The storage provider to use.
     */
    public PrivateConversationState(Storage storage) {
        super(storage, PrivateConversationState.class.getSimpleName());
    }

    /**
     * Gets the key to use when reading and writing state to and from storage.
     *
     * @param turnContext The context object for this turn.
     * @return The storage key.
     */
    @Override
    public String getStorageKey(TurnContext turnContext) throws IllegalArgumentException {
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

        if (
            turnContext.getActivity().getFrom() == null
                || StringUtils.isEmpty(turnContext.getActivity().getFrom().getId())
        ) {
            throw new IllegalArgumentException("invalid activity-missing From.Id");
        }

        // {channelId}/conversations/{conversationId}/users/{userId}
        return turnContext.getActivity().getChannelId() + "/conversations/"
            + turnContext.getActivity().getConversation().getId() + "/users/"
            + turnContext.getActivity().getFrom().getId();
    }
}
