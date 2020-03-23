// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskModuleRequestContext {
    @JsonProperty(value = "theme")
    private String theme;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String withTheme) {
        theme = withTheme;
    }
}
