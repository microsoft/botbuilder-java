// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.CardAction;

import java.util.List;

public class MessagingExtensionSuggestedAction {
    @JsonProperty(value = "actions")
    public List<CardAction> actions;

    public List<CardAction> getActions() {
        return actions;
    }

    public void setActions(List<CardAction> withActions) {
        actions = withActions;
    }
}
