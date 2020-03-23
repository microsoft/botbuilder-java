// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MessagingExtensionQuery {
    @JsonProperty(value = "commandId")
    private String commandId;

    @JsonProperty(value = "parameters")
    public List<MessagingExtensionParameter> parameters;

    @JsonProperty(value = "queryOptions")
    private MessagingExtensionQueryOptions queryOptions;

    @JsonProperty(value = "state")
    private String state;

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String withCommandId) {
        commandId = withCommandId;
    }

    public List<MessagingExtensionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<MessagingExtensionParameter> withParameters) {
        parameters = withParameters;
    }

    public MessagingExtensionQueryOptions getQueryOptions() {
        return queryOptions;
    }

    public void setQueryOptions(MessagingExtensionQueryOptions withQueryOptions) {
        queryOptions = withQueryOptions;
    }

    public String getState() {
        return state;
    }

    public void setState(String withState) {
        state = withState;
    }
}
