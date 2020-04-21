// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A channel info object which describes the channel.
 */
public class ChannelInfo {
    @JsonProperty(value = "id")
    private String id;

    /**
     * name of the channel.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Get the unique identifier representing a channel.
     *
     * @return the unique identifier representing a channel.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set unique identifier representing a channel.
     *
     * @param withId the unique identifier representing a channel.
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Get the name of the channel.
     *
     * @return name of the channel.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of the channel.
     *
     * @param withName the name of the channel.
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * Initializes a new instance of the ChannelInfo class.
     *
     * @param withId   identifier representing a channel.
     * @param withName Name of the channel.
     */
    public ChannelInfo(String withId, String withName) {
        this.id = withId;
        this.name = withName;
    }

    /**
     * Initializes a new instance of the ChannelInfo class.
     */
    public ChannelInfo() {
    }

    /**
     * Initialzies a new instance of the ChannelInfo class with an id.
     * 
     * @param withId The id.
     */
    public ChannelInfo(String withId) {
        id = withId;
    }
}
