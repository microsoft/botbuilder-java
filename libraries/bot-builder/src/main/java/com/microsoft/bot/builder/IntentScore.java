package com.microsoft.bot.builder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class IntentScore {
    @JsonProperty
    private double score;

    private HashMap<String, JsonNode> properties = new HashMap<>();

    public double getScore() {
        return score;
    }

    public void setScore(double withScore) {
        score = withScore;
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }
}
