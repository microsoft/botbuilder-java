// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

/**
 * Values and Constants used for Authentication and Authorization by the Bot
 * Framework Protocol to US Government DataCenters.
 */
public final class GovernmentAuthenticationConstants {
    private GovernmentAuthenticationConstants() {

    }

    public static final String CHANNELSERVICE = "https://botframework.azure.us";

    /**
     * TO GOVERNMENT CHANNEL FROM BOT: Login URL.
     */
    public static final String TO_CHANNEL_FROM_BOT_LOGIN_URL =
        "https://login.microsoftonline.us/cab8a31a-1906-4287-a0d8-4eef66b95f6e";

    /**
     * TO GOVERNMENT CHANNEL FROM BOT: OAuth scope to request.
     */
    public static final String TO_CHANNEL_FROM_BOT_OAUTH_SCOPE =
        "https://api.botframework.us/.default";

    /**
     * TO BOT FROM GOVERNMENT CHANNEL: Token issuer.
     */
    public static final String TO_BOT_FROM_CHANNEL_TOKEN_ISSUER = "https://api.botframework.us";

    /**
     * OAuth Url used to get a token from OAuthApiClient.
     */
    public static final String OAUTH_URL_GOV = "https://api.botframework.azure.us";

    /**
     * TO BOT FROM GOVERNMANT CHANNEL: OpenID metadata document for tokens coming
     * from MSA.
     */
    public static final String TO_BOT_FROM_CHANNEL_OPENID_METADATA_URL =
        "https://login.botframework.azure.us/v1/.well-known/openidconfiguration";

    /**
     * TO BOT FROM GOVERNMENT EMULATOR: OpenID metadata document for tokens coming
     * from MSA.
     */
    public static final String TO_BOT_FROM_EMULATOR_OPENID_METADATA_URL =
        "https://login.microsoftonline.us/cab8a31a-1906-4287-a0d8-4eef66b95f6e/v2.0/.well-known/openid-configuration";
}
