// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;

public class BotAccessTokenStub implements ServiceClientCredentials {
    private final String token;

    public BotAccessTokenStub(String token) {
        this.token = token;
    }

    /**
     * Apply the credentials to the HTTP client builder.
     *
     * @param clientBuilder the builder for building up an {@link OkHttpClient}
     */
    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new TestBearerTokenInterceptor(this.token));
    }
}
