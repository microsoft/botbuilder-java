// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;

/**
 * Invoke ('tab/submit') request value payload data.
 */
public class TabSubmitData {
    @JsonProperty(value = "type")
    private String type;

    /**
     * Holds the overflow properties that aren't first class properties in the
     * object. This allows extensibility while maintaining the object.
     */
    private HashMap<String, JsonNode> properties = new HashMap<>();

    /**
     * Initializes a new instance of the class.
     */
    public TabSubmitData() {

    }

    /**
     * Gets the type for this TabSubmitData.
     *
     * @return Currently, 'tab/submit'.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type for this TabSubmitData.

     * @param withType Currently, 'tab/submit'.
     */
    public void setType(String withType) {
        type = withType;
    }

    /**
     * Holds the overflow properties that aren't first class properties in the
     * object. This allows extensibility while maintaining the object.
     *
     * @return Map of additional properties.
     */
    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    /**
     * Holds the overflow properties that aren't first class properties in the
     * object. This allows extensibility while maintaining the object.
     *
     * @param key       The key of the property to set.
     * @param withValue The value for the property.
     */
    @JsonAnySetter
    public void setProperties(String key, JsonNode withValue) {
        this.properties.put(key, withValue);
    }
}
