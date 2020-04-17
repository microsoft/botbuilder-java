// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a team or channel entity.
 */
public class MessageActionsPayloadConversation {
    @JsonProperty(value = "conversationIdentityType")
    private String conversationIdentityType;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "displayName")
    private String displayName;

    /**
     * Gets the type of conversation, whether a team or channel. Possible values
     * include: 'team', 'channel'
     * 
     * @return The type of conversation.
     */
    public String getConversationIdentityType() {
        return conversationIdentityType;
    }

    /**
     * Sets the type of conversation, whether a team or channel. Possible values
     * include: 'team', 'channel'
     * 
     * @param withConversationIdentityType The type of the conversation.
     */
    public void setConversationIdentityType(String withConversationIdentityType) {
        conversationIdentityType = withConversationIdentityType;
    }

    /**
     * Gets the id of the team or channel.
     * 
     * @return The id of the team or channel.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the team or channel.
     * 
     * @param withId The id of the team or channel.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets the plaintext display name of the team or channel entity.
     * 
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the plaintext display name of the team or channel entity.
     * 
     * @param withDisplayName The display name.
     */
    public void setDisplayName(String withDisplayName) {
        displayName = withDisplayName;
    }
}
