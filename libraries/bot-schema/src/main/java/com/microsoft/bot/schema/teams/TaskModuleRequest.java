// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Task module invoke request value payload.
 */
public class TaskModuleRequest {
    @JsonProperty(value = "data")
    private Object data;

    @JsonProperty(value = "context")
    private TaskModuleRequestContext context;

    @JsonProperty(value = "tabContext")
    private TabContext tabContext;

    /**
     * Gets user input data. Free payload with key-value pairs.
     * 
     * @return The input data.
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets user input data. Free payload with key-value pairs.
     * 
     * @param withData The input data.
     */
    public void setData(Object withData) {
        data = withData;
    }

    /**
     * Gets current user context, i.e., the current theme.
     * 
     * @return The user context.
     */
    public TaskModuleRequestContext getContext() {
        return context;
    }

    /**
     * Sets current user context, i.e., the current theme.
     * 
     * @param withContext The user context.
     */
    public void setContext(TaskModuleRequestContext withContext) {
        context = withContext;
    }

    /**
     * Gets current tab request context.
     * @return Tab request context.
     */
    public TabContext getTabContext() {
        return tabContext;
    }

    /**
     * Sets current tab request context.
     * @param withTabContext Tab request context.
     */
    public void setTabContext(TabContext withTabContext) {
        tabContext = withTabContext;
    }
}
