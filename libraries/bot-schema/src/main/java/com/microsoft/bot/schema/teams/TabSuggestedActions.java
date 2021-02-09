// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.CardAction;
import java.util.Arrays;
import java.util.List;

/**
 * Tab SuggestedActions (Only when type is 'auth' or 'silentAuth').
 */
public class TabSuggestedActions {
    @JsonProperty(value = "actions")
    private List<CardAction> actions;

    /**
     * Initializes a new instance of the class.
     */
    public TabSuggestedActions() {

    }

    /**
     * Gets actions for a card tab response.
     * @return Actions for this TabSuggestedActions.
     */
    public List<CardAction> getActions() {
        return actions;
    }

    /**
     * Sets actions for a card tab response.
     * @param withActions Actions for this TabSuggestedActions.
     */
    public void setActions(List<CardAction> withActions) {
        actions = withActions;
    }

    /**
     * Sets actions for a card tab response.
     * @param withActions Actions for this TabSuggestedActions.
     */
    public void setActions(CardAction... withActions) {
        actions = Arrays.asList(withActions);
    }
}
