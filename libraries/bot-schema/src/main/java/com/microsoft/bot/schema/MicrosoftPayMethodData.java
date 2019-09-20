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
 * W3C Payment Method Data for Microsoft Pay.
 */
public class MicrosoftPayMethodData {
    /**
     * Microsoft Pay Merchant ID.
     */
    @JsonProperty(value = "merchantId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String merchantId;

    /**
     * Supported payment networks (e.g., "visa" and "mastercard").
     */
    @JsonProperty(value = "supportedNetworks")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> supportedNetworks;

    /**
     * Supported payment types (e.g., "credit").
     */
    @JsonProperty(value = "supportedTypes")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> supportedTypes;

    /**
     * Get the merchantId value.
     *
     * @return the merchantId value
     */
    public String getMerchantId() {
        return this.merchantId;
    }

    /**
     * Set the merchantId value.
     *
     * @param withMerchantId the merchantId value to set
     * @return the MicrosoftPayMethodData object itself.
     */
    public void setMerchantId(String withMerchantId) {
        this.merchantId = withMerchantId;
    }

    /**
     * Get the supportedNetworks value.
     *
     * @return the supportedNetworks value
     */
    public List<String> getSupportedNetworks() {
        return this.supportedNetworks;
    }

    /**
     * Set the supportedNetworks value.
     *
     * @param withSupportedNetworks the supportedNetworks value to set
     */
    public void setSupportedNetworks(List<String> withSupportedNetworks) {
        this.supportedNetworks = withSupportedNetworks;
    }

    /**
     * Get the supportedTypes value.
     *
     * @return the supportedTypes value
     */
    public List<String> getSupportedTypes() {
        return this.supportedTypes;
    }

    /**
     * Set the supportedTypes value.
     *
     * @param withSupportedTypes the supportedTypes value to set
     */
    public void setSupportedTypes(List<String> withSupportedTypes) {
        this.supportedTypes = withSupportedTypes;
    }
}
