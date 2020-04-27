// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O365 connector card input for ActionCard action.
 */
public class O365ConnectorCardInputBase {
    @JsonProperty(value = "@type")
    private String type;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "isRequired")
    private Boolean isRequired;

    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "value")
    private String value;

    /**
     * Gets input type name. Possible values include: 'textInput', 'dateInput',
     * 'multichoiceInput'
     * 
     * @return The input type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets input type name. Possible values include: 'textInput', 'dateInput',
     * 'multichoiceInput'
     * 
     * @param withType The input type.
     */
    public void setType(String withType) {
        type = withType;
    }

    /**
     * Gets the input Id. It must be unique per entire O365 connector card.
     * 
     * @return The card id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the input Id. It must be unique per entire O365 connector card.
     * 
     * @param withId The card id.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets whether this input is a required field. Default value is false.
     * 
     * @return True if required input.
     */
    public Boolean getRequired() {
        return isRequired;
    }

    /**
     * Sets whether this input is a required field.
     * 
     * @param withRequired True if required input.
     */
    public void setRequired(Boolean withRequired) {
        isRequired = withRequired;
    }

    /**
     * Gets input title that will be shown as the placeholder.
     * 
     * @return The input title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets input title that will be shown as the placeholder.
     * 
     * @param withTitle The input title.
     */
    public void setTitle(String withTitle) {
        title = withTitle;
    }

    /**
     * Gets default value for this input field.
     * 
     * @return The default input value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets default value for this input field.
     * 
     * @param withValue The default input value.
     */
    public void setValue(String withValue) {
        value = withValue;
    }
}
