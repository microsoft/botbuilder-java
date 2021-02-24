// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Current TabRequest entity context, or 'tabEntityId'.
 */
public class TabEntityContext {
    @JsonProperty(value = "tabEntityId")
    private String tabEntityId;

    /**
     * Initializes a new instance of the class.
     */
    public TabEntityContext() {

    }

    /**
     * Initializes a new instance of the class.
     * @param withTabEntityId The entity id of the tab.
     */
    public TabEntityContext(String withTabEntityId) {
        tabEntityId = withTabEntityId;
    }

    /**
     * Gets the entity id of the tab.
     * @return The entity id of the tab.
     */
    public String getTabEntityId() {
        return tabEntityId;
    }

    /**
     * Sets the entity id of the tab.
     * @param withTabEntityId The entity id of the tab.
     */
    public void setTabEntityId(String withTabEntityId) {
        tabEntityId = withTabEntityId;
    }
}
