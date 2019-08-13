package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import java.time.OffsetDateTime;

/**
 * Transcript store item.
 */
public class Transcript {
    /**
     * channelId that the transcript was taken from.
     */
    private String channelId;

    public String channelId() {
        return this.channelId;
    }

    public Transcript withChannelId(String value) {
        this.channelId = value;
        return this;
    }

    /**
     * Conversation id.
     */
    private String id;

    public String getId() {
        return this.id;
    }

    public Transcript withId(String value) {
        this.id = value;
        return this;
    }

    /**
     * Date conversation was started.
     */
    private OffsetDateTime created = OffsetDateTime.now();

    public OffsetDateTime getCreated() {
        return this.created;
    }

    public Transcript withCreated(OffsetDateTime value) {
        this.created = value;
        return this;
    }
}
