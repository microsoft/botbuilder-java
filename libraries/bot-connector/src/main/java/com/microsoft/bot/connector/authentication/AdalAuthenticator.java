// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.bot.connector.ExecutorFactory;

import java.net.MalformedURLException;
import java.util.concurrent.Future;

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

    public Future<AuthenticationResult> acquireToken() {
        return context.acquireToken(oAuthConfiguration.scope(), clientCredential, null);
    }
}
