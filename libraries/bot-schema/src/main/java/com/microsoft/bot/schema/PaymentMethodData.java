/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Indicates a set of supported payment methods and any associated payment
 * method specific data for those methods.
 */
public class PaymentMethodData {
    /**
     * Required sequence of strings containing payment method identifiers for
     * payment methods that the merchant web site accepts.
     */
    @JsonProperty(value = "supportedMethods")
    private List<String> supportedMethods;

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
