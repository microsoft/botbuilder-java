// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.concurrent.CompletableFuture;

/**
 * CredentialProvider interface. This interface allows Bots to provide their own
 * implementation of what is, and what is not, a valid appId and password. This
 * is useful in the case of multi-tenant bots, where the bot may need to call
 * out to a service to determine if a particular appid/password pair is valid.
 *
 * For Single Tenant bots (the vast majority) the simple static providers are
 * sufficient.
 */
public interface CredentialProvider {
    /**
     * Validates an app ID.
     *
     * @param appId The app ID to validate.
     * @return A task that represents the work queued to execute. If the task is
     *         successful, the result is true if appId is valid for the controller;
     *         otherwise, false.
     */
    CompletableFuture<Boolean> isValidAppId(String appId);

    /**
     * Gets the app password for a given bot app ID.
     *
     * @param appId The ID of the app to get the password for.
     * @return A task that represents the work queued to execute. If the task is
     *         successful and the app ID is valid, the result contains the password;
     *         otherwise, null. This method is async to enable custom
     *         implementations that may need to call out to serviced to validate the
     *         appId / password pair.
     */
    CompletableFuture<String> getAppPassword(String appId);

    /**
     * Checks whether bot authentication is disabled.
     *
     * @return A task that represents the work queued to execute. If the task is
     *         successful and bot authentication is disabled, the result is true;
     *         otherwise, false. This method is async to enable custom
     *         implementations that may need to call out to serviced to validate the
     *         appId / password pair.
     */
    CompletableFuture<Boolean> isAuthenticationDisabled();
}
