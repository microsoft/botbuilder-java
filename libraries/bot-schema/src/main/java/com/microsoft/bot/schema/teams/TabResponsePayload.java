// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for Tab Response.
 */
public class TabResponsePayload {
    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "value")
    private TabResponseCards value;

    @JsonProperty(value = "suggestedActions")
    private TabSuggestedActions suggestedActions;

    /**
     * Initializes a new instance of the class.
     */
    public TabResponsePayload() {

    }

    /**
     * Gets choice of action options when responding to the tab/fetch message.
     * Possible values include: 'continue', 'auth' or 'silentAuth'.
     *
     * @return One of either: 'continue', 'auth' or 'silentAuth'.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets choice of action options when responding to the tab/fetch message.
     * Possible values include: 'continue', 'auth' or 'silentAuth'.

     * @param withType One of either: 'continue', 'auth' or 'silentAuth'.
     */
    public void setType(String withType) {
        type = withType;
    }

    /**
     * Gets or sets the TabResponseCards when responding to
     * tab/fetch activity with type of 'continue'.
     *
     * @return Cards in response to a TabResponseCards.
     */
    public TabResponseCards getValue() {
        return value;
    }

    /**
     * Sets or sets the TabResponseCards when responding to
     * tab/fetch activity with type of 'continue'.
     *
     * @param withValue Cards in response to a TabResponseCards.
     */
    public void setValue(TabResponseCards withValue) {
        value = withValue;
    }

    /**
     * Gets the Suggested Actions for this card tab.
     * @return The Suggested Actions for this card tab.
     */
    public TabSuggestedActions getSuggestedActions() {
        return suggestedActions;
    }

    /**
     * Sets the Suggested Actions for this card tab.
     * @param withSuggestedActions The Suggested Actions for this card tab.
     */
    public void setSuggestedActions(TabSuggestedActions withSuggestedActions) {
        suggestedActions = withSuggestedActions;
    }
}
