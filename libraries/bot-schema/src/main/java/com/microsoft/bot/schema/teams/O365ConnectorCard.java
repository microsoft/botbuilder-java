// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * O365 connector card.
 */
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
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<O365ConnectorCardSection> sections;

    @JsonProperty(value = "potentialAction")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<O365ConnectorCardActionBase> potentialAction;

    /**
     * Gets the title of the card.
     * 
     * @return The card title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the card.
     * 
     * @param withTitle The card title.
     */
    public void setTitle(String withTitle) {
        title = withTitle;
    }

    /**
     * Gets the text for the card.
     * 
     * @return The card text.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text for the card.
     * 
     * @param withText The card text.
     */
    public void setText(String withText) {
        text = withText;
    }

    /**
     * Gets the summary for the card.
     * 
     * @return The card summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summary for the card.
     * 
     * @param withSummary The card summary.
     */
    public void setSummary(String withSummary) {
        summary = withSummary;
    }

    /**
     * Gets the theme color for the card.
     * 
     * @return The card color.
     */
    public String getThemeColor() {
        return themeColor;
    }

    /**
     * Sets the theme color for the card.
     * 
     * @param withThemeColor The card color.
     */
    public void setThemeColor(String withThemeColor) {
        themeColor = withThemeColor;
    }

    /**
     * Gets the list of sections for the current card.
     * 
     * @return The card sections.
     */
    public List<O365ConnectorCardSection> getSections() {
        return sections;
    }

    /**
     * Sets the of sections for the current card.
     * 
     * @param withSections The card sections.
     */
    public void setSections(List<O365ConnectorCardSection> withSections) {
        sections = withSections;
    }

    /**
     * Gets the of actions for the current card.
     * 
     * @return The card actions.
     */
    public List<O365ConnectorCardActionBase> getPotentialAction() {
        return potentialAction;
    }

    /**
     * Sets the of actions for the current card.
     * 
     * @param withPotentialAction The card actions.
     */
    public void setPotentialAction(List<O365ConnectorCardActionBase> withPotentialAction) {
        potentialAction = withPotentialAction;
    }
}
