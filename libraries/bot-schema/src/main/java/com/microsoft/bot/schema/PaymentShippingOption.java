// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a shipping option.
 */
public class PaymentShippingOption {
    /**
     * String identifier used to reference this PaymentShippingOption.
     */
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    /**
     * Human-readable description of the item.
     */
    @JsonProperty(value = "label")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String label;

    /**
     * Contains the monetary amount for the item.
     */
    @JsonProperty(value = "amount")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private PaymentCurrencyAmount amount;

    /**
     * Indicates whether this is the default selected PaymentShippingOption.
     */
    @JsonProperty(value = "selected")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean selected;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param withId the id value to set
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Get the label value.
     *
     * @return the label value
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Set the label value.
     *
     * @param withLabel the label value to set
     */
    public void setLabel(String withLabel) {
        this.label = withLabel;
    }

    /**
     * Get the amount value.
     *
     * @return the amount value
     */
    public PaymentCurrencyAmount getAmount() {
        return this.amount;
    }

    /**
     * Set the amount value.
     *
     * @param withAmount the amount value to set
     */
    public void setAmount(PaymentCurrencyAmount withAmount) {
        this.amount = withAmount;
    }

    /**
     * Get the selected value.
     *
     * @return the selected value
     */
    public boolean getSelected() {
        return this.selected;
    }

    /**
     * Set the selected value.
     *
     * @param withSelected the selected value to set
     */
    public void setSelected(boolean withSelected) {
        this.selected = withSelected;
    }
}
