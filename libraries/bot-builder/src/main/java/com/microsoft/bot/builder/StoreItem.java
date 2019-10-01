// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface StoreItem {
    /**
     * Get eTag for concurrency.
     */
    @JsonProperty(value = "eTag")
    String getETag();

    /**
     * Set eTag for concurrency.
     */
    @JsonProperty(value = "eTag")
    void setETag(String withETag);
}
