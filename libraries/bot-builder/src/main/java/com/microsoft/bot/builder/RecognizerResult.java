// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class RecognizerResult implements RecognizerConvert {
    @JsonProperty
    JsonNode entities;
    @JsonProperty
    private String text;
    @JsonProperty
    private String alteredText;
    @JsonProperty
    private Map<String, IntentScore> intents;
    private HashMap<String, JsonNode> properties = new HashMap<>();

    public IntentScore getTopScoringIntent() {
        if (getIntents() == null) {
            throw new IllegalArgumentException("RecognizerResult.Intents cannot be null");
        }

        IntentScore topIntent = new IntentScore();
        for (Map.Entry<String, IntentScore> intent : getIntents().entrySet()) {
            double score = intent.getValue().getScore();
            if (score > topIntent.getScore()) {
                topIntent = intent.getValue();
            }
        }

        return topIntent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAlteredText() {
        return alteredText;
    }

    public void setAlteredText(String alteredText) {
        this.alteredText = alteredText;
    }

    public Map<String, IntentScore> getIntents() {
        return intents;
    }

    public void setIntents(Map<String, IntentScore> intents) {
        this.intents = intents;
    }

    public JsonNode getEntities() {
        return entities;
    }

    public void setEntities(JsonNode entities) {
        this.entities = entities;
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }

    @Override
    public void convert(Object result) {
        setText(((RecognizerResult) result).getText());
        setAlteredText((((RecognizerResult) result).getAlteredText()));
        setIntents(((RecognizerResult) result).getIntents());
        setEntities(((RecognizerResult) result).getEntities());

        for (String key : ((RecognizerResult) result).getProperties().keySet()) {
            setProperties(key, ((RecognizerResult) result).getProperties().get(key));
        }
    }
}
