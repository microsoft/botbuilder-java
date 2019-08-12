// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.concurrent.CompletableFuture;

public class CredentialProviderImpl extends BotCredentials implements CredentialProvider {

    public CredentialProviderImpl(String appId, String appPassword) {
        this.appId = appId;
        this.appPassword = appPassword;
    }

    public CredentialProviderImpl(BotCredentials credentials) {
        this(credentials.appId, credentials.appPassword);
    }

    @Override
    public CredentialProviderImpl withAppId(String appId) {
        return (CredentialProviderImpl) super.withAppId(appId);
    }

    @Override
    public CredentialProviderImpl withAppPassword(String appPassword) {
        return (CredentialProviderImpl) super.withAppPassword(appPassword);
    }

    @Override
    public CompletableFuture<Boolean> isValidAppIdAsync(String appId) {
        return CompletableFuture.completedFuture(this.appId.equals(appId));
    }

    @Override
    public CompletableFuture<String> getAppPasswordAsync(String appId) {
        return CompletableFuture.completedFuture(this.appId.equals(appId) ? this.appPassword : null);
    }

    @Override
    public CompletableFuture<Boolean> isAuthenticationDisabledAsync() {
        return CompletableFuture.completedFuture(this.appId == null || this.appId.isEmpty());
    }
}
