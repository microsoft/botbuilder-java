// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessagingExtensionParameter {
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "value")
    private Object value;

    public String getName() {
        return name;
    }

    public void setName(String withName) {
        name = withName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object withValue) {
        value = withValue;
    }
}
