// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides information about the options desired for the payment request.
 */
public class PaymentOptions {
    /**
     * Indicates whether the user agent should collect and return the payer's
     * name as part of the payment request.
     */
    @JsonProperty(value = "requestPayerName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean requestPayerName;

    /**
     * Indicates whether the user agent should collect and return the payer's
     * email address as part of the payment request.
     */
    @JsonProperty(value = "requestPayerEmail")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean requestPayerEmail;

    /**
     * Indicates whether the user agent should collect and return the payer's
     * phone number as part of the payment request.
     */
    @JsonProperty(value = "requestPayerPhone")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean requestPayerPhone;

    /**
     * Indicates whether the user agent should collect and return a shipping
     * address as part of the payment request.
     */
    @JsonProperty(value = "requestShipping")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean requestShipping;

    /**
     * If requestShipping is set to true, then the shippingType field may be
     * used to influence the way the user agent presents the user interface for
     * gathering the shipping address.
     */
    @JsonProperty(value = "shippingType")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String shippingType;

    /**
     * Get the requestPayerName value.
     *
     * @return the requestPayerName value
     */
    public boolean getRequestPayerName() {
        return this.requestPayerName;
    }

    /**
     * Set the requestPayerName value.
     *
     * @param withRequestPayerName the requestPayerName value to set
     */
    public void setRequestPayerName(boolean withRequestPayerName) {
        this.requestPayerName = withRequestPayerName;
    }

    /**
     * Get the requestPayerEmail value.
     *
     * @return the requestPayerEmail value
     */
    public boolean getRequestPayerEmail() {
        return this.requestPayerEmail;
    }

    /**
     * Set the requestPayerEmail value.
     *
     * @param withRequestPayerEmail the requestPayerEmail value to set
     */
    public void setRequestPayerEmail(boolean withRequestPayerEmail) {
        this.requestPayerEmail = withRequestPayerEmail;
    }

    /**
     * Get the requestPayerPhone value.
     *
     * @return the requestPayerPhone value
     */
    public boolean getRequestPayerPhone() {
        return this.requestPayerPhone;
    }

    /**
     * Set the requestPayerPhone value.
     *
     * @param withRequestPayerPhone the requestPayerPhone value to set
     */
    public void setRequestPayerPhone(boolean withRequestPayerPhone) {
        this.requestPayerPhone = withRequestPayerPhone;
    }

    /**
     * Get the requestShipping value.
     *
     * @return the requestShipping value
     */
    public boolean getRequestShipping() {
        return this.requestShipping;
    }

    /**
     * Set the requestShipping value.
     *
     * @param withRequestShipping the requestShipping value to set
     */
    public void setRequestShipping(boolean withRequestShipping) {
        this.requestShipping = withRequestShipping;
    }

    /**
     * Get the shippingType value.
     *
     * @return the shippingType value
     */
    public String getShippingType() {
        return this.shippingType;
    }

    /**
     * Set the shippingType value.
     *
     * @param withShippingType the shippingType value to set
     */
    public void setShippingType(String withShippingType) {
        this.shippingType = withShippingType;
    }
}
