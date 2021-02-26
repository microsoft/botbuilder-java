// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for EndOfConversationCodes.
 */
public enum EndOfConversationCodes {
    /**
     * Enum value unknown.
     */
    UNKNOWN("unknown"),

    /**
     * Enum value completedSuccessfully.
     */
    COMPLETED_SUCCESSFULLY("completedSuccessfully"),

    /**
     * Enum value userCancelled.
     */
    USER_CANCELLED("userCancelled"),

    /**
     * Enum value botTimedOut.
     */
    BOT_TIMED_OUT("botTimedOut"),

    /**
     * Enum value botIssuedInvalidMessage.
     */
    BOT_ISSUED_INVALID_MESSAGE("botIssuedInvalidMessage"),

    /**
     * Enum value channelFailed.
     */
    CHANNEL_FAILED("channelFailed"),

    /**
     * Enum value skillError.
     */
    SKILL_ERROR("skillError"),

    /**
     * Enum value channelFailed.
     */
    ROOT_SKILL_ERROR("rootSkillError");

    /**
     * The actual serialized value for a EndOfConversationCodes instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     *
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    EndOfConversationCodes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a EndOfConversationCodes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed EndOfConversationCodes object, or null if unable to parse.
     */
    @JsonCreator
    public static EndOfConversationCodes fromString(String value) {
        EndOfConversationCodes[] items = EndOfConversationCodes.values();
        for (EndOfConversationCodes item : items) {
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
