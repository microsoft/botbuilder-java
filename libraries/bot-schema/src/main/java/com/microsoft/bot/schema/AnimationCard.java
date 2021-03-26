// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * An animation card (Ex: gif or short video clip).
 */
public class AnimationCard {
    @JsonIgnore
    public static final String CONTENTTYPE = "application/vnd.microsoft.card.animation";

    /**
     * Title of this card.
     */
    @JsonProperty(value = "title")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String title;

    /**
     * Subtitle of this card.
     */
    @JsonProperty(value = "subtitle")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String subtitle;

    /**
     * Text of this card.
     */
    @JsonProperty(value = "text")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String text;

    /**
     * Thumbnail placeholder.
     */
    @JsonProperty(value = "image")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ThumbnailUrl image;

    /**
     * Media URLs for this card. When this field contains more than one URL, each
     * URL is an alternative format of the same content.
     */
    @JsonProperty(value = "media")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MediaUrl> media;

    /**
     * Actions on this card.
     */
    @JsonProperty(value = "buttons")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CardAction> buttons;

    /**
     * This content may be shared with others (default:true).
     */
    @JsonProperty(value = "shareable")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean shareable;

    /**
     * Should the client loop playback at end of content (default:true).
     */
    @JsonProperty(value = "autoloop")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean autoloop;

    /**
     * Should the client automatically start playback of media in this card
     * (default:true).
     */
    @JsonProperty(value = "autostart")
    private boolean autostart;

    /**
     * Aspect ratio of thumbnail/media placeholder, allowed values are "16:9" and
     * "4:3".
     */
    @JsonProperty(value = "aspect")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String aspect;

    /**
     * Describes the length of the media content without requiring a receiver to
     * open the content. Formatted as an ISO 8601 Duration field.
     */
    @JsonProperty(value = "duration")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String duration;

    /**
     * Supplementary parameter for this card.
     */
    @JsonProperty(value = "value")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object value;

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
    public ThumbnailUrl getImage() {
        return this.image;
    }

    /**
     * Set the image value.
     *
     * @param withImage the image value to set
     */
    public void setImage(ThumbnailUrl withImage) {
        this.image = withImage;
    }

    /**
     * Get the media value.
     *
     * @return the media value
     */
    public List<MediaUrl> getMedia() {
        return this.media;
    }

    /**
     * Set the media value.
     *
     * @param withMedia the media value to set
     */
    public void setMedia(List<MediaUrl> withMedia) {
        this.media = withMedia;
    }

    /**
     * Set the media value.
     *
     * @param withMedia the media value to set
     */
    public void setMedia(MediaUrl... withMedia) {
        this.media = Arrays.asList(withMedia);
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
     * Get the shareable value.
     *
     * @return the shareable value
     */
    public boolean getShareable() {
        return this.shareable;
    }

    /**
     * Set the shareable value.
     *
     * @param withShareable the shareable value to set
     */
    public void setShareable(boolean withShareable) {
        this.shareable = withShareable;
    }

    /**
     * Get the autoloop value.
     *
     * @return the autoloop value
     */
    public boolean getAutoloop() {
        return this.autoloop;
    }

    /**
     * Set the autoloop value.
     *
     * @param withAutoloop the autoloop value to set
     */
    public void setAutoloop(boolean withAutoloop) {
        this.autoloop = withAutoloop;
    }

    /**
     * Get the autostart value.
     *
     * @return the autostart value
     */
    public boolean getAutostart() {
        return this.autostart;
    }

    /**
     * Set the autostart value.
     *
     * @param withAutostart the autostart value to set
     */
    public void setAutostart(boolean withAutostart) {
        this.autostart = withAutostart;
    }

    /**
     * Get the aspect value.
     *
     * @return the aspect value
     */
    public String getAspect() {
        return this.aspect;
    }

    /**
     * Set the aspect value.
     *
     * @param withAspect the aspect value to set
     */
    public void setAspect(String withAspect) {
        this.aspect = withAspect;
    }

    /**
     * Gets the duration value.
     * 
     * @return The animation duration.
     */
    public String getDuration() {
        return this.duration;
    }

    /**
     * Sets the duration value.
     *
     * @param withDuration the duration value to set
     */
    public void setDuration(String withDuration) {
        this.duration = withDuration;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param withValue the value value to set
     */
    public void setValue(Object withValue) {
        this.value = withValue;
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
