// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.connector.authentication.Authenticator;

public class MockAppCredentials extends AppCredentials {

    private String token;

    public MockAppCredentials(String token) {
        super(null);
        this.token = token;
    }

    @Override
    public CompletableFuture<String> getToken() {
        CompletableFuture<String> result;

        result = new CompletableFuture<String>();
        result.complete(this.token);
        return result;
    }

        /**
     * Returns an appropriate Authenticator that is provided by a subclass.
     *
     * @return An Authenticator object.
     * @throws MalformedURLException If the endpoint isn't valid.
     */
    protected Authenticator buildAuthenticator(){
        return null;
    };

}
