package com.microsoft.bot.connector.customizations;

import java.util.concurrent.CompletableFuture;

public interface CredentialProvider {
    CompletableFuture<Boolean> isValidAppIdAsync(String appId);
    CompletableFuture<String> getAppPasswordAsync(String appId);
    CompletableFuture<Boolean> isAuthenticationDisabledAsync();
}
