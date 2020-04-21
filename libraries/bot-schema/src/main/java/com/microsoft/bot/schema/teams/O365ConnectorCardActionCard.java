// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * O365 connector card ActionCard action.
 */
public class O365ConnectorCardActionCard extends O365ConnectorCardActionBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "ActionCard";

    @JsonProperty(value = "inputs")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<O365ConnectorCardInputBase> inputs;

    @JsonProperty(value = "actions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<O365ConnectorCardActionBase> actions;

    /**
     * Gets list of inputs contained in this ActionCard whose each item can be in
     * any subtype of O365ConnectorCardInputBase.
     * 
     * @return The card inputs.
     */
    public List<O365ConnectorCardInputBase> getInputs() {
        return inputs;
    }

    /**
     * Sets list of inputs contained in this ActionCard whose each item can be in
     * any subtype of O365ConnectorCardInputBase.
     * 
     * @param withInputs The card inputs.
     */
    public void setInputs(List<O365ConnectorCardInputBase> withInputs) {
        inputs = withInputs;
    }

    /**
     * Gets list of actions contained in this ActionCard whose each item can be in
     * any subtype of O365ConnectorCardActionBase except
     * O365ConnectorCardActionCard, as nested ActionCard is forbidden.
     * 
     * @return The card actions.
     */
    public List<O365ConnectorCardActionBase> getActions() {
        return actions;
    }

    /**
     * Sets list of actions contained in this ActionCard whose each item can be in
     * any subtype of O365ConnectorCardActionBase except
     * O365ConnectorCardActionCard, as nested ActionCard is forbidden.
     * 
     * @param withActions The card actions.
     */
    public void setActions(List<O365ConnectorCardActionBase> withActions) {
        actions = withActions;
    }
}
