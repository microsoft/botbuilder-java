// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.ConnectorClient;

public abstract class ConnectorFactory {
    /**
     * A factory method used to create {@link ConnectorClient} instances.
     *
     * @param serviceUrl The url for the client.
     * @param audience   The audience for the credentials the client will use.
     * @return A {@link ConnectorClient} for sending activities to the audience at
     *         the serviceUrl.
     */
    public abstract CompletableFuture<ConnectorClient> create(String serviceUrl, String audience);
}
