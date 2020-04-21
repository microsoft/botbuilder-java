// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Token credentials filter for placing a token credential into request headers.
 */
public class AppCredentialsInterceptor implements Interceptor {
    /**
     * The credentials instance to apply to the HTTP client pipeline.
     */
    private AppCredentials credentials;

    /**
     * Initialize a TokenCredentialsFilter class with a TokenCredentials credential.
     *
     * @param withCredentials a TokenCredentials instance
     */
    public AppCredentialsInterceptor(AppCredentials withCredentials) {
        credentials = withCredentials;
    }

    /**
     * Apply the credentials to the HTTP request.
     *
     * @param chain The Okhttp3 Interceptor Chain.
     * @return The modified Response.
     * @throws IOException via Chain or failure to get token.
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        if (credentials.shouldSetToken(chain.request().url().url().toString())) {
            String token;
            try {
                token = credentials.getToken().get();
            } catch (Throwable t) {
                throw new IOException(t);
            }

            Request newRequest =
                chain.request().newBuilder().header("Authorization", "Bearer " + token).build();
            return chain.proceed(newRequest);
        }
        return chain.proceed(chain.request());
    }
}
