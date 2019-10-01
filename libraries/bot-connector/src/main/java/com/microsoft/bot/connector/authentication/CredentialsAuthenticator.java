package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class CredentialsAuthenticator implements Authenticator {
    private ConfidentialClientApplication app;
    ClientCredentialParameters parameters;

    public CredentialsAuthenticator(
        MicrosoftAppCredentials credentials, OAuthConfiguration configuration) throws MalformedURLException {

        app = ConfidentialClientApplication.builder(
            credentials.getAppId(), ClientCredentialFactory.create(credentials.getAppPassword()))
            .authority(configuration.getAuthority())
            .build();

        parameters = ClientCredentialParameters.builder(
            Collections.singleton(configuration.getScope()))
            .build();
    }

    @Override
    public CompletableFuture<IAuthenticationResult> acquireToken() {
        return app.acquireToken(parameters)
            .exceptionally(exception -> {
                // wrapping whatever msal throws into our own exception
                throw new AuthenticationException(exception);
            });
    }
}
