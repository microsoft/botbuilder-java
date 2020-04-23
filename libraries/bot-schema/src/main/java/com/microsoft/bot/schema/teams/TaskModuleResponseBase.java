// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for Task Module responses.
 */
public class TaskModuleResponseBase {
    @JsonProperty(value = "type")
    private String type;

    /**
     * Gets choice of action options when responding to the task/submit message.
     * Possible values include: 'message', 'continue'
     * 
     * @return The response type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets choice of action options when responding to the task/submit message.
     * Possible values include: 'message', 'continue'
     * 
     * @param withType The response type.
     */
    public void setType(String withType) {
        type = withType;
    }
}
