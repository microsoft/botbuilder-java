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

    public String channelId() {
        return this.channelId;
    }

    public void setChannelId(String withValue) {
        channelId = withValue;
    }

    /**
     * Conversation id.
     */
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String witValue) {
        id = witValue;
    }

    /**
     * Date conversation was started.
     */
    private OffsetDateTime created = OffsetDateTime.now();

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime withValue) {
        created = withValue;
    }
}
