// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

/**
 * The result from a call to authenticate a Bot Framework Protocol request.
 */
public class AuthenticateRequestResult {

    private String audience;
    private ClaimsIdentity claimsIdentity;
    private String callerId;
    private ConnectorFactory connectorFactory;

    /**
     * Gets a value for the Audience.
     * @return A value for the Audience.
     */
    public String getAudience() {
        return audience;
    }

    /**
     * Sets a value for the Audience.
     * @param audience A value for the Audience.
     */
    public void setAudience(String audience) {
        this.audience = audience;
    }

    /**
     * Gets a value for the ClaimsIdentity.
     * @return A value for the ClaimsIdentity.
     */
    public ClaimsIdentity getClaimsIdentity() {
        return claimsIdentity;
    }

    /**
     * Sets a value for the ClaimsIdentity.
     * @param claimsIdentity A value for the ClaimsIdentity.
     */
    public void setClaimsIdentity(ClaimsIdentity claimsIdentity) {
        this.claimsIdentity = claimsIdentity;
    }

    /**
     * Gets a value for the CallerId.
     * @return A value for the CallerId.
     */
    public String getCallerId() {
        return callerId;
    }

    /**
     * Sets a value for the CallerId.
     * @param callerId A value for the CallerId.
     */
    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    /**
     * Gets a value for the ConnectorFactory.
     * @return A value for the ConnectorFactory.
     */
    public ConnectorFactory getConnectorFactory() {
        return connectorFactory;
    }

    /**
     * Sets a value for the ConnectorFactory.
     * @param connectorFactory A value for the ConnectorFactory.
     */
    public void setConnectorFactory(ConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }
}
