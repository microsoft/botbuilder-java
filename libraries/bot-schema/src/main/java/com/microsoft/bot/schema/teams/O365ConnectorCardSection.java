// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * O365 connector card section.
 */
public class O365ConnectorCardSection {
    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "text")
    private String text;

    @JsonProperty(value = "activityTitle")
    private String activityTitle;

    @JsonProperty(value = "activitySubtitle")
    private String activitySubtitle;

    @JsonProperty(value = "activityText")
    private String activityText;

    @JsonProperty(value = "activityImage")
    private String activityImage;

    @JsonProperty(value = "activityImageType")
    private String activityImageType;

    @JsonProperty(value = "markdown")
    private Boolean markdown;

    @JsonProperty(value = "facts")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<O365ConnectorCardFact> facts;

    @JsonProperty(value = "images")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<O365ConnectorCardImage> images;

    @JsonProperty(value = "potentialAction")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<O365ConnectorCardActionBase> potentialAction;

    /**
     * Gets title of the section.
     * 
     * @return The section title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title of the section.
     * 
     * @param withTitle The section title.
     */
    public void setTitle(String withTitle) {
        title = withTitle;
    }

    /**
     * Gets text for the section.
     * 
     * @return The section text.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets text for the section.
     * 
     * @param withText The section text.
     */
    public void setText(String withText) {
        text = withText;
    }

    /**
     * Gets the activity title.
     * 
     * @return The activity title.
     */
    public String getActivityTitle() {
        return activityTitle;
    }

    /**
     * Set the activity title.
     * 
     * @param withActivityTitle The activity title.
     */
    public void setActivityTitle(String withActivityTitle) {
        activityTitle = withActivityTitle;
    }

    /**
     * Gets the activity subtitle.
     * 
     * @return The activity subtitle.
     */
    public String getActivitySubtitle() {
        return activitySubtitle;
    }

    /**
     * Sets the activity subtitle.
     * 
     * @param withActivitySubtitle The activity subtitle.
     */
    public void setActivitySubtitle(String withActivitySubtitle) {
        activitySubtitle = withActivitySubtitle;
    }

    /**
     * Gets the activity text.
     * 
     * @return The activity text.
     */
    public String getActivityText() {
        return activityText;
    }

    /**
     * Sets the activity text.
     * 
     * @param withActivityText The activity text.
     */
    public void setActivityText(String withActivityText) {
        activityText = withActivityText;
    }

    /**
     * Gets the activity image.
     * 
     * @return The activity image.
     */
    public String getActivityImage() {
        return activityImage;
    }

    /**
     * Sets the activity image.
     * 
     * @param withActivityImage The activity image.
     */
    public void setActivityImage(String withActivityImage) {
        activityImage = withActivityImage;
    }

    /**
     * Describes how Activity image is rendered. Possible values include: 'avatar',
     * 'article'
     * 
     * @return The activity image type.
     */
    public String getActivityImageType() {
        return activityImageType;
    }

    /**
     * Sets how Activity image is rendered. Possible values include: 'avatar',
     * 'article'
     * 
     * @param withActivityImageType The activity image type.
     */
    public void setActivityImageType(String withActivityImageType) {
        activityImageType = withActivityImageType;
    }

    /**
     * Indicates markdown for all text contents. Default value is true.
     * 
     * @return True if text is markdown.
     */
    public Boolean getMarkdown() {
        return markdown;
    }

    /**
     * Sets markdown for all text contents.
     * 
     * @param withMarkdown True to use markdown for text content.
     */
    public void setMarkdown(Boolean withMarkdown) {
        markdown = withMarkdown;
    }

    /**
     * List of facts for the current section.
     * 
     * @return Facts for the section.
     */
    public List<O365ConnectorCardFact> getFacts() {
        return facts;
    }

    /**
     * Set list of facts for the current section.
     * 
     * @param withFacts Facts for the section.
     */
    public void setFacts(List<O365ConnectorCardFact> withFacts) {
        facts = withFacts;
    }

    /**
     * List of images for the current section.
     * 
     * @return Images for the section.
     */
    public List<O365ConnectorCardImage> getImages() {
        return images;
    }

    /**
     * Set list of images for the current section.
     * 
     * @param withImages Images for the section.
     */
    public void setImages(List<O365ConnectorCardImage> withImages) {
        images = withImages;
    }

    /**
     * List of actions for the current section.
     * 
     * @return Actions for the section.
     */
    public List<O365ConnectorCardActionBase> getPotentialAction() {
        return potentialAction;
    }

    /**
     * Sets list of actions for the current section.
     * 
     * @param withPotentialAction Actions for the section.
     */
    public void setPotentialAction(List<O365ConnectorCardActionBase> withPotentialAction) {
        potentialAction = withPotentialAction;
    }
}
