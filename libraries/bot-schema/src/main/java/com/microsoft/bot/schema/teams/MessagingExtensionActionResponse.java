// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response of messaging extension action.
 */
public class MessagingExtensionActionResponse {
    @JsonProperty(value = "task")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private TaskModuleResponseBase task;

    @JsonProperty(value = "composeExtension")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private MessagingExtensionResult composeExtension;

    @JsonProperty(value = "cacheInfo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CacheInfo cacheInfo;

    /**
     * Gets the Adaptive card to appear in the task module.
     * 
     * @return The task card.
     */
    public TaskModuleResponseBase getTask() {
        return task;
    }

    /**
     * Sets the Adaptive card to appear in the task module.
     * 
     * @param withTask The task card.
     */
    public void setTask(TaskModuleResponseBase withTask) {
        task = withTask;
    }

    /**
     * Gets the extension result.
     * 
     * @return The extension result.
     */
    public MessagingExtensionResult getComposeExtension() {
        return composeExtension;
    }

    /**
     * Sets the extension result.
     * 
     * @param withComposeExtension The extension result.
     */
    public void setComposeExtension(MessagingExtensionResult withComposeExtension) {
        composeExtension = withComposeExtension;
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
