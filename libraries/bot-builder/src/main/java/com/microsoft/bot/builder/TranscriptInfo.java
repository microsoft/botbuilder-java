// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.time.OffsetDateTime;

/**
 * Represents a copy of a conversation.
 */
public class TranscriptInfo {
    /**
     * channelId that the transcript was taken from.
     */
    private String channelId;
    /**
     * Conversation id.
     */
    private String id;
    /**
     * Date conversation was started.
     */
    private OffsetDateTime created = OffsetDateTime.now();

    public TranscriptInfo(String withId, String withChannelId, OffsetDateTime withCreated) {
        id = withId;
        channelId = withChannelId;
        created = withCreated;
    }

    public String channelId() {
        return this.channelId;
    }

    public void setChannelId(String withValue) {
        channelId = withValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String witValue) {
        id = witValue;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime withValue) {
        created = withValue;
    }
}
