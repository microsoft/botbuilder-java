// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Current user context, i.e., the current theme.
 */
public class TaskModuleRequestContext {
    @JsonProperty(value = "theme")
    private String theme;

    /**
     * Gets the theme value.
     * 
     * @return The theme.
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Sets the theme value.
     * 
     * @param withTheme The theme.
     */
    public void setTheme(String withTheme) {
        theme = withTheme;
    }
}
