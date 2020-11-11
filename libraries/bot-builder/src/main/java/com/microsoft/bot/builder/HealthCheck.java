// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.ExecutionException;

import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.schema.HealthCheckResponse;
import com.microsoft.bot.schema.HealthResults;

/**
 * A class to process a HealthCheck request.
 */
public final class HealthCheck {

    private HealthCheck() {
        // not called
    }

    /**
     * @param connector the ConnectorClient instance for this request
     * @return HealthCheckResponse
     */
    public static HealthCheckResponse createHealthCheckResponse(ConnectorClient connector) {
        HealthResults healthResults = new HealthResults();
        healthResults.setSuccess(true);

        if (connector != null) {
            healthResults.setUserAgent(connector.getUserAgent());
            AppCredentials credentials = (AppCredentials) connector.credentials();
            try {
                healthResults.setAuthorization(credentials.getToken().get());
            } catch (InterruptedException | ExecutionException ignored) {
                // An exception here may happen when you have a valid appId but invalid or blank secret.
                // No callbacks will be possible, although the bot maybe healthy in other respects.
            }
        }

        if (healthResults.getAuthorization() != null) {
            healthResults.setMessages(new String[]{"Health check succeeded."});
        } else {
            healthResults.setMessages(new String[]{"Health check succeeded.", "Callbacks are not authorized."});
        }

        HealthCheckResponse response = new HealthCheckResponse();
        response.setHealthResults(healthResults);
        return response;
    }
}
