// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Current tab request context, i.e., the current theme.
 */
public class TabContext {
    @JsonProperty(value = "theme")
    private String theme;

    /**
     * Initializes a new instance of the class.
     */
    public TabContext() {

    }

    /**
     * Initializes a new instance of the class.
     * @param withTheme The current user's theme.
     */
    public TabContext(String withTheme) {
        theme = withTheme;
    }

    /**
     * Gets the current user's theme.
     * @return The current user's theme.
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Sets the current user's theme.
     * @param withTheme The current user's theme.
     */
    public void setTheme(String withTheme) {
        theme = withTheme;
    }
}
