// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for AttachmentLayoutTypes.
 */
public enum AttachmentLayoutTypes {
    /**
     * Enum value list.
     */
    LIST("list"),

    /**
     * Enum value carousel.
     */
    CAROUSEL("carousel");

    /**
     * The actual serialized value for a AttachmentLayoutTypes instance.
     */
    private String value;

    /**
     * Creates a AttachmentLayoutTypes enum from a string.
     * 
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    AttachmentLayoutTypes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a AttachmentLayoutTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AttachmentLayoutTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static AttachmentLayoutTypes fromString(String value) {
        AttachmentLayoutTypes[] items = AttachmentLayoutTypes.values();
        for (AttachmentLayoutTypes item : items) {
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
