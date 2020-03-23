// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class O365ConnectorCardActionCard extends O365ConnectorCardActionBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "ActionCard";

    @JsonProperty(value = "inputs")
    private List<O365ConnectorCardInputBase> inputs;

    @JsonProperty(value = "actions")
    private List<O365ConnectorCardActionBase> actions;

    public List<O365ConnectorCardInputBase> getInputs() {
        return inputs;
    }

    public void setInputs(List<O365ConnectorCardInputBase> withInputs) {
        inputs = withInputs;
    }

    public List<O365ConnectorCardActionBase> getActions() {
        return actions;
    }

    public void setActions(List<O365ConnectorCardActionBase> withActions) {
        actions = withActions;
    }
}
