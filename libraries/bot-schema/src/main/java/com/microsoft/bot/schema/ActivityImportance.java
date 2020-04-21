// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ActivityImportance.
 */
public enum ActivityImportance {
    /**
     * Enum value low.
     */
    LOW("low"),

    /**
     * Enum value normal.
     */
    NORMAL("normal"),

    /**
     * Enum value high.
     */
    HIGH("high");

    /**
     * The actual serialized value for a ActivityImportance instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     * 
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    ActivityImportance(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a ActivityImportance instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ActivityImportance object, or null if unable to parse.
     */
    @JsonCreator
    public static ActivityImportance fromString(String value) {
        ActivityImportance[] items = ActivityImportance.values();
        for (ActivityImportance item : items) {
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
