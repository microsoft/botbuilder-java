// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.adal4j.ClientCredential;
import org.slf4j.LoggerFactory;

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

    @Override
    protected AdalAuthenticator buildAuthenticator() {
        try {
            return new AdalAuthenticator(
                new ClientCredential(getAppId(), getAppPassword()),
                new OAuthConfiguration(oAuthEndpoint(), oAuthScope()));
        } catch (MalformedURLException e) {
            // intentional no-op.  This class validates the URL on construction or setChannelAuthTenant.
            // That is... this will never happen.
            LoggerFactory.getLogger(MicrosoftAppCredentials.class).error("getAuthenticator", e);
        }

        return null;
    }
}
