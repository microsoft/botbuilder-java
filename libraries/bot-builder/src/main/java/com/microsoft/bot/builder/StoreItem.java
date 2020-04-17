// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Exposes an ETag for concurrency control.
 */
public interface StoreItem {
    /**
     * Get eTag for concurrency.
     * 
     * @return The eTag value.
     */
    @JsonProperty(value = "eTag")
    String getETag();

    /**
     * Set eTag for concurrency.
     * 
     * @param withETag The eTag value.
     */
    @JsonProperty(value = "eTag")
    void setETag(String withETag);
}
