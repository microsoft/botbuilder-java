// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskModuleMessageResponse extends TaskModuleResponseBase {
    @JsonProperty(value = "value")
    private TaskModuleTaskInfo value;

    public TaskModuleTaskInfo getValue() {
        return value;
    }

    public void setValue(TaskModuleTaskInfo withValue) {
        value = withValue;
    }
}
