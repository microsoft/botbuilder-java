// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;

/**
 * Abstraction to build connector clients.
 */
    public interface ConnectorClientBuilder {

        /**
         * Creates the connector client asynchronous.
         * @param serviceUrl The service URL.
         * @param claimsIdentity The claims claimsIdentity.
         * @param audience The target audience for the connector.
         * @return ConnectorClient instance.
         */
        CompletableFuture<ConnectorClient> createConnectorClient(String serviceUrl,
                                                                 ClaimsIdentity claimsIdentity,
                                                                 String audience);
}
