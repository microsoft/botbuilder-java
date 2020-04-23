// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for InputHints.
 */
public enum InputHints {
    /**
     * Enum value acceptingInput.
     */
    ACCEPTING_INPUT("acceptingInput"),

    /**
     * Enum value ignoringInput.
     */
    IGNORING_INPUT("ignoringInput"),

    /**
     * Enum value expectingInput.
     */
    EXPECTING_INPUT("expectingInput");

    /**
     * The actual serialized value for a InputHints instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     * 
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    InputHints(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a InputHints instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed InputHints object, or null if unable to parse.
     */
    @JsonCreator
    public static InputHints fromString(String value) {
        InputHints[] items = InputHints.values();
        for (InputHints item : items) {
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
