// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result from a completed payment request.
 */
public class PaymentRequestCompleteResult {
    /**
     * Result of the payment request completion.
     */
    @JsonProperty(value = "result")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String result;

    /**
     * Get the result value.
     *
     * @return the result value
     */
    public String getResult() {
        return this.result;
    }

    /**
     * Set the result value.
     *
     * @param withResult the result value to set
     */
    public void setResult(String withResult) {
        this.result = withResult;
    }
}
