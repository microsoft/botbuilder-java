// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ContactRelationUpdateActionTypes.
 */
public enum ContactRelationUpdateActionTypes {
    /**
     * Enum value add.
     */
    ADD("add"),

    /**
     * Enum value remove.
     */
    REMOVE("remove");

    /**
     * The actual serialized value for a ContactRelationUpdateActionTypes instance.
     */
    private String value;

    /**
     * Creates a ContactRelationUpdateActionTypes enum from a string.
     * 
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    ContactRelationUpdateActionTypes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a ContactRelationUpdateActionTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ContactRelationUpdateActionTypes object, or null if unable
     *         to parse.
     */
    @JsonCreator
    public static ContactRelationUpdateActionTypes fromString(String value) {
        ContactRelationUpdateActionTypes[] items = ContactRelationUpdateActionTypes.values();
        for (ContactRelationUpdateActionTypes item : items) {
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
