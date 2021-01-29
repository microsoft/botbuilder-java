// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * An Authenticator using app id and password.
 */
public class CredentialsAuthenticator implements Authenticator {
    private ConfidentialClientApplication app;
    private ClientCredentialParameters parameters;

    /**
     * Constructs an Authenticator using appId and appPassword.
     *
     * @param appId         The app id.
     * @param appPassword   The app password.
     * @param configuration The OAuthConfiguration.
     * @throws MalformedURLException Invalid endpoint.
     */
    CredentialsAuthenticator(String appId, String appPassword, OAuthConfiguration configuration)
        throws MalformedURLException {

        app = ConfidentialClientApplication
            .builder(appId, ClientCredentialFactory.createFromSecret(appPassword))
            .authority(configuration.getAuthority())
            .build();

        parameters = ClientCredentialParameters
            .builder(Collections.singleton(configuration.getScope()))
            .build();
    }

    /**
     * Gets an auth result via MSAL.
     *
     * @return The auth result.
     */
    @Override
    public CompletableFuture<IAuthenticationResult> acquireToken() {
        return app.acquireToken(parameters)
            .exceptionally(
                exception -> {
                    // wrapping whatever msal throws into our own exception
                    throw new AuthenticationException(exception);
                }
            );
    }
}
