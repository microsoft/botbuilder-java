// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A cache info object which notifies Teams how long an object should be cached for.
 */
public class CacheInfo {
    @JsonProperty(value = "cacheType")
    private String cacheType;

    @JsonProperty(value = "cacheDuration")
    private Integer cacheDuration;

    /**
     * Gets cache type.
     * @return The type of cache for this object.
     */
    public String getCacheType() {
        return cacheType;
    }

    /**
     * Sets cache type.
     * @param withCacheType The type of cache for this object.
     */
    public void setCacheType(String withCacheType) {
        cacheType = withCacheType;
    }

    /**
     * Gets cache duration.
     * @return The time in seconds for which the cached object should remain in the cache.
     */
    public Integer getCacheDuration() {
        return cacheDuration;
    }

    /**
     * Sets cache duration.
     * @param withCacheDuration The time in seconds for which the cached object should
     *                          remain in the cache.
     */
    public void setCacheDuration(Integer withCacheDuration) {
        cacheDuration = withCacheDuration;
    }
}
