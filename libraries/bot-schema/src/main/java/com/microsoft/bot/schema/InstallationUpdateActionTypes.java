// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for InstallationUpdateActionTypes.
 */
public enum InstallationUpdateActionTypes {
    /**
     * Enum value add.
     */
    ADD("add"),

    /**
     * Enum value remove.
     */
    REMOVE("remove");

    /**
     * The actual serialized value for a InstallationUpdateActionTypes instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     * 
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    InstallationUpdateActionTypes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a InstallationUpdateActionTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed InstallationUpdateActionTypes object, or null if unable to
     *         parse.
     */
    @JsonCreator
    public static InstallationUpdateActionTypes fromString(String value) {
        InstallationUpdateActionTypes[] items = InstallationUpdateActionTypes.values();
        for (InstallationUpdateActionTypes item : items) {
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
