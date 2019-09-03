/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Indicates what the payment request is for and the value asked for.
 */
public class PaymentItem {
    /**
     * Human-readable description of the item.
     */
    @JsonProperty(value = "label")
    private String label;

    /**
     * Monetary amount for the item.
     */
    @JsonProperty(value = "amount")
    private PaymentCurrencyAmount amount;

    /**
     * When set to true this flag means that the amount field is not final.
     */
    @JsonProperty(value = "pending")
    private boolean pending;

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
     * Get the pending value.
     *
     * @return the pending value
     */
    public boolean getPending() {
        return this.pending;
    }

    /**
     * Set the pending value.
     *
     * @param withPending the pending value to set
     */
    public void setPending(boolean withPending) {
        this.pending = withPending;
    }
}
