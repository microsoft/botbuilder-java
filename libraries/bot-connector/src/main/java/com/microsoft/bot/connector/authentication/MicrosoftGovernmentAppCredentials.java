// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import org.apache.commons.lang3.StringUtils;

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
        super(
            appId,
            password,
            null,
            GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE
        );
    }

    /**
     * Initializes a new instance of the MicrosoftGovernmentAppCredentials class.
     *
     * @param appId      The Microsoft app ID.
     * @param password   The Microsoft app password.
     * @param oAuthScope The scope for the token.
     */
    public MicrosoftGovernmentAppCredentials(String appId, String password, String oAuthScope) {
        super(
            appId,
            password,
            null,
            StringUtils.isEmpty(oAuthScope)
                ? GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE
                : oAuthScope
        );
    }

    /**
     * Initializes a new instance of the MicrosoftGovernmentAppCredentials class.
     *
     * @param withAppId             The Microsoft app ID.
     * @param withAppPassword       The Microsoft app password.
     * @param withChannelAuthTenant Optional. The oauth token tenant.
     * @param withOAuthScope        The scope for the token.
     */
    public MicrosoftGovernmentAppCredentials(
        String withAppId,
        String withAppPassword,
        String withChannelAuthTenant,
        String withOAuthScope
    ) {
        super(withAppId, withAppPassword, withChannelAuthTenant, withOAuthScope);
    }

    /**
     * An empty set of credentials.
     *
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
}
