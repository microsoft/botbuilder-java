// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Messaging extension query options.
 */
public class MessagingExtensionQueryOptions {
    @JsonProperty(value = "skip")
    private int skip;

    @JsonProperty(value = "count")
    private int count;

    /**
     * Gets number of entities to skip.
     * 
     * @return The number of entities to skip.
     */
    public int getSkip() {
        return skip;
    }

    /**
     * Sets number of entities to skip.
     * 
     * @param withSkip The number of entities to skip.
     */
    public void setSkip(int withSkip) {
        skip = withSkip;
    }

    /**
     * Gets number of entities to fetch.
     * 
     * @return The number of entities to fetch.
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets number of entities to fetch.
     * 
     * @param withCount The number of entities to fetch.
     */
    public void setCount(int withCount) {
        count = withCount;
    }
}
