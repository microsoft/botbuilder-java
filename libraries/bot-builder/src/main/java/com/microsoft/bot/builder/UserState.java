// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;


import org.apache.commons.lang3.StringUtils;

/**
 * Handles persistence of a user state object using the user ID as part of the key.
 */
public class UserState extends BotState {
    /**
     * Creates a new {@link UserState{TState}} object.
     *
     * @param withStorage  The storage provider to use.
     */
    public UserState(Storage withStorage) {
        super(withStorage, UserState.class.getSimpleName());
    }

    @Override
    public String getStorageKey(TurnContext turnContext) {
        if (turnContext.getActivity() == null) {
            throw new IllegalArgumentException("invalid activity");
        }

        if (StringUtils.isEmpty(turnContext.getActivity().getChannelId())) {
            throw new IllegalArgumentException("invalid activity-missing channelId");
        }

        if (turnContext.getActivity().getFrom() == null
            || StringUtils.isEmpty(turnContext.getActivity().getFrom().getId())) {
            throw new IllegalArgumentException("invalid activity-missing From.Id");
        }

        // {channelId}/users/{conversationId}
        return turnContext.getActivity().getChannelId()
            + "/users/"
            + turnContext.getActivity().getFrom().getId();
    }
}
