// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Task Module response with message action.
 */
public class TaskModuleMessageResponse extends TaskModuleResponseBase {
    @JsonProperty(value = "value")
    private String value;

    /**
     * Initializes a new instance.
     */
    public TaskModuleMessageResponse() {
        setType("message");
    }

    /**
     * Gets info teams will display the value of value in a popup message box.
     *
     * @return The popup info.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets info teams will display the value of value in a popup message box.
     *
     * @param withValue The popup info.
     */
    public void setValue(String withValue) {
        value = withValue;
    }
}
