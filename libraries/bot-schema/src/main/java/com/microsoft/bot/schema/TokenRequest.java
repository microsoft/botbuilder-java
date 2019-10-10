// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * A request to receive a user token.
 */
public class TokenRequest {
    /**
     * The provider to request a user token from.
     */
    @JsonProperty(value = "provider")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String provider;

    /**
     * A collection of settings for the specific provider for this request.
     */
    @JsonProperty(value = "settings")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> settings;

    /**
     * Get the provider value.
     *
     * @return the provider value
     */
    public String getProvider() {
        return this.provider;
    }

    /**
     * Set the provider value.
     *
     * @param withProvider the provider value to set
     */
    public void setProvider(String withProvider) {
        this.provider = withProvider;
    }

    /**
     * Get the settings value.
     *
     * @return the settings value
     */
    public Map<String, Object> getSettings() {
        return this.settings;
    }

    /**
     * Set the settings value.
     *
     * @param withSettings the settings value to set
     */
    public void setSettings(Map<String, Object> withSettings) {
        this.settings = withSettings;
    }
}
