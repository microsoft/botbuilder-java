/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A result object from a Payment Request Update invoke operation.
 */
public class PaymentRequestUpdateResult {
    /**
     * Update payment details.
     */
    @JsonProperty(value = "details")
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
