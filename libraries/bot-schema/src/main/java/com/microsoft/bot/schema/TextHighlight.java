// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Refers to a substring of content within another field.
 */
public class TextHighlight {
    /**
     * Defines the snippet of text to highlight.
     */
    @JsonProperty(value = "text")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String text;

    /**
     * Occurrence of the text field within the referenced text, if multiple exist.
     */
    @JsonProperty(value = "occurence")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer occurence;

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String getText() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param withText the text value to set
     */
    public void setText(String withText) {
        this.text = withText;
    }

    /**
     * Get the occurence value.
     *
     * @return the occurence value
     */
    public Integer getOccurence() {
        return this.occurence;
    }

    /**
     * Set the occurence value.
     *
     * @param withOccurence the occurence value to set
     */
    public void setOccurence(Integer withOccurence) {
        this.occurence = withOccurence;
    }
}
