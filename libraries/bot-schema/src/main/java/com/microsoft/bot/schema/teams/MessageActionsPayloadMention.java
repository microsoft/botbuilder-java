// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the entity that was mentioned in the message.
 */
public class MessageActionsPayloadMention {
    @JsonProperty(value = "id")
    private int id;

    @JsonProperty(value = "mentionText")
    private String mentionText;

    @JsonProperty(value = "mentioned")
    private MessageActionsPayloadFrom mentioned;

    /**
     * Gets the id of the mentioned entity.
     * 
     * @return The id of the mention.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the mentioned entity.
     * 
     * @param withId The id of the mention.
     */
    public void setId(int withId) {
        id = withId;
    }

    /**
     * Gets the plaintext display name of the mentioned entity.
     * 
     * @return The plaintext display name.
     */
    public String getMentionText() {
        return mentionText;
    }

    /**
     * Sets the plaintext display name of the mentioned entity.
     * 
     * @param withMentionText The plaintext display name.
     */
    public void setMentionText(String withMentionText) {
        mentionText = withMentionText;
    }

    /**
     * Gets details on the mentioned entity.
     * 
     * @return From details.
     */
    public MessageActionsPayloadFrom getMentioned() {
        return mentioned;
    }

    /**
     * Sets details on the mentioned entity.
     * 
     * @param withMentioned From details.
     */
    public void setMentioned(MessageActionsPayloadFrom withMentioned) {
        mentioned = withMentioned;
    }
}
