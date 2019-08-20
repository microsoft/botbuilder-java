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

    @Override
    public CompletableFuture<Boolean> isValidAppIdAsync(String appId) {
        return CompletableFuture.completedFuture(StringUtils.equals(appId, this.appId));
    }

    @Override
    public CompletableFuture<String> getAppPasswordAsync(String appId) {
        return CompletableFuture.completedFuture(StringUtils.equals(appId, this.appId) ? this.password : null);
    }

    @Override
    public CompletableFuture<Boolean> isAuthenticationDisabledAsync() {
        return CompletableFuture.completedFuture(StringUtils.isEmpty(this.appId));
    }
}
