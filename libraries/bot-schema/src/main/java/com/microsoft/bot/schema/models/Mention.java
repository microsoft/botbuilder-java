/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 */

package com.microsoft.bot.schema.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.EntityImpl;

/**
 * Mention information (entity type: "mention").
 */
public class Mention extends EntityImpl {
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

    /**
     * Get the mentioned value.
     *
     * @return the mentioned value
     */
    public ChannelAccount mentioned() {
        return this.mentioned;
    }

    /**
     * Set the mentioned value.
     *
     * @param mentioned the mentioned value to set
     * @return the Mention object itself.
     */
    public Mention withMentioned(ChannelAccount mentioned) {
        this.mentioned = mentioned;
        return this;
    }

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String text() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param text the text value to set
     * @return the Mention object itself.
     */
    public Mention withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the Mention object itself.
     */
    public Mention withType(String type) {
        this.type = type;
        return this;
    }

}
