package com.microsoft.bot.connector.authentication;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * Validates JWT tokens sent to and from a Skill.
 */
@SuppressWarnings("PMD")
public final class SkillValidation {

    private SkillValidation() {

    }

    /// <summary>
    /// TO SKILL FROM BOT and TO BOT FROM SKILL: Token validation parameters when
    /// connecting a bot to a skill.
    /// </summary>
    private static final TokenValidationParameters TOKENVALIDATIONPARAMETERS = new TokenValidationParameters(true,
            Stream.of(
                    // Auth v3.1, 1.0 token
                    "https://sts.windows.net/d6d49420-f39b-4df7-a1dc-d59a935871db/",
                    // Auth v3.1, 2.0 token
                    "https://login.microsoftonline.com/d6d49420-f39b-4df7-a1dc-d59a935871db/v2.0",
                    // Auth v3.2, 1.0 token
                    "https://sts.windows.net/f8cdef31-a31e-4b4a-93e4-5f571e91255a/",
                    // Auth v3.2, 2.0 token
                    "https://login.microsoftonline.com/f8cdef31-a31e-4b4a-93e4-5f571e91255a/v2.0",
                    // Auth for US Gov, 1.0 token
                    "https://sts.windows.net/cab8a31a-1906-4287-a0d8-4eef66b95f6e/",
                    // Auth for US Gov, 2.0 token
                    "https://login.microsoftonline.us/cab8a31a-1906-4287-a0d8-4eef66b95f6e/v2.0",
                    // Auth for US Gov, 1.0 token
                    "https://login.microsoftonline.us/f8cdef31-a31e-4b4a-93e4-5f571e91255a/",
                    // Auth for US Gov, 2.0 token
                    "https://login.microsoftonline.us/f8cdef31-a31e-4b4a-93e4-5f571e91255a/v2.0")
                    .collect(Collectors.toList()),
            false, // Audience validation takes place manually in code.
            true, Duration.ofMinutes(5), true);

    /**
     * Checks if the given list of claims represents a skill. A skill claim should
     * contain: An AuthenticationConstants.VersionClaim" claim. An
     * AuthenticationConstants.AudienceClaim claim. An
     * AuthenticationConstants.AppIdClaim claim (v1) or an a
     * AuthenticationConstants.AuthorizedParty claim (v2). And the appId claim
     * should be different than the audience claim. When a channel (webchat, teams,
     * etc.) invokes a bot, the <see cref="AuthenticationConstants.AudienceClaim"/>
     * is set to <see cref="AuthenticationConstants.ToBotFromChannelTokenIssuer"/>
     * but when a bot calls another bot, the audience claim is set to the appId of
     * the bot being invoked. The protocol supports v1 and v2 tokens: For v1 tokens,
     * the AuthenticationConstants.AppIdClaim is present and set to the app Id of
     * the calling bot. For v2 tokens, the AuthenticationConstants.AuthorizedParty
     * is present and set to the app Id of the calling bot.
     *
     * @param claims A map of claims
     * @return True if the list of claims is a skill claim, false if is not.
     */
    public static Boolean isSkillClaim(Map<String, String> claims) {

        for (Map.Entry<String, String> entry : claims.entrySet()) {
            if (entry.getValue() == AuthenticationConstants.ANONYMOUS_SKILL_APPID
                    && entry.getKey() == AuthenticationConstants.APPID_CLAIM) {
                return true;
            }
        }

        Optional<Map.Entry<String, String>> version = claims.entrySet().stream()
                .filter((x) -> x.getKey() == AuthenticationConstants.VERSION_CLAIM).findFirst();
        if (!version.isPresent()) {
            // Must have a version claim.
            return false;
        }

        Optional<Map.Entry<String, String>> audience = claims.entrySet().stream()
                .filter((x) -> x.getKey() == AuthenticationConstants.AUDIENCE_CLAIM).findFirst();

        if (!audience.isPresent()
                || AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER == audience.get().getValue()) {
            // The audience is https://api.botframework.com and not an appId.
            return false;
        }

        String appId = JwtTokenValidation.getAppIdFromClaims(claims);
        if (StringUtils.isBlank(appId)) {
            return false;
        }

        // Skill claims must contain and app ID and the AppID must be different than the
        // audience.
        return appId != audience.get().getValue();
    }

}
