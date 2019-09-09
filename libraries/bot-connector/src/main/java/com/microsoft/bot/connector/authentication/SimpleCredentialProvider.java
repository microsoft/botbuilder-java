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
     * @param appId    The app ID.
     * @param password The app password.
     */
    public SimpleCredentialProvider(String appId, String password) {
        this.appId = appId;
        this.password = password;
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Validates an app ID.
     *
     * @param appId The app ID to validate.
     * @return If the task is successful, the result is true if appId is valid for the controller; otherwise, false.
     */
    @Override
    public CompletableFuture<Boolean> isValidAppId(String appId) {
        return CompletableFuture.completedFuture(StringUtils.equals(appId, this.appId));
    }

    /**
     * Gets the app password for a given bot app ID.
     *
     * @param appId The ID of the app to get the password for.
     * @return If the task is successful and the app ID is valid, the result
     * contains the password; otherwise, null.
     */
    @Override
    public CompletableFuture<String> getAppPassword(String appId) {
        return CompletableFuture.completedFuture(StringUtils.equals(appId, this.appId) ? this.password : null);
    }

    /**
     * Checks whether bot authentication is disabled.
     *
     * @return A task that represents the work queued to execute If the task is successful and bot authentication
     * is disabled, the result is true; otherwise, false.
     */
    @Override
    public CompletableFuture<Boolean> isAuthenticationDisabled() {
        return CompletableFuture.completedFuture(StringUtils.isEmpty(this.appId));
    }
}
