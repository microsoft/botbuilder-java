// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Conversations result.
 */
public class ConversationsResult {
    /**
     * Paging token.
     */
    @JsonProperty(value = "continuationToken")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String continuationToken;

    /**
     * List of conversations.
     */
    @JsonProperty(value = "conversations")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ConversationMembers> conversations;

    /**
     * Get the continuationToken value.
     *
     * @return the continuationToken value
     */
    public String getContinuationToken() {
        return this.continuationToken;
    }

    /**
     * Set the continuationToken value.
     *
     * @param withContinuationToken the continuationToken value to set
     */
    public void setContinuationToken(String withContinuationToken) {
        this.continuationToken = withContinuationToken;
    }

    /**
     * Get the conversations value.
     *
     * @return the conversations value
     */
    public List<ConversationMembers> getConversations() {
        return this.conversations;
    }

    /**
     * Set the conversations value.
     *
     * @param withConversations the conversations value to set
     */
    public void setConversations(List<ConversationMembers> withConversations) {
        this.conversations = withConversations;
    }
}
