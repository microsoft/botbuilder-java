// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for TextFormatTypes.
 */
public enum TextFormatTypes {
    /**
     * Enum value markdown.
     */
    MARKDOWN("markdown"),

    /**
     * Enum value plain.
     */
    PLAIN("plain"),

    /**
     * Enum value xml.
     */
    XML("xml");

    /**
     * The actual serialized value for a TextFormatTypes instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     * 
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    TextFormatTypes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a TextFormatTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed TextFormatTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static TextFormatTypes fromString(String value) {
        TextFormatTypes[] items = TextFormatTypes.values();
        for (TextFormatTypes item : items) {
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
