// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.sample.dialogskillbot.cognitivemodels;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Strongly typed information corresponding to LUIS $instance value.
 */
public class InstanceData {

    @JsonProperty(value = "startIndex")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private int startIndex;

    @JsonProperty(value = "endIndex")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private int endIndex;

    @JsonProperty(value = "text")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String text;

    @JsonProperty(value = "score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Double score;

    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    @JsonProperty(value = "subtype")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String subtype;

    private Map<String, Object> properties;

    /**
     * Gets 0-super. index in the analyzed text for where entity
     * starts.
     * @return the StartIndex value as a int.
     */
    public int getStartIndex() {
        return this.startIndex;
    }

    /**
     * Sets 0-super. index in the analyzed text for where entity
     * starts.
     * @param withStartIndex The StartIndex value.
     */
    public void setStartIndex(int withStartIndex) {
        this.startIndex = withStartIndex;
    }

    /**
     * Gets 0-super. index of the first character beyond the recognized
     * entity.
     * @return the EndIndex value as a int.
     */
    public int getEndIndex() {
        return this.endIndex;
    }

    /**
     * Sets 0-super. index of the first character beyond the recognized
     * entity.
     * @param withEndIndex The EndIndex value.
     */
    public void setEndIndex(int withEndIndex) {
        this.endIndex = withEndIndex;
    }

    /**
     * Gets word broken and normalized text for the entity.
     * @return the Text value as a String.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets word broken and normalized text for the entity.
     * @param withText The Text value.
     */
    public void setText(String withText) {
        this.text = withText;
    }

    /**
     * Gets optional confidence in the recognition.
     * @return the Score value as a double?.
     */
    public Double getScore() {
        return this.score;
    }

    /**
     * Sets optional confidence in the recognition.
     * @param withScore The Score value.
     */
    public void setScore(Double withScore) {
        this.score = withScore;
    }

    /**
     * Gets optional type for the entity.
     * @return the Type value as a String.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets optional type for the entity.
     * @param withType The Type value.
     */
    public void setType(String withType) {
        this.type = withType;
    }

    /**
     * Gets optional subtype for the entity.
     * @return the Subtype value as a String.
     */
    public String getSubtype() {
        return this.subtype;
    }

    /**
     * Sets optional subtype for the entity.
     * @param withSubtype The Subtype value.
     */
    public void setSubtype(String withSubtype) {
        this.subtype = withSubtype;
    }

    /**
     * Gets any extra properties.
     * @return the Properties value as a Map<String, Object>.
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * Sets any extra properties.
     * @param withProperties The Properties value.
     */
    @JsonAnySetter
    public void setProperties(Map<String, Object> withProperties) {
        this.properties = withProperties;
    }

}
