// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.net.MalformedURLException;

/**
 * MicrosoftAppCredentials auth implementation and cache.
 */
public class MicrosoftAppCredentials extends AppCredentials {
    /**
     * The configuration property for the Microsoft app Password.
     */
    public static final String MICROSOFTAPPID = "MicrosoftAppId";

    /**
     * The configuration property for the Microsoft app ID.
     */
    public static final String MICROSOFTAPPPASSWORD = "MicrosoftAppPassword";

    private String appPassword;

    public static MicrosoftAppCredentials empty() {
        return new MicrosoftAppCredentials(null, null);
    }

    public MicrosoftAppCredentials(String withAppId, String withAppPassword) {
        this(withAppId, withAppPassword, null);
    }

    public MicrosoftAppCredentials(String withAppId, String withAppPassword, String withChannelAuthTenant) {
        super(withChannelAuthTenant);
        setAppId(withAppId);
        setAppPassword(withAppPassword);
    }

    public String getAppPassword() {
        return appPassword;
    }

    public void setAppPassword(String withAppPassword) {
        appPassword = withAppPassword;
    }

    protected Authenticator buildAuthenticator() throws MalformedURLException {
        return new CredentialsAuthenticator(this, new OAuthConfiguration(oAuthEndpoint(), oAuthScope()));
    }
}
