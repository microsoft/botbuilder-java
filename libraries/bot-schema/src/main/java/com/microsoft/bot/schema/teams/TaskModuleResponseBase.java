// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskModuleResponseBase {
    @JsonProperty(value = "type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String withType) {
        type = withType;
    }
}
