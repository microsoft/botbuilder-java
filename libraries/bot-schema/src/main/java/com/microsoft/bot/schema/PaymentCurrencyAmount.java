// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Supplies monetary amounts.
 */
public class PaymentCurrencyAmount {
    /**
     * A currency identifier.
     */
    @JsonProperty(value = "currency")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String currency;

    /**
     * Decimal monetary value.
     */
    @JsonProperty(value = "value")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String value;

    /**
     * Currency system.
     */
    @JsonProperty(value = "currencySystem")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String currencySystem;

    /**
     * Get the currency value.
     *
     * @return the currency value
     */
    public String getCurrency() {
        return this.currency;
    }

    /**
     * Set the currency value.
     *
     * @param withCurrency the currency value to set
     */
    public void setCurrency(String withCurrency) {
        this.currency = withCurrency;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param withValue the value value to set
     */
    public void setValue(String withValue) {
        this.value = withValue;
    }

    /**
     * Get the currencySystem value.
     *
     * @return the currencySystem value
     */
    public String getCurrencySystem() {
        return this.currencySystem;
    }

    /**
     * Set the currencySystem value.
     *
     * @param withCurrencySystem the currencySystem value to set
     */
    public void setCurrencySystem(String withCurrencySystem) {
        this.currencySystem = withCurrencySystem;
    }
}
