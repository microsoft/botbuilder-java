// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * A simple implementation of the CredentialProvider interface.
 */
public class SimpleCredentialProvider implements CredentialProvider {
    private String appId;
    private String password;

    /**
     * Initializes a new instance with empty credentials.
     */
    public SimpleCredentialProvider() {
    }

    /**
     * Initializes a new instance with the provided credentials.
     *
     * @param withAppId    The app ID.
     * @param withPassword The app password.
     */
    public SimpleCredentialProvider(String withAppId, String withPassword) {
        appId = withAppId;
        password = withPassword;
    }

    /**
     * Gets the app ID for this credential.
     * 
     * @return The app id.
     */
    public String getAppId() {
        return this.appId;
    }

    /**
     * Sets the app ID for this credential.
     * 
     * @param witAppId The app id.
     */
    public void setAppId(String witAppId) {
        appId = witAppId;
    }

    /**
     * Gets the app password for this credential.
     * 
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the app password for this credential.
     * 
     * @param withPassword The password.
     */
    public void setPassword(String withPassword) {
        password = withPassword;
    }

    /**
     * Validates an app ID.
     *
     * @param validateAppId The app ID to validate.
     * @return If the task is successful, the result is true if appId is valid for
     *         the controller; otherwise, false.
     */
    @Override
    public CompletableFuture<Boolean> isValidAppId(String validateAppId) {
        return CompletableFuture.completedFuture(StringUtils.equals(validateAppId, appId));
    }

    /**
     * Gets the app password for a given bot app ID.
     *
     * @param validateAppId The ID of the app to get the password for.
     * @return If the task is successful and the app ID is valid, the result
     *         contains the password; otherwise, null.
     */
    @Override
    public CompletableFuture<String> getAppPassword(String validateAppId) {
        return CompletableFuture
            .completedFuture(StringUtils.equals(validateAppId, appId) ? password : null);
    }

    /**
     * Checks whether bot authentication is disabled.
     *
     * @return A task that represents the work queued to execute If the task is
     *         successful and bot authentication is disabled, the result is true;
     *         otherwise, false.
     */
    @Override
    public CompletableFuture<Boolean> isAuthenticationDisabled() {
        return CompletableFuture.completedFuture(StringUtils.isEmpty(this.appId));
    }
}
