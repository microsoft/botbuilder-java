// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.customizations;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Token credentials filter for placing a token credential into request headers.
 */
class MicrosoftAppCredentialsInterceptor implements Interceptor {
    /**
     * The credentials instance to apply to the HTTP client pipeline.
     */
    private MicrosoftAppCredentials credentials;

    /**
     * Initialize a TokenCredentialsFilter class with a
     * TokenCredentials credential.
     *
     * @param credentials a TokenCredentials instance
     */
    MicrosoftAppCredentialsInterceptor(MicrosoftAppCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (MicrosoftAppCredentials.isTrustedServiceUrl(chain.request().url().url().toString())) {
            Request newRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer " + credentials.getToken(chain.request()))
                .build();
            return chain.proceed(newRequest);
        }
        return chain.proceed(chain.request());
    }
}
