// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.concurrent.CompletableFuture;

public interface CredentialProvider {
    CompletableFuture<Boolean> isValidAppIdAsync(String appId);
    CompletableFuture<String> getAppPasswordAsync(String appId);
    CompletableFuture<Boolean> isAuthenticationDisabledAsync();
}
