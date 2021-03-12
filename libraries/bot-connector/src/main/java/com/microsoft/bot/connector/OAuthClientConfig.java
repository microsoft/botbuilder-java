// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.AuthenticationConstants;

/**
 * OAuthClient config.
 */
public final class OAuthClientConfig {
    private OAuthClientConfig() {

    }

    /**
     * The default endpoint that is used for API requests.
     */
    public static final String OAUTHENDPOINT = AuthenticationConstants.OAUTH_URL;

    /**
     * Value indicating whether when using the Emulator, whether to emulate the
     * OAuthCard behavior or use connected flows.
     */
    @SuppressWarnings("checkstyle:VisibilityModifier")
    public static boolean emulateOAuthCards = false;
}
