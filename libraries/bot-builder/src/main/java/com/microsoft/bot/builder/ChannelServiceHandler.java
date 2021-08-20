package com.microsoft.bot.builder;

import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.AuthenticationException;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.connector.authentication.SkillValidation;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * A class to help with the implementation of the Bot Framework protocol.
 */
public class ChannelServiceHandler extends ChannelServiceHandlerBase {

    private ChannelProvider channelProvider;

    private final AuthenticationConfiguration authConfiguration;
    private final CredentialProvider credentialProvider;

    /**
     * Initializes a new instance of the {@link ChannelServiceHandler} class,
     * using a credential provider.
     *
     * @param credentialProvider  The credential provider.
     * @param authConfiguration   The authentication configuration.
     * @param channelProvider     The channel provider.
     */
    public ChannelServiceHandler(
        CredentialProvider credentialProvider,
        AuthenticationConfiguration authConfiguration,
        ChannelProvider channelProvider) {

        if (credentialProvider == null) {
            throw new IllegalArgumentException("credentialprovider cannot be null");
        }

        if (authConfiguration == null) {
            throw new IllegalArgumentException("authConfiguration cannot be null");
        }

        this.credentialProvider = credentialProvider;
        this.authConfiguration = authConfiguration;
        this.channelProvider = channelProvider;
    }

    /**
     * Helper to authenticate the header.
     *
     * This code is very similar to the code in
     * {@link JwtTokenValidation#authenticateRequest(Activity, String,
     * CredentialProvider, ChannelProvider, AuthenticationConfiguration,
     * HttpClient)} , we should move this code somewhere in that library when
     * we refactor auth, for now we keep it private to avoid adding more public
     * static functions that we will need to deprecate later.
     * @param authHeader The Bearer token included as part of the request.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<ClaimsIdentity> authenticate(String authHeader) {
        if (StringUtils.isEmpty(authHeader)) {
            return credentialProvider.isAuthenticationDisabled().thenCompose(isAuthDisabled -> {
                if (!isAuthDisabled) {
                    return Async.completeExceptionally(
                        // No auth header. Auth is required. Request is not authorized.
                        new AuthenticationException("No auth header, Auth is required. Request is not authorized")
                    );
                }

                // In the scenario where auth is disabled, we still want to have the
                // IsAuthenticated flag set in the ClaimsIdentity.
                // To do this requires adding in an empty claim.
                // Since ChannelServiceHandler calls are always a skill callback call, we set the skill claim too.
                return CompletableFuture.completedFuture(SkillValidation.createAnonymousSkillClaim());
            });
        }

        // Validate the header and extract claims.
        return  JwtTokenValidation.validateAuthHeader(
                    authHeader, credentialProvider, getChannelProvider(), "unknown", null, authConfiguration);
    }
    /**
     * Gets the channel provider that implements {@link ChannelProvider} .
     * @return the ChannelProvider value as a getChannelProvider().
     */
    protected ChannelProvider getChannelProvider() {
        return this.channelProvider;
    }

}

