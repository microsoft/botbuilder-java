/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

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

    public static MessageReaction clone(MessageReaction messageReaction) {
        if (messageReaction == null) {
            return null;
        }

        return new MessageReaction() {{
           setType(messageReaction.getType());
        }};
    }

    public static List<MessageReaction> cloneList(List<MessageReaction> messageReactions) {
        if (messageReactions == null) {
            return null;
        }

        return messageReactions.stream()
            .map(messageReaction -> MessageReaction.clone(messageReaction))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public MessageReaction() {

    }

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