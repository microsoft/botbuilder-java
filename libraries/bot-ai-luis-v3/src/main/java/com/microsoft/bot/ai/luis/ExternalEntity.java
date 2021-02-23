// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Request Body element to use when passing External Entities to the Luis
 * Service call.
 *
 */
public class ExternalEntity {

    /**
     * Initializes a new instance of ExternalEntity.
     */
    public ExternalEntity() {
    }

    /**
     * Initializes a new instance of ExternalEntity.
     * 
     * @param entity     name of the entity to extend.
     * @param start      start character index of the predicted entity.
     * @param length     length of the predicted entity.
     * @param resolution supplied custom resolution to return as the entity's
     *                   prediction.
     */
    public ExternalEntity(String entity, int start, int length, JsonNode resolution) {
        this.entity = entity;
        this.start = start;
        this.length = length;
        this.resolution = resolution;
    }

    @JsonProperty(value = "entityName")
    private String entity;

    @JsonProperty(value = "startIndex")
    private int start;

    @JsonProperty(value = "entityLength")
    private int length = -1;

    @JsonProperty(value = "resolution")
    private JsonNode resolution;

    /**
     * Gets the start character index of the predicted entity.
     * 
     * @return start character index of the predicted entity.
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the start character index of the predicted entity.
     * 
     * @param start character index of the predicted entity.
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Gets the name of the entity to extend.
     * 
     * @return name of the entity to extend.
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Sets the name of the entity to extend.
     * 
     * @param entity name of the entity to extend.
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     * Gets the length of the predicted entity.
     * 
     * @return length of the predicted entity.
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the length of the predicted entity.
     * 
     * @param length of the predicted entity.
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Gets a user supplied custom resolution to return as the entity's prediction.
     * 
     * @return custom resolution to return as the entity's prediction.
     */
    public JsonNode getResolution() {
        return resolution;
    }

    /**
     * Sets External entities to be recognized in query.
     * 
     * @param resolution custom resolution to return as the entity's prediction.
     */
    public void setResolution(JsonNode resolution) {
        this.resolution = resolution;
    }

    /**
     * Validate the object.
     * 
     * @throws IllegalArgumentException on null or invalid values
     */
    public void validate() throws IllegalArgumentException {
        if (entity == null || length == -1) {
            throw new IllegalArgumentException("ExternalEntity requires an EntityName and EntityLength > 0");
        }
    }
}
