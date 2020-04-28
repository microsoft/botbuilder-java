// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Task Module Response with continue action.
 */
public class TaskModuleContinueResponse extends TaskModuleResponseBase {
    @JsonProperty(value = "value")
    private TaskModuleTaskInfo value;

    /**
     * Initializes a new instance.
     */
    public TaskModuleContinueResponse() {
        setType("continue");
    }

    /**
     * Gets the Adaptive card to appear in the task module.
     *
     * @return The value info.
     */
    public TaskModuleTaskInfo getValue() {
        return value;
    }

    /**
     * Sets the Adaptive card to appear in the task module.
     *
     * @param withValue The value info.
     */
    public void setValue(TaskModuleTaskInfo withValue) {
        value = withValue;
    }
}
