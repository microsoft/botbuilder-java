// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Role of the entity behind the account (Example: User, Bot, etc.).
 */
public enum RoleTypes {
    /**
     * Enum value user.
     */
    USER("user"),

    /**
     * Enum value bot.
     */
    BOT("bot"),

    /**
     * Enum value skill.
     */
    SKILL("skill");

    /**
     * The actual serialized value for a RoleTypes instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     *
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    RoleTypes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a RoleTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed RoleTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static RoleTypes fromString(String value) {
        RoleTypes[] items = RoleTypes.values();
        for (RoleTypes item : items) {
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
