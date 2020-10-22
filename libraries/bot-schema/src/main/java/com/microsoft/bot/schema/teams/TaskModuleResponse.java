// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Envelope for Task Module Response.
 */
public class TaskModuleResponse {
    @JsonProperty(value = "task")
    private TaskModuleResponseBase task;

    @JsonProperty(value = "cacheInfo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CacheInfo cacheInfo;

    /**
     * Gets the response task.
     * 
     * @return The response task.
     */
    public TaskModuleResponseBase getTask() {
        return task;
    }

    /**
     * Sets the response task.
     * 
     * @param withTask The response task.
     */
    public void setTask(TaskModuleResponseBase withTask) {
        task = withTask;
    }

    /**
     * Gets the CacheInfo for this MessagingExtensionActionResponse.
     * @return CacheInfo
     */
    public CacheInfo getCacheInfo() {
        return cacheInfo;
    }

    /**
     * Sets the CacheInfo for this MessagingExtensionActionResponse.
     * @param withCacheInfo CacheInfo
     */
    public void setCacheInfo(CacheInfo withCacheInfo) {
        cacheInfo = withCacheInfo;
    }
}
