// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
    public Boolean markdown;

    @JsonProperty(value = "facts")
    public List<O365ConnectorCardFact> facts;

    @JsonProperty(value = "images")
    public List<O365ConnectorCardImage> images;

    @JsonProperty(value = "potentialAction")
    public List<O365ConnectorCardActionBase> potentialAction;

    public String getTitle() {
        return title;
    }

    public void setTitle(String withTitle) {
        title = withTitle;
    }

    public String getText() {
        return text;
    }

    public void setText(String withText) {
        text = withText;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String withActivityTitle) {
        activityTitle = withActivityTitle;
    }

    public String getActivitySubtitle() {
        return activitySubtitle;
    }

    public void setActivitySubtitle(String withActivitySubtitle) {
        activitySubtitle = withActivitySubtitle;
    }

    public String getActivityText() {
        return activityText;
    }

    public void setActivityText(String withActivityText) {
        activityText = withActivityText;
    }

    public String getActivityImage() {
        return activityImage;
    }

    public void setActivityImage(String withActivityImage) {
        activityImage = withActivityImage;
    }

    public String getActivityImageType() {
        return activityImageType;
    }

    public void setActivityImageType(String withActivityImageType) {
        activityImageType = withActivityImageType;
    }

    public Boolean getMarkdown() {
        return markdown;
    }

    public void setMarkdown(Boolean withMarkdown) {
        markdown = withMarkdown;
    }

    public List<O365ConnectorCardFact> getFacts() {
        return facts;
    }

    public void setFacts(List<O365ConnectorCardFact> withFacts) {
        facts = withFacts;
    }

    public List<O365ConnectorCardImage> getImages() {
        return images;
    }

    public void setImages(List<O365ConnectorCardImage> withImages) {
        images = withImages;
    }

    public List<O365ConnectorCardActionBase> getPotentialAction() {
        return potentialAction;
    }

    public void setPotentialAction(List<O365ConnectorCardActionBase> withPotentialAction) {
        potentialAction = withPotentialAction;
    }
}
