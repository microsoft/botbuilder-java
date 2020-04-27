// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An image on a card.
 */
public class CardImage {
    /**
     * URL thumbnail image for major content property.
     */
    @JsonProperty(value = "url")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String url;

    /**
     * Image description intended for screen readers.
     */
    @JsonProperty(value = "alt")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String alt;

    /**
     * Action assigned to specific Attachment.
     */
    @JsonProperty(value = "tap")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CardAction tap;

    /**
     * Creates a new CardImage.
     */
    public CardImage() {

    }

    /**
     * Creates a new CardImage with an initial URL.
     * 
     * @param withUrl The URL for the image.
     */
    public CardImage(String withUrl) {
        setUrl(withUrl);
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param withUrl the url value to set
     */
    public void setUrl(String withUrl) {
        this.url = withUrl;
    }

    /**
     * Get the alt value.
     *
     * @return the alt value
     */
    public String getAlt() {
        return this.alt;
    }

    /**
     * Set the alt value.
     *
     * @param withAlt the alt value to set
     */
    public void setAlt(String withAlt) {
        this.alt = withAlt;
    }

    /**
     * Get the tap value.
     *
     * @return the tap value
     */
    public CardAction getTap() {
        return this.tap;
    }

    /**
     * Set the tap value.
     *
     * @param withTap the tap value to set
     */
    public void setTap(CardAction withTap) {
        this.tap = withTap;
    }
}
