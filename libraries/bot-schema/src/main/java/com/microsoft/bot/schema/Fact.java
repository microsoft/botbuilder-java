// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Set of key-value pairs. Advantage of this section is that key and value
 * properties will be rendered with default style information with some
 * delimiter between them. So there is no need for developer to specify style
 * information.
 */
public class Fact {
    /**
     * The key for this Fact.
     */
    @JsonProperty(value = "key")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String key;

    /**
     * The value for this Fact.
     */
    @JsonProperty(value = "value")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String value;

    /**
     * Creates a new fact.
     */
    public Fact() {
    }

    /**
     * Creates a new fact.
     * @param withKey The key value.
     * @param withValue The value
     */
    public Fact(String withKey, String withValue) {
        key = withKey;
        value = withValue;
    }

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Set the key value.
     *
     * @param withKey the key value to set
     */
    public void setKey(String withKey) {
        this.key = withKey;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param withValue the value value to set
     */
    public void setValue(String withValue) {
        this.value = withValue;
    }
}
