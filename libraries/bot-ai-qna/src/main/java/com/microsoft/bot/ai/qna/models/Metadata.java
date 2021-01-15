// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the Metadata object sent as part of QnA Maker requests.
 */
public class Metadata implements Serializable {
    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    /**
     * Gets the name for the Metadata property.
     *
     * @return A string.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name for the Metadata property.
     *
     * @param withName A string.
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * Gets the value for the Metadata property.
     *
     * @return A string.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value for the Metadata property.
     *
     * @param withValue A string.
     */
    public void setValue(String withValue) {
        this.value = withValue;
    }
}
