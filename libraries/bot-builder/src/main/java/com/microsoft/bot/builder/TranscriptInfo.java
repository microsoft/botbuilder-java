// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.time.OffsetDateTime;

/**
 * Represents a copy of a conversation.
 */
public class TranscriptInfo {
    private String channelId;
    private String id;
    private OffsetDateTime created;

    /**
     * Constructor.
     * 
     * @param withId        The conversation id.
     * @param withChannelId The channel id.
     * @param withCreated   Created timestamp.
     */
    public TranscriptInfo(String withId, String withChannelId, OffsetDateTime withCreated) {
        id = withId;
        channelId = withChannelId;
        created = withCreated;
    }

    /**
     * Gets the ID of the channel in which the conversation occurred.
     * 
     * @return The ID of the channel in which the conversation occurred.
     */
    public String channelId() {
        return channelId;
    }

    /**
     * Sets the ID of the channel in which the conversation occurred.
     * 
     * @param withChannelId The ID of the channel in which the conversation
     *                      occurred.
     */
    public void setChannelId(String withChannelId) {
        channelId = withChannelId;
    }

    /**
     * Gets the ID of the conversation.
     * 
     * @return The ID of the conversation.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the conversation.
     * 
     * @param withId The ID of the conversation.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets the date the conversation began.
     * 
     * @return The date then conversation began.
     */
    public OffsetDateTime getCreated() {
        return created;
    }

    /**
     * Sets the date the conversation began.
     * 
     * @param withCreated The date then conversation began.
     */
    public void setCreated(OffsetDateTime withCreated) {
        created = withCreated;
    }
}
