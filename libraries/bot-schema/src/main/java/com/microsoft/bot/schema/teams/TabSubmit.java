// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Invoke ('tab/submit') request value payload.
 */
public class TabSubmit {
    @JsonProperty(value = "tabContext")
    private TabEntityContext tabEntityContext;

    @JsonProperty(value = "context")
    private TabContext context;

    @JsonProperty(value = "data")
    private TabSubmitData data;

    /**
     * Initializes a new instance of the class.
     */
    public TabSubmit() {

    }

    /**
     * Gets current tab entity request context.
     * @return Tab context for the TabSubmit.
     */
    public TabEntityContext getTabEntityContext() {
        return tabEntityContext;
    }

    /**
     * Sets current tab entity request context.
     * @param withTabEntityContext Tab context for the TabSubmit.
     */
    public void setTabEntityContext(TabEntityContext withTabEntityContext) {
        tabEntityContext = withTabEntityContext;
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
     * Gets user input data. Free payload containing properties of key-value pairs.
     * @return User input data. Free payload containing properties of key-value pairs.
     */
    public TabSubmitData getData() {
        return data;
    }

    /**
     * Sets user input data. Free payload containing properties of key-value pairs.
     * @param withData User input data. Free payload containing properties of key-value pairs.
     */
    public void setData(TabSubmitData withData) {
        data = withData;
    }
}
