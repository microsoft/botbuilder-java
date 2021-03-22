// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Values for deliveryMode field.
 */
public enum DeliveryModes {
    /**
     * The mode value for normal delivery modes.
     */
    NORMAL("normal"),

    /**
     * The mode value for notification delivery modes.
     */
    NOTIFICATION("notification"),

    /**
     * The value for expected replies delivery modes.
     */
    EXPECT_REPLIES("expectReplies"),

    /**
     * The value for ephemeral delivery modes.
     */
    EPHEMERAL("ephemeral");

    /**
     * The actual serialized value for a DeliveryModes instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     * 
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    DeliveryModes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a ActivityTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ActivityTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static DeliveryModes fromString(String value) {
        DeliveryModes[] items = DeliveryModes.values();
        for (DeliveryModes item : items) {
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
