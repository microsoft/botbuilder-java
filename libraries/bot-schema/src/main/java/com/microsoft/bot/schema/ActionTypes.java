// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines action types for clickable buttons.
 */
public enum ActionTypes {
    /**
     * Enum value openUrl.
     */
    OPEN_URL("openUrl"),

    /**
     * Enum value imBack.
     */
    IM_BACK("imBack"),

    /**
     * Enum value postBack.
     */
    POST_BACK("postBack"),

    /**
     * Enum value playAudio.
     */
    PLAY_AUDIO("playAudio"),

    /**
     * Enum value playVideo.
     */
    PLAY_VIDEO("playVideo"),

    /**
     * Enum value showImage.
     */
    SHOW_IMAGE("showImage"),

    /**
     * Enum value downloadFile.
     */
    DOWNLOAD_FILE("downloadFile"),

    /**
     * Enum value signin.
     */
    SIGNIN("signin"),

    /**
     * Enum value call.
     */
    CALL("call"),

    /**
     * Enum value messageBack.
     */
    MESSAGE_BACK("messageBack"),

    /**
     * Enum value invoke.
     */
    INVOKE("invoke");

    /**
     * The actual serialized value for a ActionTypes instance.
     */
    private String value;

    /**
     * Creates a ActionTypes enum from a string.
     * 
     * @param withValue The string value. Should be a valid enum value.
     * @throws IllegalArgumentException If the string doesn't match a valid value.
     */
    ActionTypes(String withValue) {
        this.value = withValue;
    }

    /**
     * Parses a serialized value to a ActionTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ActionTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static ActionTypes fromString(String value) {
        ActionTypes[] items = ActionTypes.values();
        for (ActionTypes item : items) {
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
