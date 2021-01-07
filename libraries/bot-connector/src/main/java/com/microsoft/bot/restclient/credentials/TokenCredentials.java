// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient.credentials;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Token based credentials for use with a REST Service Client.
 */
public class TokenCredentials implements ServiceClientCredentials {
    /** The authentication scheme. */
    private final String scheme;

    /** The secure token. */
    private final String token;

    /**
     * Initializes a new instance of the TokenCredentials.
     *
     * @param withScheme scheme to use. If null, defaults to Bearer
     * @param withToken  valid token
     */
    public TokenCredentials(String withScheme, String withToken) {
        if (withScheme == null) {
            withScheme = "Bearer";
        }
        this.scheme = withScheme;
        this.token = withToken;
    }

    /**
     * Get the secure token. Override this method to provide a mechanism
     * for acquiring tokens.
     *
     * @param request the context of the HTTP request
     * @return the secure token.
     */
    protected String getToken(Request request) {
        return token;
    }

    /**
     * Get the authentication scheme.
     *
     * @return the authentication scheme
     */
    protected String getScheme() {
        return scheme;
    }

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new TokenCredentialsInterceptor(this));
    }
}
