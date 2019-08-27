/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ActivityTypes.
 */
public enum ActivityTypes {
    /**
     * Enum value message.
     */
    MESSAGE("message"),

    /**
     * Enum value contactRelationUpdate.
     */
    CONTACT_RELATION_UPDATE("contactRelationUpdate"),

    /**
     * Enum value conversationUpdate.
     */
    CONVERSATION_UPDATE("conversationUpdate"),

    /**
     * Enum value typing.
     */
    TYPING("typing"),

    /**
     * Enum value endOfConversation.
     */
    END_OF_CONVERSATION("endOfConversation"),

    /**
     * Enum value event.
     */
    EVENT("event"),

    /**
     * Enum value invoke.
     */
    INVOKE("invoke"),

    /**
     * Enum value deleteUserData.
     */
    DELETE_USER_DATA("deleteUserData"),

    /**
     * Enum value messageUpdate.
     */
    MESSAGE_UPDATE("messageUpdate"),

    /**
     * Enum value messageDelete.
     */
    MESSAGE_DELETE("messageDelete"),

    /**
     * Enum value installationUpdate.
     */
    INSTALLATION_UPDATE("installationUpdate"),

    /**
     * Enum value messageReaction.
     */
    MESSAGE_REACTION("messageReaction"),

    /**
     * Enum value suggestion.
     */
    SUGGESTION("suggestion"),

    /**
     * Enum value trace.
     */
    TRACE("trace"),

    /**
     * Enum value handoff.
     */
    HANDOFF("handoff");

    /**
     * The actual serialized value for a ActivityTypes instance.
     */
    private String value;

    /**
     * Creates a ActivityTypes enum from a string.
     * @param withValue The string value.  Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    ActivityTypes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a ActivityTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ActivityTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static ActivityTypes fromString(String value) {
        ActivityTypes[] items = ActivityTypes.values();
        for (ActivityTypes item : items) {
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
