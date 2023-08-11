// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;

/**
 * The ServiceClientCredentialsFactory abstract class that allows Bots to
 * provide their own ServiceClientCredentials for bot to bot channel or skill
 * bot to parent bot calls.
 */
public abstract class ServiceClientCredentialsFactory {

    /**
     * Validates an app ID.
     *
     * @param appId The app ID to validate.
     * @return The result is true if `appId` is valid for the controller; otherwise,
     *         false.
     */
    public abstract CompletableFuture<Boolean> isValidAppId(String appId);

    /**
     * Checks whether bot authentication is disabled.
     *
     * @return If bot authentication is disabled, the result is true; otherwise,
     *         false.
     */
    public abstract CompletableFuture<Boolean> isAuthenticationDisabled();

    /**
     * A factory method for creating ServiceClientCredentials.
     *
     * @param appId             The appId.
     * @param audience          The audience.
     * @param loginEndpoint     The login url.
     * @param validateAuthority he validate authority value to use.
     * @return A {@link ServiceClientCredentials}.
     */
    public abstract CompletableFuture<ServiceClientCredentials> createCredentials(String appId, String audience,
            String loginEndpoint, Boolean validateAuthority);
}
