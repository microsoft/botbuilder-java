/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload delivered when completing a payment request.
 */
public class PaymentRequestComplete {
    /**
     * Payment request ID.
     */
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    /**
     * Initial payment request.
     */
    @JsonProperty(value = "paymentRequest")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private PaymentRequest paymentRequest;

    /**
     * Corresponding payment response.
     */
    @JsonProperty(value = "paymentResponse")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private PaymentResponse paymentResponse;

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
     * Get the paymentRequest value.
     *
     * @return the paymentRequest value
     */
    public PaymentRequest getPaymentRequest() {
        return this.paymentRequest;
    }

    /**
     * Set the paymentRequest value.
     *
     * @param withPaymentRequest the paymentRequest value to set
     */
    public void setPaymentRequest(PaymentRequest withPaymentRequest) {
        this.paymentRequest = withPaymentRequest;
    }

    /**
     * Get the paymentResponse value.
     *
     * @return the paymentResponse value
     */
    public PaymentResponse getPaymentResponse() {
        return this.paymentResponse;
    }

    /**
     * Set the paymentResponse value.
     *
     * @param withPaymentResponse the paymentResponse value to set
     */
    public void setPaymentResponse(PaymentResponse withPaymentResponse) {
        this.paymentResponse = withPaymentResponse;
    }
}
