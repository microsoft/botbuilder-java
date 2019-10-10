// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An update to a payment request.
 */
public class PaymentRequestUpdate {
    /**
     * ID for the payment request to update.
     */
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    /**
     * Update payment details.
     */
    @JsonProperty(value = "details")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private PaymentDetails details;

    /**
     * Updated shipping address.
     */
    @JsonProperty(value = "shippingAddress")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private PaymentAddress shippingAddress;

    /**
     * Updated shipping options.
     */
    @JsonProperty(value = "shippingOption")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String shippingOption;

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
     * Get the details value.
     *
     * @return the details value
     */
    public PaymentDetails getDetails() {
        return this.details;
    }

    /**
     * Set the details value.
     *
     * @param withDetails the details value to set
     */
    public void setDetails(PaymentDetails withDetails) {
        this.details = withDetails;
    }

    /**
     * Get the shippingAddress value.
     *
     * @return the shippingAddress value
     */
    public PaymentAddress getShippingAddress() {
        return this.shippingAddress;
    }

    /**
     * Set the shippingAddress value.
     *
     * @param withShippingAddress the shippingAddress value to set
     */
    public void setShippingAddress(PaymentAddress withShippingAddress) {
        this.shippingAddress = withShippingAddress;
    }

    /**
     * Get the shippingOption value.
     *
     * @return the shippingOption value
     */
    public String getShippingOption() {
        return this.shippingOption;
    }

    /**
     * Set the shippingOption value.
     *
     * @param withShippingOption the shippingOption value to set
     */
    public void setShippingOption(String withShippingOption) {
        this.shippingOption = withShippingOption;
    }
}
