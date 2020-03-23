// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class O365ConnectorCard {
    /**
     * Content type to be used in the type property.
     */
    public static final String CONTENT_TYPE = "application/vnd.microsoft.teams.card.o365connector";

    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "text")
    private String text;

    @JsonProperty(value = "summary")
    private String summary;

    @JsonProperty(value = "themeColor")
    private String themeColor;

    @JsonProperty(value = "sections")
    public List<O365ConnectorCardSection> sections;

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String withSummary) {
        summary = withSummary;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String withThemeColor) {
        themeColor = withThemeColor;
    }

    public List<O365ConnectorCardSection> getSections() {
        return sections;
    }

    public void setSections(List<O365ConnectorCardSection> withSections) {
        sections = withSections;
    }

    public List<O365ConnectorCardActionBase> getPotentialAction() {
        return potentialAction;
    }

    public void setPotentialAction(List<O365ConnectorCardActionBase> withPotentialAction) {
        potentialAction = withPotentialAction;
    }
}
