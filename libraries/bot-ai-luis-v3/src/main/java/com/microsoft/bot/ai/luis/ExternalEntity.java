package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.applicationinsights.core.dependencies.google.gson.JsonSyntaxException;

public class ExternalEntity {

    public ExternalEntity() {
    }

    public ExternalEntity(String entity, int start, int length, JsonNode resolution) {
        entity = entity;
        start = start;
        length = length;
        resolution = resolution;
    }

    @JsonProperty(value = "entityName")
    private String entity;


    @JsonProperty(value = "startIndex")
    private int start;


    @JsonProperty(value = "entityLength")
    private int length = -1;

    @JsonProperty(value = "resolution")
    private JsonNode resolution;

    /// <summary>
    /// Gets or sets the start character index of the predicted entity.
    /// </summary>
    /// <value>
    /// The start character index of the predicted entity.
    /// </value>
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    /// <summary>
    /// Gets or sets the name of the entity to extend.
    /// </summary>
    /// <value>
    /// The name of the entity to extend.
    /// </value>
    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    /// <summary>
    /// Gets or sets the length of the predicted entity.
    /// </summary>
    /// <value>
    /// The length of the predicted entity.
    /// </value>
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    /// <summary>
    /// Gets or sets a user supplied custom resolution to return as the entity's prediction.
    /// </summary>
    /// <value>
    /// A user supplied custom resolution to return as the entity's prediction.
    /// </value>
    public JsonNode getResolution() {
        return resolution;
    }

    public void setResolution(JsonNode resolution) {
        this.resolution = resolution;
    }

    /// <summary>
    /// Validate the object.
    /// </summary>
    /// <exception cref="Microsoft.Rest.ValidationException">
    /// Thrown if validation fails.
    /// </exception>
    public void validate() throws JsonSyntaxException {
        if (entity == null || length == -1) {
            throw new JsonSyntaxException("ExternalEntity requires an EntityName and EntityLength > 0");
        }
    }
}
