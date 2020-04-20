// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The status of a particular token.
 */
public class TokenStatus {
    /**
     * The channelId of the token status pertains to.
     */
    @JsonProperty(value = "channelId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String channelId;

    /**
     * The name of the connection the token status pertains to.
     */
    @JsonProperty(value = "connectionName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionName;

    /**
     * True if a token is stored for this ConnectionName.
     */
    @JsonProperty(value = "hasToken")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean hasToken;

    /**
     * The display name of the service provider for which this Token belongs to.
     */
    @JsonProperty(value = "serviceProviderDisplayName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String serviceProviderDisplayName;

    /**
     * Gets the channelId.
     * 
     * @return The channelId.
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Sets the channelId.
     * 
     * @param withChannelId The channelId.
     */
    public void setChannelId(String withChannelId) {
        channelId = withChannelId;
    }

    /**
     * Gets the connectionName.
     * 
     * @return The connection name.
     */
    public String getConnectionName() {
        return connectionName;
    }

    /**
     * Sets the connectionName.
     * 
     * @param withConnectionName The connection name.
     */
    public void setConnectionName(String withConnectionName) {
        connectionName = withConnectionName;
    }

    /**
     * Gets the hasToken value.
     * 
     * @return The hasToken value.
     */
    public boolean hasToken() {
        return hasToken;
    }

    /**
     * Sets the hasToken value.
     * 
     * @param withHasToken The hasToken value.
     */
    public void setHasToken(boolean withHasToken) {
        hasToken = withHasToken;
    }

    /**
     * Gets the serviceProviderDisplayName field.
     * 
     * @return The service provider display name.
     */
    public String getServiceProviderDisplayName() {
        return serviceProviderDisplayName;
    }

    /**
     * Sets the serviceProviderDisplayName field.
     * 
     * @param withServiceProviderDisplayName The service provider display name.
     */
    public void setServiceProviderDisplayName(String withServiceProviderDisplayName) {
        serviceProviderDisplayName = withServiceProviderDisplayName;
    }
}
