/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A PaymentResponse is returned when a user has selected a payment method and
 * approved a payment request.
 */
public class PaymentResponse {
    /**
     * The payment method identifier for the payment method that the user
     * selected to fulfil the transaction.
     */
    @JsonProperty(value = "methodName")
    private String methodName;

    /**
     * A JSON-serializable object that provides a payment method specific
     * message used by the merchant to process the transaction and determine
     * successful fund transfer.
     */
    @JsonProperty(value = "details")
    private Object details;

    /**
     * If the requestShipping flag was set to true in the PaymentOptions passed
     * to the PaymentRequest constructor, then shippingAddress will be the full
     * and final shipping address chosen by the user.
     */
    @JsonProperty(value = "shippingAddress")
    private PaymentAddress shippingAddress;

    /**
     * If the requestShipping flag was set to true in the PaymentOptions passed
     * to the PaymentRequest constructor, then shippingOption will be the id
     * attribute of the selected shipping option.
     */
    @JsonProperty(value = "shippingOption")
    private String shippingOption;

    /**
     * If the requestPayerEmail flag was set to true in the PaymentOptions
     * passed to the PaymentRequest constructor, then payerEmail will be the
     * email address chosen by the user.
     */
    @JsonProperty(value = "payerEmail")
    private String payerEmail;

    /**
     * If the requestPayerPhone flag was set to true in the PaymentOptions
     * passed to the PaymentRequest constructor, then payerPhone will be the
     * phone number chosen by the user.
     */
    @JsonProperty(value = "payerPhone")
    private String payerPhone;

    /**
     * Get the methodName value.
     *
     * @return the methodName value
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * Set the methodName value.
     *
     * @param withMethodName the methodName value to set
     */
    public void setMethodName(String withMethodName) {
        this.methodName = withMethodName;
    }

    /**
     * Get the details value.
     *
     * @return the details value
     */
    public Object getDetails() {
        return this.details;
    }

    /**
     * Set the details value.
     *
     * @param withDetails the details value to set
     */
    public void setDetails(Object withDetails) {
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

    /**
     * Get the payerEmail value.
     *
     * @return the payerEmail value
     */
    public String getPayerEmail() {
        return this.payerEmail;
    }

    /**
     * Set the payerEmail value.
     *
     * @param withPayerEmail the payerEmail value to set
     */
    public void setPayerEmail(String withPayerEmail) {
        this.payerEmail = withPayerEmail;
    }

    /**
     * Get the payerPhone value.
     *
     * @return the payerPhone value
     */
    public String getPayerPhone() {
        return this.payerPhone;
    }

    /**
     * Set the payerPhone value.
     *
     * @param withPayerPhone the payerPhone value to set
     * @return the PaymentResponse object itself.
     */
    public void setPayerPhone(String withPayerPhone) {
        this.payerPhone = withPayerPhone;
    }
}
