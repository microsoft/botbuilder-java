// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * List of channels under a team.
 */
public class ConversationList {
    @JsonProperty(value = "conversations")
    private List<ChannelInfo> conversations;

    /**
     * Gets the list of conversations.
     * 
     * @return The list of conversations.
     */
    public List<ChannelInfo> getConversations() {
        return conversations;
    }

    /**
     * Sets the list of conversations.
     * 
     * @param withConversations The new list of conversations.
     */
    public void setConversations(List<ChannelInfo> withConversations) {
        conversations = withConversations;
    }
}
