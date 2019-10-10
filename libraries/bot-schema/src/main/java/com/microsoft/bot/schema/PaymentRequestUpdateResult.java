// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A result object from a Payment Request Update invoke operation.
 */
public class PaymentRequestUpdateResult {
    /**
     * Update payment details.
     */
    @JsonProperty(value = "details")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private PaymentDetails details;

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
}
