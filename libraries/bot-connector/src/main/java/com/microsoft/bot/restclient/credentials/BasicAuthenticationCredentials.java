// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient.credentials;

import okhttp3.OkHttpClient;

/**
 * Basic Auth credentials for use with a REST Service Client.
 */
public class BasicAuthenticationCredentials implements ServiceClientCredentials {

    /**
     * Basic auth UserName.
     */
    private final String userName;

    /**
     * Basic auth password.
     */
    private final String password;

    /**
     * Instantiates a new basic authentication credential.
     *
     * @param withUserName basic auth user name
     * @param withPassword basic auth password
     */
    public BasicAuthenticationCredentials(String withUserName, String withPassword) {
        this.userName = withUserName;
        this.password = withPassword;
    }

    /**
     * @return the user name of the credential
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the password of the credential
     */
    protected String getPassword() {
        return password;
    }

    /**
     * Apply the credentials to the HTTP client builder.
     *
     * @param clientBuilder the builder for building up an {@link OkHttpClient}
     */
    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new BasicAuthenticationCredentialsInterceptor(this));
    }
}
