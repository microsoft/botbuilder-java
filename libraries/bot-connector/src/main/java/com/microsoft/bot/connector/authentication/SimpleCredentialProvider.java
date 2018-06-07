package com.microsoft.bot.connector.authentication;

import org.apache.commons.lang3.StringUtils;
import java.util.concurrent.CompletableFuture;

public class SimpleCredentialProvider implements CredentialProvider {
    private String _appId;
    private String _password;


    public SimpleCredentialProvider() {
    }
    public SimpleCredentialProvider(String appId, String password) {
        this._appId = appId;
        this._password = password;
    }

    public String getAppId() {
        return _appId;
    }
    public void setAppId(String appId) {
        this._appId = appId;
    }

    public String getPassword() {
        return _password;
    }
    public void setPassword(String password) {
        this._password = password;
    }

    @Override
    public CompletableFuture<Boolean> isValidAppIdAsync(String appId) {
        return CompletableFuture.completedFuture(appId == this._appId);
    }

    @Override
    public CompletableFuture<String> getAppPasswordAsync(String appId) {
        return CompletableFuture.completedFuture((appId == this._appId) ? this._password: null);
    }

    @Override
    public CompletableFuture<Boolean> isAuthenticationDisabledAsync() {
        return CompletableFuture.completedFuture(StringUtils.isEmpty(_appId));
    }


}
