/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata object pertaining to an activity.
 */
public class Entity {
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     */
    private HashMap<String, JsonNode> properties = new HashMap<String, JsonNode>();

    /**
     * Type of this entity (RFC 3987 IRI).
     */
    @JsonProperty(value = "type")
    private String type;

    /**
     * Get the {@link #type} value.
     * @return the type value
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the {@link #type} value.
     * @param withType the type value to set
     */
    public void setType(String withType) {
        this.type = withType;
    }

    /**
     * @see #properties
     */
    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    /**
     * @see #properties
     */
    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }

    /**
     * Retrieve internal payload.
     *
     * @param classType of type T
     * @return
     */
    public <T extends EntitySerialization> T getAs(Class<T> classType) {

        // Serialize
        String tempJson;
        try {
            tempJson = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        // Deserialize
        T newObj = null;
        try {
            newObj = objectMapper.readValue(tempJson, classType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return newObj;
    }

    /**
     * Set internal payload.
     *
     * This is only intended to be used with other Enitity classes:
     * @see Mention
     * @see Place
     * @see GeoCoordinates
     *
     * @param obj of type T
     * @param obj
     */
    public <T extends EntitySerialization> Entity setAs(T obj) {
        // Serialize
        String tempJson;
        try {
            tempJson = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }

        Entity tempEntity;
        try {
            tempEntity = objectMapper.readValue(tempJson, Entity.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        for (Map.Entry<String, JsonNode> entry : tempEntity.properties.entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }

        this.type = tempEntity.getType();

        return this;
    }
}
