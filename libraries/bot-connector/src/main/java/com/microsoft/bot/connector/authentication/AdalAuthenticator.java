// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.bot.connector.ExecutorFactory;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.concurrent.CompletableFuture;

public class AdalAuthenticator {
    private AuthenticationContext context;
    private OAuthConfiguration oAuthConfiguration;
    private ClientCredential clientCredential;

    public AdalAuthenticator(ClientCredential clientCredential, OAuthConfiguration configurationOAuth)
        throws MalformedURLException {
        this.oAuthConfiguration = configurationOAuth;
        this.clientCredential = clientCredential;
        this.context = new AuthenticationContext(configurationOAuth.authority(), false,
            ExecutorFactory.getExecutor());
    }

    public CompletableFuture<AuthenticationResult> acquireToken() {
        CompletableFuture<AuthenticationResult> tokenFuture = new CompletableFuture<>();

        context.acquireToken(oAuthConfiguration.scope(), clientCredential, new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult result) {
                ExecutorFactory.getExecutor().execute(() -> tokenFuture.complete(result));
            }

            @Override
            public void onFailure(Throwable throwable) {
                LoggerFactory.getLogger(AdalAuthenticator.class).warn("acquireToken", throwable);
                ExecutorFactory.getExecutor().execute(() -> tokenFuture.completeExceptionally(throwable));
            }
        });

        return tokenFuture;
    }
}
