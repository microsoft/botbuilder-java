/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Represents a reference to a programmatic action
 */
public class SemanticAction {
    /**
     * Entities associated with this action.
     */
    @JsonProperty(value = "entities")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Entity> entities;

    /**
     * ID of this action.
     */
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    /**
     * Gets ID of this action.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets ID of this action.
     *
     * @param withId ID of this action
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Gets entities associated with this action.
     *
     * @return the activities value
     */
    public Map<String, Entity> getEntities() {
        return this.entities;
    }

    /**
     * Sets entities associated with this action.
     *
     * @param withEntities
     */
    public void setEntities(Map<String, Entity> withEntities) {
        this.entities = withEntities;
    }
}
