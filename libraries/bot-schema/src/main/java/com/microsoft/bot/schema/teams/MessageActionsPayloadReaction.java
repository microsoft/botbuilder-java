// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the reaction of a user to a message.
 */
public class MessageActionsPayloadReaction {
    @JsonProperty(value = "reactionType")
    private String reactionType;

    @JsonProperty(value = "createdDateTime")
    private String createdDateTime;

    @JsonProperty(value = "user")
    private MessageActionsPayloadFrom user;

    /**
     * Gets or sets the type of reaction given to the message. Possible values
     * include: 'like', 'heart', 'laugh', 'surprised', 'sad', 'angry'
     * 
     * @return The reaction type.
     */
    public String getReactionType() {
        return reactionType;
    }

    /**
     * Sets Gets or sets the type of reaction given to the message. Possible values
     * include: 'like', 'heart', 'laugh', 'surprised', 'sad', 'angry'
     * 
     * @param withReactionType The reaction type.
     */
    public void setReactionType(String withReactionType) {
        reactionType = withReactionType;
    }

    /**
     * Gets timestamp of when the user reacted to the message.
     * 
     * @return The created timestamp.
     */
    public String getCreatedDateTime() {
        return createdDateTime;
    }

    /**
     * Sets timestamp of when the user reacted to the message.
     * 
     * @param withCreatedDateTime The created timestamp.
     */
    public void setCreatedDateTime(String withCreatedDateTime) {
        createdDateTime = withCreatedDateTime;
    }

    /**
     * Gets the user with which the reaction is associated.
     * 
     * @return The From user.
     */
    public MessageActionsPayloadFrom getUser() {
        return user;
    }

    /**
     * Sets the user with which the reaction is associated.
     * 
     * @param withUser The From user.
     */
    public void setUser(MessageActionsPayloadFrom withUser) {
        user = withUser;
    }
}
