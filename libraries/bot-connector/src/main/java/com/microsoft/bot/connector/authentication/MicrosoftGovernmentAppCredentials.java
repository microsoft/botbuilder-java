// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

/**
 * MicrosoftGovernmentAppCredentials auth implementation.
 */
public class MicrosoftGovernmentAppCredentials extends MicrosoftAppCredentials {
    /**
     * Initializes a new instance of the MicrosoftGovernmentAppCredentials class.
     *
     * @param appId    The Microsoft app ID.
     * @param password The Microsoft app password.
     */
    public MicrosoftGovernmentAppCredentials(String appId, String password) {
        super(appId, password);
    }

    /**
     * An empty set of credentials.
     * @return An empty Gov credentials.
     */
    public static MicrosoftGovernmentAppCredentials empty() {
        return new MicrosoftGovernmentAppCredentials(null, null);
    }

    /**
     * Gets the Gov OAuth endpoint to use.
     *
     * @return The OAuth endpoint to use.
     */
    @Override
    public String oAuthEndpoint() {
        return GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_LOGIN_URL;
    }

    /**
     * Gets the Gov OAuth scope to use.
     *
     * @return The OAuth scope to use.
     */
    @Override
    public String oAuthScope() {
        return GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE;
    }
}
