// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A response that includes a user token.
 */
public class TokenResponse {

    /**
     * Initializes a new instance of the TokenResponse class.
     */
    public TokenResponse() {

    }

    /**
     * Initializes a new instance of the TokenResponse class.
     * @param channelId The channelId.
     * @param connectionName The connectionName.
     * @param token The token.
     * @param expiration the expiration.
     */
    public TokenResponse(String channelId, String connectionName, String token, String expiration) {
        this.channelId = channelId;
        this.connectionName = connectionName;
        this.token = token;
        this.expiration = expiration;
    }

    /**
     * The channelId of the TokenResponse.
     */
    @JsonProperty(value = "channelId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String channelId;

    /**
     * The connection name.
     */
    @JsonProperty(value = "connectionName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionName;

    /**
     * The user token.
     */
    @JsonProperty(value = "token")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String token;

    /**
     * Expiration for the token, in ISO 8601 format (e.g. "2007-04-05T14:30Z").
     */
    @JsonProperty(value = "expiration")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String expiration;

    /**
     * Gets the channelId value.
     *
     * @return THe channel id.
     */
    public String getChannelId() {
        return this.channelId;
    }

    /**
     * Sets the channelId value.
     *
     * @param withChannelId The channel id to set.
     */
    public void setChannelId(String withChannelId) {
        this.channelId = withChannelId;
    }

    /**
     * Get the connectionName value.
     *
     * @return the connectionName value
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    /**
     * Set the connectionName value.
     *
     * @param withConnectionName the connectionName value to set
     */
    public void setConnectionName(String withConnectionName) {
        this.connectionName = withConnectionName;
    }

    /**
     * Get the token value.
     *
     * @return the token value
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Set the token value.
     *
     * @param withToken the token value to set
     */
    public void setToken(String withToken) {
        this.token = withToken;
    }

    /**
     * Get the expiration value.
     *
     * @return the expiration value
     */
    public String getExpiration() {
        return this.expiration;
    }

    /**
     * Set the expiration value.
     *
     * @param withExpiration the expiration value to set
     */
    public void setExpiration(String withExpiration) {
        this.expiration = withExpiration;
    }
}
