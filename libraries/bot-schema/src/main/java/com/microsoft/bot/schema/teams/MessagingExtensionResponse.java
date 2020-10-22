// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Messaging extension response.
 */
public class MessagingExtensionResponse {
    @JsonProperty(value = "composeExtension")
    private MessagingExtensionResult composeExtension;

    @JsonProperty(value = "cacheInfo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CacheInfo cacheInfo;

    /**
     * Creates a new empty response.
     */
    public MessagingExtensionResponse() {

    }

    /**
     * Creates a new response with the specified result.
     * 
     * @param withResult The result.
     */
    public MessagingExtensionResponse(MessagingExtensionResult withResult) {
        composeExtension = withResult;
    }

    /**
     * Gets the response result.
     * 
     * @return The result.
     */
    public MessagingExtensionResult getComposeExtension() {
        return composeExtension;
    }

    /**
     * Sets the response result.
     * 
     * @param withComposeExtension The result.
     */
    public void setComposeExtension(MessagingExtensionResult withComposeExtension) {
        composeExtension = withComposeExtension;
    }

    /**
     * Gets the CacheInfo for this MessagingExtensionResponse.
     * @return CacheInfo
     */
    public CacheInfo getCacheInfo() {
        return cacheInfo;
    }

    /**
     * Sets the CacheInfo for this MessagingExtensionResponse.
     * @param withCacheInfo CacheInfo
     */
    public void setCacheInfo(CacheInfo withCacheInfo) {
        cacheInfo = withCacheInfo;
    }
}
