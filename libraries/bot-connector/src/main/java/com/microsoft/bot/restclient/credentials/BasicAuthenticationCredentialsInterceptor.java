// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient.credentials;

import com.google.common.io.BaseEncoding;
import java.nio.charset.StandardCharsets;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Basic Auth credentials interceptor for placing a basic auth credential into request headers.
 */
final class BasicAuthenticationCredentialsInterceptor implements Interceptor {
    /**
     * The credentials instance to apply to the HTTP client pipeline.
     */
    private final BasicAuthenticationCredentials credentials;

    /**
     * Initialize a BasicAuthenticationCredentialsFilter class with a
     * BasicAuthenticationCredentials credential.
     *
     * @param withCredentials a BasicAuthenticationCredentials instance
     */
    BasicAuthenticationCredentialsInterceptor(BasicAuthenticationCredentials withCredentials) {
        this.credentials = withCredentials;
    }

    /**
     * Handle OKHttp intercept.
     * @param chain okhttp3 Chain
     * @return okhttp3 Response
     * @throws IOException IOException during http IO.
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        String auth = credentials.getUserName() + ":" + credentials.getPassword();
        auth = BaseEncoding.base64().encode(auth.getBytes(StandardCharsets.UTF_8));
        Request newRequest = chain.request().newBuilder()
                .header("Authorization", "Basic " + auth)
                .build();
        return chain.proceed(newRequest);
    }
}
