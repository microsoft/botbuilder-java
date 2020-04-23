// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Indicates whether the semantic action is starting, continuing, or done.
 */
public enum SemanticActionStates {
    /**
     * Enum value start.
     */
    START("start"),

    /**
     * Enum value continue.
     */
    CONTINUE("continue"),

    /**
     * Enum value done.
     */
    DONE("done");

    /**
     * The actual serialized value for a SemanticActionStates instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     * 
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    SemanticActionStates(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a ActivityTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ActivityTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static SemanticActionStates fromString(String value) {
        SemanticActionStates[] items = SemanticActionStates.values();
        for (SemanticActionStates item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
