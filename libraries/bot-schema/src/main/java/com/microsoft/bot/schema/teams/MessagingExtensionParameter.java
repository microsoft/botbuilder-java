// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Messaging extension query parameters.
 */
public class MessagingExtensionParameter {
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "value")
    private Object value;

    /**
     * Gets name of the parameter.
     * 
     * @return The parameter name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of the parameter.
     * 
     * @param withName The parameter name.
     */
    public void setName(String withName) {
        name = withName;
    }

    /**
     * Gets value of the parameter.
     * 
     * @return The parameter value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets value of the parameter.
     * 
     * @param withValue The parameter value.
     */
    public void setValue(Object withValue) {
        value = withValue;
    }
}
