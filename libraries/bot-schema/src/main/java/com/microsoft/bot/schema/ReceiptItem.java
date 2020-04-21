// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An item on a receipt card.
 */
public class ReceiptItem {
    /**
     * Title of the Card.
     */
    @JsonProperty(value = "title")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String title;

    /**
     * Subtitle appears just below Title field, differs from Title in font styling
     * only.
     */
    @JsonProperty(value = "subtitle")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String subtitle;

    /**
     * Text field appears just below subtitle, differs from Subtitle in font styling
     * only.
     */
    @JsonProperty(value = "text")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String text;

    /**
     * Image.
     */
    @JsonProperty(value = "image")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CardImage image;

    /**
     * Amount with currency.
     */
    @JsonProperty(value = "price")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String price;

    /**
     * Number of items of given kind.
     */
    @JsonProperty(value = "quantity")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String quantity;

    /**
     * This action will be activated when user taps on the Item bubble.
     */
    @JsonProperty(value = "tap")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CardAction tap;

    /**
     * Get the title value.
     *
     * @return the title value
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title value.
     *
     * @param withTitle the title value to set
     */
    public void setTitle(String withTitle) {
        this.title = withTitle;
    }

    /**
     * Get the subtitle value.
     *
     * @return the subtitle value
     */
    public String getSubtitle() {
        return this.subtitle;
    }

    /**
     * Set the subtitle value.
     *
     * @param withSubtitle the subtitle value to set
     */
    public void setSubtitle(String withSubtitle) {
        this.subtitle = withSubtitle;
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
     * Get the image value.
     *
     * @return the image value
     */
    public CardImage getImage() {
        return this.image;
    }

    /**
     * Set the image value.
     *
     * @param withImage the image value to set
     */
    public void setImage(CardImage withImage) {
        this.image = withImage;
    }

    /**
     * Get the price value.
     *
     * @return the price value
     */
    public String getPrice() {
        return this.price;
    }

    /**
     * Set the price value.
     *
     * @param withPrice the price value to set
     */
    public void setPrice(String withPrice) {
        this.price = withPrice;
    }

    /**
     * Get the quantity value.
     *
     * @return the quantity value
     */
    public String getQuantity() {
        return this.quantity;
    }

    /**
     * Set the quantity value.
     *
     * @param withQuantity the quantity value to set
     */
    public void setQuantity(String withQuantity) {
        this.quantity = withQuantity;
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
