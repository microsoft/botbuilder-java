// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Envelope for Task Module Response.
 */
public class TaskModuleResponse {
    @JsonProperty(value = "task")
    private TaskModuleResponseBase task;

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
}
