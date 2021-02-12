// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Invoke ('tab/fetch') request value payload.
 */
public class TabRequest {
    @JsonProperty(value = "tabContext")
    private TabEntityContext tabContext;

    @JsonProperty(value = "context")
    private TabContext context;

    @JsonProperty(value = "state")
    private String state;

    /**
     * Initializes a new instance of the class.
     */
    public TabRequest() {

    }

    /**
     * Gets current tab entity request context.
     * @return Tab context
     */
    public TabEntityContext getTabContext() {
        return tabContext;
    }

    /**
     * Sets current tab entity request context.
     * @param withTabContext Tab context
     */
    public void setTabContext(TabEntityContext withTabContext) {
        tabContext = withTabContext;
    }

    /**
     * Gets current user context, i.e., the current theme.
     * @return Current user context, i.e., the current theme.
     */
    public TabContext getContext() {
        return context;
    }

    /**
     * Sets current user context, i.e., the current theme.
     * @param withContext Current user context, i.e., the current theme.
     */
    public void setContext(TabContext withContext) {
        context = withContext;
    }

    /**
     * Gets state, which is the magic code for OAuth Flow.
     * @return State, which is the magic code for OAuth Flow.
     */
    public String getState() {
        return state;
    }

    /**
     * Sets state, which is the magic code for OAuth Flow.
     * @param withState State, which is the magic code for OAuth Flow.
     */
    public void setState(String withState) {
        state = withState;
    }
}
