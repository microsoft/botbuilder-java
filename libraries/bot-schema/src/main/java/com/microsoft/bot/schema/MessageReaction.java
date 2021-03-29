// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Message reaction object.
 */
public class MessageReaction {
    /**
     * Message reaction type. Possible values include: 'like', 'plusOne'.
     */
    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    /**
     * Performs a deep copy of a MessageReaction.
     *
     * @param messageReaction The MessageReaction to copy.
     * @return A clone of the MessageReaction.
     */
    public static MessageReaction clone(MessageReaction messageReaction) {
        if (messageReaction == null) {
            return null;
        }
        MessageReaction cloned = new MessageReaction();
        cloned.setType(messageReaction.getType());
        return cloned;
    }

    /**
     * Performs a deep copy of a List of MessageReactions.
     *
     * @param messageReactions The List to clone.
     * @return A clone of the List.
     */
    public static List<MessageReaction> cloneList(List<MessageReaction> messageReactions) {
        if (messageReactions == null) {
            return null;
        }

        return messageReactions.stream()
            .map(messageReaction -> MessageReaction.clone(messageReaction))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Empty MessageReaction.
     */
    public MessageReaction() {

    }

    /**
     * MessageReaction of a type.
     *
     * @param withType The type.
     */
    public MessageReaction(String withType) {
        type = withType;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param withType the type value to set
     */
    public void setType(String withType) {
        this.type = withType;
    }
}
