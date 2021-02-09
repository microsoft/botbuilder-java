// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Envelope for Card Tab Response Payload.
 */
public class TabResponse {
    @JsonProperty(value = "tab")
    private TabResponsePayload tab;

    /**
     * Initializes a new instance of the class.
     */
    public TabResponse() {

    }

    /**
     * Gets the response to the tab/fetch message.
     * @return Cards in response to a TabRequest.
     */
    public TabResponsePayload getTab() {
        return tab;
    }

    /**
     * Sets the response to the tab/fetch message.
     * @param withTab Cards in response to a TabRequest.
     */
    public void setTab(TabResponsePayload withTab) {
        tab = withTab;
    }
}
