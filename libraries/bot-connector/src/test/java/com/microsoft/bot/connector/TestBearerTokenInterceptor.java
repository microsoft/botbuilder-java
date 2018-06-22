// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;




/**
 * Adds bearer token
 */
class TestBearerTokenInterceptor implements Interceptor {
    /**
     * The token HTTP client pipeline.
     */
    private String bearerToken;

    /**
     * Initialize a TokenCredentialsFilter class with a
     * TokenCredentials credential.
     *
     * @param bearerToken token
     */
    TestBearerTokenInterceptor(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request newRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer " + this.bearerToken)
                .build();
        return chain.proceed(newRequest);
    }
}

