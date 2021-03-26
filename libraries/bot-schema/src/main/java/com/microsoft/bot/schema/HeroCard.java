// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * A Hero card (card with a single, large image).
 */
public class HeroCard {
    @JsonIgnore
    public static final String CONTENTTYPE = "application/vnd.microsoft.card.hero";

    /**
     * Title of the card.
     */
    @JsonProperty(value = "title")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String title;

    /**
     * Subtitle of the card.
     */
    @JsonProperty(value = "subtitle")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String subtitle;

    /**
     * Text for the card.
     */
    @JsonProperty(value = "text")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String text;

    /**
     * Array of images for the card.
     */
    @JsonProperty(value = "images")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CardImage> images;

    /**
     * Set of actions applicable to the current card.
     */
    @JsonProperty(value = "buttons")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CardAction> buttons;

    /**
     * This action will be activated when user taps on the card itself.
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
     * Get the images value.
     *
     * @return the images value
     */
    public List<CardImage> getImages() {
        return this.images;
    }

    /**
     * Set the images value.
     *
     * @param withImages the images value to set
     */
    public void setImages(List<CardImage> withImages) {
        this.images = withImages;
    }

    /**
     * Set the images value.
     *
     * @param withImages the images value to set
     */
    public void setImages(CardImage... withImages) {
        this.images = Arrays.asList(withImages);
    }

    /**
     * Get the buttons value.
     *
     * @return the buttons value
     */
    public List<CardAction> getButtons() {
        return this.buttons;
    }

    /**
     * Set the buttons value.
     *
     * @param withButtons the buttons value to set
     */
    public void setButtons(List<CardAction> withButtons) {
        this.buttons = withButtons;
    }

    /**
     * Set the buttons value.
     *
     * @param withButtons the buttons value to set
     */
    public void setButtons(CardAction... withButtons) {
        this.buttons = Arrays.asList(withButtons);
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

    /**
     * Creates an @{link Attachment} for this card.
     * 
     * @return An Attachment object containing the card.
     */
    public Attachment toAttachment() {
        Attachment attachment = new Attachment();
        attachment.setContent(this);
        attachment.setContentType(CONTENTTYPE);
        return attachment;
    }
}
