/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Provides details that modify the PaymentDetails based on payment method
 * identifier.
 */
public class PaymentDetailsModifier {
    /**
     * Contains a sequence of payment method identifiers.
     */
    @JsonProperty(value = "supportedMethods")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> supportedMethods;

    /**
     * This value overrides the total field in the PaymentDetails dictionary
     * for the payment method identifiers in the supportedMethods field.
     */
    @JsonProperty(value = "total")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private PaymentItem total;

    /**
     * Provides additional display items that are appended to the displayItems
     * field in the PaymentDetails dictionary for the payment method
     * identifiers in the supportedMethods field.
     */
    @JsonProperty(value = "additionalDisplayItems")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PaymentItem> additionalDisplayItems;

    /**
     * A JSON-serializable object that provides optional information that might
     * be needed by the supported payment methods.
     */
    @JsonProperty(value = "data")
    private Object data;

    /**
     * Get the supportedMethods value.
     *
     * @return the supportedMethods value
     */
    public List<String> getSupportedMethods() {
        return this.supportedMethods;
    }

    /**
     * Set the supportedMethods value.
     *
     * @param withSupportedMethods the supportedMethods value to set
     */
    public void setSupportedMethods(List<String> withSupportedMethods) {
        this.supportedMethods = withSupportedMethods;
    }

    /**
     * Get the total value.
     *
     * @return the total value
     */
    public PaymentItem getTotal() {
        return this.total;
    }

    /**
     * Set the total value.
     *
     * @param withTotal the total value to set
     */
    public void setTotal(PaymentItem withTotal) {
        this.total = withTotal;
    }

    /**
     * Get the additionalDisplayItems value.
     *
     * @return the additionalDisplayItems value
     */
    public List<PaymentItem> getAdditionalDisplayItems() {
        return this.additionalDisplayItems;
    }

    /**
     * Set the additionalDisplayItems value.
     *
     * @param withAdditionalDisplayItems the additionalDisplayItems value to set
     */
    public void setAdditionalDisplayItems(List<PaymentItem> withAdditionalDisplayItems) {
        this.additionalDisplayItems = withAdditionalDisplayItems;
    }

    /**
     * Get the data value.
     *
     * @return the data value
     */
    public Object getData() {
        return this.data;
    }

    /**
     * Set the data value.
     *
     * @param withData the data value to set
     */
    public void setData(Object withData) {
        this.data = withData;
    }
}
