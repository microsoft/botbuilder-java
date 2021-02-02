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

    /**
     * Returns an empty set of credentials.
     * 
     * @return A empty set of MicrosoftAppCredentials.
     */
    public static MicrosoftAppCredentials empty() {
        return new MicrosoftAppCredentials(null, null);
    }

    /**
     * Initializes a new instance of the MicrosoftAppCredentials class.
     * 
     * @param withAppId       The Microsoft app ID.
     * @param withAppPassword The Microsoft app password.
     */
    public MicrosoftAppCredentials(String withAppId, String withAppPassword) {
        this(withAppId, withAppPassword, null);
    }

    /**
     * Initializes a new instance of the MicrosoftAppCredentials class.
     * 
     * @param withAppId             The Microsoft app ID.
     * @param withAppPassword       The Microsoft app password.
     * @param withChannelAuthTenant Optional. The oauth token tenant.
     */
    public MicrosoftAppCredentials(
        String withAppId,
        String withAppPassword,
        String withChannelAuthTenant
    ) {
        super(withChannelAuthTenant);
        setAppId(withAppId);
        setAppPassword(withAppPassword);
    }

    /**
     * Initializes a new instance of the MicrosoftAppCredentials class.
     *
     * @param withAppId             The Microsoft app ID.
     * @param withAppPassword       The Microsoft app password.
     * @param withChannelAuthTenant Optional. The oauth token tenant.
     * @param withOAuthScope        The scope for the token.
     */
    public MicrosoftAppCredentials(
        String withAppId,
        String withAppPassword,
        String withChannelAuthTenant,
        String withOAuthScope
    ) {
        super(withChannelAuthTenant, withOAuthScope);
        setAppId(withAppId);
        setAppPassword(withAppPassword);
    }

    /**
     * Gets the app password for this credential.
     * 
     * @return The app password.
     */
    public String getAppPassword() {
        return appPassword;
    }

    /**
     * Sets the app password for this credential.
     * 
     * @param withAppPassword The app password.
     */
    public void setAppPassword(String withAppPassword) {
        appPassword = withAppPassword;
    }

    /**
     * Returns an credentials Authenticator.
     * 
     * @return A CredentialsAuthenticator.
     * @throws MalformedURLException Invalid endpoint url.
     */
    protected Authenticator buildAuthenticator() throws MalformedURLException {
        return new CredentialsAuthenticator(
            getAppId(),
            getAppPassword(),
            new OAuthConfiguration(oAuthEndpoint(), oAuthScope())
        );
    }
}
