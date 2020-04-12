// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * O365 connector card multiple choice input.
 */
public class O365ConnectorCardMultichoiceInput {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "MultichoiceInput";

    @JsonProperty(value = "choices")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<O365ConnectorCardMultichoiceInputChoice> choices;

    @JsonProperty(value = "style")
    private String style;

    @JsonProperty(value = "isMultiSelect")
    private Boolean isMultiSelect;

    /**
     * Gets list of choices whose each item can be in any subtype of
     * O365ConnectorCardMultichoiceInputChoice.
     * 
     * @return List of choices.
     */
    public List<O365ConnectorCardMultichoiceInputChoice> getChoices() {
        return choices;
    }

    /**
     * Sets list of choices whose each item can be in any subtype of
     * O365ConnectorCardMultichoiceInputChoice.
     * 
     * @param withChoices List of choices.
     */
    public void setChoices(List<O365ConnectorCardMultichoiceInputChoice> withChoices) {
        choices = withChoices;
    }

    /**
     * Gets choice item rendering style. Default value is 'compact'. Possible values
     * include: 'compact', 'expanded'
     * 
     * @return The choice style.
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets choice item rendering style. Default value is 'compact'. Possible values
     * include: 'compact', 'expanded'
     * 
     * @param withStyle The choice style.
     */
    public void setStyle(String withStyle) {
        style = withStyle;
    }

    /**
     * Defines if this input field allows multiple selections. Default value is
     * false.
     * 
     * @return True if the choice allows multiple selections.
     */
    public Boolean getMultiSelect() {
        return isMultiSelect;
    }

    /**
     * Sets if this input field allows multiple selections.
     * 
     * @param withMultiSelect True if the choice allows multiple selections.
     */
    public void setMultiSelect(Boolean withMultiSelect) {
        isMultiSelect = withMultiSelect;
    }
}
