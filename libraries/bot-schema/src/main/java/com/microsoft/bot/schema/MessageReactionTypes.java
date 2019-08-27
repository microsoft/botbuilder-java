/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for MessageReactionTypes.
 */
public enum MessageReactionTypes {
    /**
     * Enum value like.
     */
    LIKE("like"),

    /**
     * Enum value plusOne.
     */
    PLUS_ONE("plusOne");

    /**
     * The actual serialized value for a MessageReactionTypes instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     * @param withValue The string value.  Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    MessageReactionTypes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a MessageReactionTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed MessageReactionTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static MessageReactionTypes fromString(String value) {
        MessageReactionTypes[] items = MessageReactionTypes.values();
        for (MessageReactionTypes item : items) {
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
