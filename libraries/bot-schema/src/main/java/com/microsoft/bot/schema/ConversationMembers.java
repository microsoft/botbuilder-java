// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Conversation and its members.
 */
public class ConversationMembers {
    /**
     * Conversation ID.
     */
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    /**
     * List of members in this conversation.
     */
    @JsonProperty(value = "members")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ChannelAccount> members;

    /**
     * Get the {@link #id} value.
     * 
     * @return the id value
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the {@link #id} value.
     * 
     * @param withId the id value to set
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Get the {@link #members} value.
     * 
     * @return the members value
     */
    public List<ChannelAccount> getMembers() {
        return this.members;
    }

    /**
     * Set the {@link #members} value.
     * 
     * @param withMembers the members value to set
     */
    public void setMembers(List<ChannelAccount> withMembers) {
        this.members = withMembers;
    }
}
