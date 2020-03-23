// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskModuleResponse {
    @JsonProperty(value = "task")
    private String task;

    public String getTask() {
        return task;
    }

    public void setTask(String withTask) {
        task = withTask;
    }
}
