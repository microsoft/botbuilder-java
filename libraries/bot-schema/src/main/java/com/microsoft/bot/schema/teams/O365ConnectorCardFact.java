// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardFact {
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "value")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String withName) {
        this.name = withName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String withValue) {
        this.value = withValue;
    }
}
