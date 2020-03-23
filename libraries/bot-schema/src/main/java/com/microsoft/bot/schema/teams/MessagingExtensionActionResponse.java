// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessagingExtensionActionResponse {
    @JsonProperty(value = "task")
    public TaskModuleResponseBase task;

    @JsonProperty(value = "composeExtension")
    public MessagingExtensionResult composeExtension;

    public TaskModuleResponseBase getTask() {
        return task;
    }

    public void setTask(TaskModuleResponseBase withTask) {
        task = withTask;
    }

    public MessagingExtensionResult getComposeExtension() {
        return composeExtension;
    }

    public void setComposeExtension(MessagingExtensionResult withComposeExtension) {
        composeExtension = withComposeExtension;
    }
}
