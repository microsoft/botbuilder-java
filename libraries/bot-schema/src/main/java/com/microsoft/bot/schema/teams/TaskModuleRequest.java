// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskModuleRequest {
    @JsonProperty(value = "data")
    private Object data;

    @JsonProperty(value = "context")
    private TaskModuleRequestContext context;

    public Object getData() {
        return data;
    }

    public void setData(Object withData) {
        data = withData;
    }

    public TaskModuleRequestContext getContext() {
        return context;
    }

    public void setContext(TaskModuleRequestContext withContext) {
        context = withContext;
    }
}
