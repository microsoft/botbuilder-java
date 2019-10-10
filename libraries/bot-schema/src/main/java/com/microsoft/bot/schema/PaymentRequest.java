// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A request to make a payment.
 */
public class PaymentRequest {
    /**
     * ID of this payment request.
     */
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    /**
     * Allowed payment methods for this request.
     */
    @JsonProperty(value = "methodData")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PaymentMethodData> methodData;

    /**
     * Details for this request.
     */
    @JsonProperty(value = "details")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private PaymentDetails details;

    /**
     * Provides information about the options desired for the payment request.
     */
    @JsonProperty(value = "options")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private PaymentOptions options;

    /**
     * Expiration for this request, in ISO 8601 duration format (e.g., 'P1D').
     */
    @JsonProperty(value = "expires")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String expires;

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
     * Get the methodData value.
     *
     * @return the methodData value
     */
    public List<PaymentMethodData> getMethodData() {
        return this.methodData;
    }

    /**
     * Set the methodData value.
     *
     * @param withMethodData the methodData value to set
     */
    public void setMethodData(List<PaymentMethodData> withMethodData) {
        this.methodData = withMethodData;
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
     * Get the options value.
     *
     * @return the options value
     */
    public PaymentOptions getOptions() {
        return this.options;
    }

    /**
     * Set the options value.
     *
     * @param withOptions the options value to set
     */
    public void setOptions(PaymentOptions withOptions) {
        this.options = withOptions;
    }

    /**
     * Get the expires value.
     *
     * @return the expires value
     */
    public String getExpires() {
        return this.expires;
    }

    /**
     * Set the expires value.
     *
     * @param withExpires the expires value to set
     */
    public void setExpires(String withExpires) {
        this.expires = withExpires;
    }
}
