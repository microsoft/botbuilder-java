/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mention information (entity type: "mention").
 */
public class Mention implements EntitySerialization {
    /**
     * The mentioned user.
     */
    @JsonProperty(value = "mentioned")
    private ChannelAccount mentioned;

    /**
     * Sub Text which represents the mention (can be null or empty).
     */
    @JsonProperty(value = "text")
    private String text;

    /**
     * Type of this entity (RFC 3987 IRI).
     */
    @JsonProperty(value = "type")
    private String type;

    public Mention() {
        this.type = "mention";
    }

    /**
     * Get the mentioned value.
     *
     * @return the mentioned value
     */
    public ChannelAccount getMentioned() {
        return this.mentioned;
    }

    /**
     * Set the mentioned value.
     *
     * @param withMentioned the mentioned value to set
     */
    public void setMentioned(ChannelAccount withMentioned) {
        this.mentioned = withMentioned;
    }

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String getText() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param withText the text value to set
     */
    public void setText(String withText) {
        this.text = withText;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String getType() {
        return this.type;
    }
}
