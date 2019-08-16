// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.ArrayList;
import java.util.List;

public final class AuthenticationConstants {
    /**
     * TO CHANNEL FROM BOT: Login URL.
     */
    @Deprecated
    public static final String ToChannelFromBotLoginUrl = "https://login.microsoftonline.com/botframework.com";

    /**
     * TO CHANNEL FROM BOT: Login URL template string. Bot developer may specify
     * which tenant to obtain an access token from. By default, the channels only
     * accept tokens from "botframework.com". For more details see https://aka.ms/bots/tenant-restriction.
     */
    public static final String ToChannelFromBotLoginUrlTemplate = "https://login.microsoftonline.com/%s";

    /**
     * TO CHANNEL FROM BOT: OAuth scope to request.
     */
    public static final String ToChannelFromBotOAuthScope = "https://api.botframework.com";

    /**
     * TO BOT FROM CHANNEL: Token issuer.
     */
    public static final String ToBotFromChannelTokenIssuer  = "https://api.botframework.com";

    /**
     * TO BOT FROM CHANNEL: OpenID metadata document for tokens coming from MSA.
     */
    public static final String ToBotFromChannelOpenIdMetadataUrl = "https://login.botframework.com/v1/.well-known/openidconfiguration";

    /**
     * TO BOT FROM EMULATOR: OpenID metadata document for tokens coming from MSA.
     */
    public static final String ToBotFromEmulatorOpenIdMetadataUrl = "https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration";

    /**
     * TO BOT FROM ENTERPRISE CHANNEL: OpenID metadata document for tokens coming from MSA.
     */
    public static final String ToBotFromEnterpriseChannelOpenIdMetadataUrlFormat = "https://%s.enterprisechannel.botframework.com/v1/.well-known/openidconfiguration";

    /**
     * Allowed token signing algorithms. Tokens come from channels to the bot. The code
     * that uses this also supports tokens coming from the emulator.
     */
    public static final List<String> AllowedSigningAlgorithms = new ArrayList<>();

    /**
     * Application Setting Key for the OAuthUrl value.
     */
    public static final String OAuthUrlKey = "OAuthApiEndpoint";

    /**
     * OAuth Url used to get a token from OAuthApiClient.
     */
    public static final String OAuthUrl = "https://api.botframework.com";

    /**
     * Application Settings Key for whether to emulate OAuthCards when using the emulator.
     */
    public static final String EmulateOAuthCardsKey = "EmulateOAuthCards";

    /**
     * Application Setting Key for the OpenIdMetadataUrl value.
     */
    public static final String BotOpenIdMetadataKey = "BotOpenIdMetadata";

    /**
     * The default tenant to acquire bot to channel token from.
     */
    public static final String DefaultChannelAuthTenant = "botframework.com";

    /**
     * "azp" Claim.
     * Authorized party - the party to which the ID Token was issued.
     * This claim follows the general format set forth in the OpenID Spec.
     * http://openid.net/specs/openid-connect-core-1_0.html#IDToken.
     */
    public static final String AuthorizedParty = "azp";

    /**
     * Audience Claim. From RFC 7519.
     * https://tools.ietf.org/html/rfc7519#section-4.1.3
     * The "aud" (audience) claim identifies the recipients that the JWT is
     * intended for. Each principal intended to process the JWT MUST
     * identify itself with a value in the audience claim. If the principal
     * processing the claim does not identify itself with a value in the
     * "aud" claim when this claim is present, then the JWT MUST be
     * rejected. In the general case, the "aud" value is an array of case-
     * sensitive strings, each containing a StringOrURI value. In the
     * special case when the JWT has one audience, the "aud" value MAY be a
     * single case-sensitive string containing a StringOrURI value. The
     * interpretation of audience values is generally application specific.
     * Use of this claim is OPTIONAL.
     */
    public static final String AudienceClaim = "aud";

    /**
     * From RFC 7515
     * https://tools.ietf.org/html/rfc7515#section-4.1.4
     * The "kid" (key ID) Header Parameter is a hint indicating which key
     * was used to secure the JWS. This parameter allows originators to
     * explicitly signal a change of key to recipients. The structure of
     * the "kid" value is unspecified. Its value MUST be a case-sensitive
     * string. Use of this Header Parameter is OPTIONAL.
     * When used with a JWK, the "kid" value is used to match a JWK "kid"
     * parameter value.
     */
    public static final String KeyIdHeader = "kid";

    /**
     * Service URL claim name. As used in Microsoft Bot Framework v3.1 auth.
     */
    public static final String ServiceUrlClaim = "serviceurl";

    /**
     * Token version claim name. As used in Microsoft AAD tokens.
     */
    public static final String VersionClaim = "ver";

    /**
     * App ID claim name. As used in Microsoft AAD 1.0 tokens.
     */
    public static final String AppIdClaim = "appid";

    static {
        AllowedSigningAlgorithms.add("RS256");
        AllowedSigningAlgorithms.add("RS384");
        AllowedSigningAlgorithms.add("RS512");
    }
}
