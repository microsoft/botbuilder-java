// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Score plus any extra information about an intent.
 */
public class IntentScore {
    /**
     * Confidence in an intent.
     */
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private double score;

    /**
     * Extra properties to include in the results.
     */
    private HashMap<String, JsonNode> properties = new HashMap<>();

    /**
     * Gets confidence in an intent.
     * 
     * @return Confidence in an intent.
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets confidence in an intent.
     * 
     * @param withScore Confidence in an intent.
     */
    public void setScore(double withScore) {
        score = withScore;
    }

    /**
     * Gets extra properties to include in the results.
     * 
     * @return Any extra properties to include in the results.
     */
    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    /**
     * Sets extra properties to include in the results.
     * 
     * @param key   The key of the property.
     * @param value The JsonNode value of the property.
     */
    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }
}
