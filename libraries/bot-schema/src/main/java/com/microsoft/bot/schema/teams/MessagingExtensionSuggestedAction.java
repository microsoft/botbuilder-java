// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.CardAction;

import java.util.Collections;
import java.util.List;

/**
 * Messaging extension Actions (Only when type is auth or config).
 */
public class MessagingExtensionSuggestedAction {
    @JsonProperty(value = "actions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CardAction> actions;

    /**
     * Gets the actions.
     *
     * @return The list of CardActions.
     */
    public List<CardAction> getActions() {
        return actions;
    }

    /**
     * Sets the actions.
     *
     * @param withActions The list of CardActions.
     */
    public void setActions(List<CardAction> withActions) {
        actions = withActions;
    }

    /**
     * Sets the list of actions to a single specified CardAction.
     * @param action The CardAction
     */
    public void setAction(CardAction action) {
        setActions(Collections.singletonList(action));
    }
}
