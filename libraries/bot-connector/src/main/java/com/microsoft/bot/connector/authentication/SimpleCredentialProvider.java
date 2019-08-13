package com.microsoft.bot.connector.authentication;

import org.apache.commons.lang3.StringUtils;
import java.util.concurrent.CompletableFuture;

public class SimpleCredentialProvider implements CredentialProvider {
    private String appId;
    private String password;


    public SimpleCredentialProvider() {
    }
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
        return CompletableFuture.completedFuture(appId == this.appId);
    }

    @Override
    public CompletableFuture<String> getAppPasswordAsync(String appId) {
        return CompletableFuture.completedFuture((appId == this.appId) ? this.password : null);
    }

    @Override
    public CompletableFuture<Boolean> isAuthenticationDisabledAsync() {
        return CompletableFuture.completedFuture(StringUtils.isEmpty(this.appId));
    }


}
