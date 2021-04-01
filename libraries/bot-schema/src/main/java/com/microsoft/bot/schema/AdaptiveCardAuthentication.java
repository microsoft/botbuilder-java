// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the structure that arrives in the Activity.getValue().Authentication
 * for Invoke activity with Name of 'adaptiveCard/action'.
 */
public class AdaptiveCardAuthentication {

    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    @JsonProperty(value = "connectionName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionName;

    @JsonProperty(value = "token")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String token;

    /**
     * Gets the Id of the adaptive card invoke authentication.
     * @return the Id value as a String.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the Id of the adaptive card invoke authentication.
     * @param withId The Id value.
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Gets the connection name of the adaptive card authentication.
     * @return the ConnectionName value as a String.
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    /**
     * Sets the connection name of the adaptive card authentication.
     * @param withConnectionName The ConnectionName value.
     */
    public void setConnectionName(String withConnectionName) {
        this.connectionName = withConnectionName;
    }

    /**
     * Gets the token of the adaptive card authentication.
     * @return the Token value as a String.
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Sets the token of the adaptive card authentication.
     * @param withToken The Token value.
     */
    public void setToken(String withToken) {
        this.token = withToken;
    }

}
