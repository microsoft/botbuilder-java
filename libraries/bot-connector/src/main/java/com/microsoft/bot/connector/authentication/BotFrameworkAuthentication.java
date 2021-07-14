// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.skills.BotFrameworkClient;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.CallerIdConstants;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Represents a Cloud Environment used to authenticate Bot Framework Protocol
 * network calls within this environment.
 */
public abstract class BotFrameworkAuthentication {

    /**
     * Validate Bot Framework Protocol requests.
     *
     * @param activity   The inbound Activity.
     * @param authHeader The http auth header.
     * @return Asynchronous Task with {@link AuthenticateRequestResult}.
     */
    public abstract CompletableFuture<AuthenticateRequestResult> authenticateRequest(Activity activity,
            String authHeader);

    /**
     * Validate Bot Framework Protocol requests.
     *
     * @param authHeader      The http auth header.
     * @param channelIdHeader The channel Id HTTP header.
     * @return Asynchronous Task with {@link AuthenticateRequestResult}.
     */
    public abstract CompletableFuture<AuthenticateRequestResult> authenticateStreamingRequest(String authHeader,
            String channelIdHeader);

    /**
     * Creates a {@link ConnectorFactory} that can be used to create
     * {@link com.microsoft.bot.connector.ConnectorClient} that use credentials from this particular cloud
     * environment.
     *
     * @param claimsIdentity The inbound @{link Activity}'s {@link ClaimsIdentity}.
     * @return A {@link ConnectorFactory}.
     */
    public abstract ConnectorFactory createConnectorFactory(ClaimsIdentity claimsIdentity);

    /**
     * Creates the appropriate {@link UserTokenClient} instance.
     *
     * @param claimsIdentity The inbound @{link Activity}'s {@link ClaimsIdentity}.
     * @return Asynchronous Task with {@link UserTokenClient} instance.
     */
    public abstract CompletableFuture<UserTokenClient> createUserTokenClient(ClaimsIdentity claimsIdentity);

    /**
     * Creates a {@link BotFrameworkClient} used for calling Skills.
     *
     * @return A {@link BotFrameworkClient} instance to call Skills.
     */
    public BotFrameworkClient createBotFrameworkClient() {
        throw new NotImplementedException("createBotFrameworkClient is not implemented");
    }

    /**
     * Gets the originating audience from Bot OAuth scope.
     *
     * @return The originating audience.
     */
    public String getOriginatingAudience() {
        throw new NotImplementedException("getOriginatingAudience is not implemented");
    }

    /**
     * Authenticate Bot Framework Protocol requests to Skills.
     * @param authHeader The http auth header received in the skill request.
     * @return A {@link ClaimsIdentity}.
     */
    public CompletableFuture<ClaimsIdentity> authenticateChannelRequest(String authHeader) {
        throw new NotImplementedException("authenticateChannelRequest is not implemented");
    }

    /**
     * Generates the appropriate callerId to write onto the activity, this might be
     * null.
     *
     * @param credentialFactory A {@link ServiceClientCredentialsFactory} to use.
     * @param claimsIdentity    The inbound claims.
     * @param callerId          The default callerId to use if this is not a skill.
     * @return The callerId, this might be null.
     */
    protected CompletableFuture<String> generateCallerId(ServiceClientCredentialsFactory credentialFactory,
            ClaimsIdentity claimsIdentity, String callerId) {
        // Is the bot accepting all incoming messages?
        return credentialFactory.isAuthenticationDisabled().thenApply(isDisabled -> {
            if (isDisabled) {
                // Return null so that the callerId is cleared.
                return null;
            }

            // Is the activity from another bot?
            return SkillValidation.isSkillClaim(claimsIdentity.claims()) ? String.format("%s%s",
                    CallerIdConstants.BOT_TO_BOT_PREFIX, JwtTokenValidation.getAppIdFromClaims(claimsIdentity.claims()))
                    : callerId;
        });
    }
}
