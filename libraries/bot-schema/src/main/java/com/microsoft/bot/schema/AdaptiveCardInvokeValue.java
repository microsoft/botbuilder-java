// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.


package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the structure that arrives in the Activity.Value for Invoke activity
 * with Name of 'adaptiveCard/action'.
 */
public class AdaptiveCardInvokeValue {

    @JsonProperty(value = "action")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private AdaptiveCardInvokeAction action;

    @JsonProperty(value = "authentication")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private AdaptiveCardAuthentication authentication;

    @JsonProperty(value = "state")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String state;

    /**
     * Gets the action of this adaptive card action invoke value.
     * @return the Action value as a AdaptiveCardInvokeAction.
     */
    public AdaptiveCardInvokeAction getAction() {
        return this.action;
    }

    /**
     * Sets the action of this adaptive card action invoke value.
     * @param withAction The Action value.
     */
    public void setAction(AdaptiveCardInvokeAction withAction) {
        this.action = withAction;
    }

    /**
     * Gets the {@link AdaptiveCardAuthentication} for this adaptive
     * card invoke action value.
     * @return the Authentication value as a AdaptiveCardAuthentication.
     */
    public AdaptiveCardAuthentication getAuthentication() {
        return this.authentication;
    }

    /**
     * Sets the {@link AdaptiveCardAuthentication} for this adaptive
     * card invoke action value.
     * @param withAuthentication The Authentication value.
     */
    public void setAuthentication(AdaptiveCardAuthentication withAuthentication) {
        this.authentication = withAuthentication;
    }

    /**
     * Gets the 'state' or magic code for an OAuth flow.
     * @return the State value as a String.
     */
    public String getState() {
        return this.state;
    }

    /**
     * Sets the 'state' or magic code for an OAuth flow.
     * @param withState The State value.
     */
    public void setState(String withState) {
        this.state = withState;
    }

}
