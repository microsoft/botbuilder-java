// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.authentication;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.SignInResource;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenExchangeState;
import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.bot.schema.TokenStatus;
import com.nimbusds.oauth2.sdk.util.StringUtils;

/**
 * Client for access user token service.
 */
public abstract class UserTokenClient {

    /**
     * Attempts to retrieve the token for a user that's in a login flow.
     *
     * @param userId         The user id that will be associated with the token.
     * @param connectionName Name of the auth connection to use.
     * @param channelId      The channel Id that will be associated with the token.
     * @param magicCode      (Optional) Optional user entered code to validate.
     * @return A {@link TokenResponse} object.
     */
    public abstract CompletableFuture<TokenResponse> getUserToken(String userId, String connectionName,
            String channelId, String magicCode);

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name.
     *
     * @param connectionName Name of the auth connection to use.
     * @param activity       The {@link Activity} from which to derive the token
     *                       exchange state.
     * @param finalRedirect  The final URL that the OAuth flow will redirect to.
     * @return A {@link SignInResource}
     */
    public abstract CompletableFuture<SignInResource> getSignInResource(String connectionName, Activity activity,
            String finalRedirect);

    /**
     * Signs the user out with the token server.
     *
     * @param userId         The user id that will be associated with the token.
     * @param connectionName Name of the auth connection to use.
     * @param channelId      The channel Id that will be associated with the token.
     * @return A Task representing the result of the asynchronous operation.
     */
    public abstract CompletableFuture<Void> signOutUser(String userId, String connectionName, String channelId);

    /**
     * Retrieves the token status for each configured connection for the given user.
     *
     * @param userId        The user id that will be associated with the token.
     * @param channelId     The channel Id that will be associated with the token.
     * @param includeFilter The includeFilter.
     * @return A list of {@link TokenStatus} objects.
     */
    public abstract CompletableFuture<List<TokenStatus>> getTokenStatus(String userId, String channelId,
            String includeFilter);

    /**
     * Retrieves Azure Active Directory tokens for particular resources on a
     * configured connection.
     *
     * @param userId         The user id that will be associated with the token.
     * @param connectionName Name of the auth connection to use.
     * @param resourceUrls   The list of resource URLs to retrieve tokens for.
     * @param channelId      The channel Id that will be associated with the token.
     * @return A Dictionary of resourceUrls to the corresponding
     *         {@link TokenResponse}.
     */
    public abstract CompletableFuture<Map<String, TokenResponse>> getAadTokens(String userId, String connectionName,
            List<String> resourceUrls, String channelId);

    /**
     * Performs a token exchange operation such as for single sign-on.
     *
     * @param userId          The user id that will be associated with the token.
     * @param connectionName  Name of the auth connection to use.
     * @param channelId       The channel Id that will be associated with the token.
     * @param exchangeRequest The exchange request details, either a token to
     *                        exchange or a uri to exchange.
     * @return A {@link TokenResponse} object.
     */
    public abstract CompletableFuture<TokenResponse> exchangeToken(String userId, String connectionName,
            String channelId, TokenExchangeRequest exchangeRequest);

    /**
     * Helper function to create the base64 encoded token exchange state used in
     * getSignInResource calls.
     *
     * @param appId          The appId to include in the token exchange state.
     * @param connectionName The connectionName to include in the token exchange
     *                       state.
     * @param activity       The {@link Activity} from which to derive the token
     *                       exchange state.
     * @return Base64 encoded token exchange state
     */
    protected static String createTokenExchangeState(String appId, String connectionName, Activity activity) {
        if (StringUtils.isBlank(appId)) {
            throw new IllegalArgumentException("appId");
        }
        if (StringUtils.isBlank(appId)) {
            throw new IllegalArgumentException("connectionName");
        }
        if (activity == null) {
            throw new IllegalArgumentException("activity");
        }

        TokenExchangeState tokenExchangeState = new TokenExchangeState();
        tokenExchangeState.setConnectionName(connectionName);
        tokenExchangeState.setConversation(activity.getConversationReference());
        tokenExchangeState.setRelatesTo(activity.getRelatesTo());
        tokenExchangeState.setMsAppId(appId);
        try {
            String serializedState = Serialization.toString(tokenExchangeState);
            return Base64.getEncoder().encodeToString(serializedState.getBytes(StandardCharsets.UTF_8));
        } catch (Throwable t) {
            throw new CompletionException(t);
        }
    }
}
