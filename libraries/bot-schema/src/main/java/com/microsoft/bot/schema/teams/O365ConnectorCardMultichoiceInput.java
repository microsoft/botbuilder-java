// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class O365ConnectorCardMultichoiceInput {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "MultichoiceInput";

    @JsonProperty(value = "choices")
    private List<O365ConnectorCardMultichoiceInputChoice> choices;

    @JsonProperty(value = "style")
    private String style;

    @JsonProperty(value = "isMultiSelect")
    private Boolean isMultiSelect;

    public List<O365ConnectorCardMultichoiceInputChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<O365ConnectorCardMultichoiceInputChoice> withChoices) {
        choices = withChoices;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String withStyle) {
        style = withStyle;
    }

    public Boolean getMultiSelect() {
        return isMultiSelect;
    }

    public void setMultiSelect(Boolean withMultiSelect) {
        isMultiSelect = withMultiSelect;
    }
}
