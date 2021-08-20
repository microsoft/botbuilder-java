// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import org.apache.commons.lang3.NotImplementedException;
import java.util.concurrent.CompletableFuture;

/**
 * This is just an internal class to allow pre-existing implementation of the request validation to be used with
 * a IServiceClientCredentialFactory.
 */
public class DelegatingCredentialProvider implements CredentialProvider {

    private ServiceClientCredentialsFactory credentialsFactory;

    /**
     * Initialize a {@link DelegatingCredentialProvider} class.
     * @param withCredentialsFactory A {@link ServiceClientCredentialsFactory} class.
     */
    public DelegatingCredentialProvider(ServiceClientCredentialsFactory withCredentialsFactory) {
        if (withCredentialsFactory == null) {
            throw new IllegalArgumentException("withCredentialsFactory cannot be null");
        }

        credentialsFactory = withCredentialsFactory;
    }

    /**
     * Gets the appPassword.
     * @param appId The ID of the app to get the password for.
     * @return The appPassword.
     */
    public CompletableFuture<String> getAppPassword(String appId) {
        throw new NotImplementedException("getAppPassword is not implemented");
    }

    /**
     * Validates if the authentication is disabled.
     * @return Boolean value depending if the authentication is disabled or not.
     */
    public CompletableFuture<Boolean> isAuthenticationDisabled() {
        return credentialsFactory.isAuthenticationDisabled();
    }

    /**
     * Validates if the received appId is valid.
     * @param appId The app ID to validate.
     * @return Boolean value depending if the received appId is valid or not.
     */
    public CompletableFuture<Boolean> isValidAppId(String appId) {
        return credentialsFactory.isValidAppId(appId);
    }
}

