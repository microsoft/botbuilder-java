// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.schema.SignInResource;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.bot.schema.TokenStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * OAuth provider.
 */
public interface UserTokenProvider {
    /**
     * Attempts to retrieve the token for a user that's in a login flow.
     *
     * @param turnContext    Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @param magicCode      (Optional) Optional user entered code to validate.
     * @return Token Response.
     */
    CompletableFuture<TokenResponse> getUserToken(
        TurnContext turnContext,
        String connectionName,
        String magicCode
    );

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name.
     *
     * @param turnContext    Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @return A task that represents the work queued to execute. If the task
     *         completes successfully, the result contains the raw signin link.
     */
    CompletableFuture<String> getOAuthSignInLink(TurnContext turnContext, String connectionName);

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name.
     *
     * @param turnContext    Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @param userId         The user id that will be associated with the token.
     * @param finalRedirect  The final URL that the OAuth flow will redirect to.
     * @return A task that represents the work queued to execute. If the task
     *         completes successfully, the result contains the raw signin link.
     */
    CompletableFuture<String> getOAuthSignInLink(
        TurnContext turnContext,
        String connectionName,
        String userId,
        String finalRedirect
    );

    /**
     * Signs the user out with the token server.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @return A task that represents the work queued to execute.
     */
    default CompletableFuture<Void> signOutUser(TurnContext turnContext) {
        return signOutUser(turnContext, null, null);
    }

    /**
     * Signs the user out with the token server.
     *
     * @param turnContext    Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @param userId         User id of user to sign out.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> signOutUser(
        TurnContext turnContext,
        String connectionName,
        String userId
    );

    /**
     * Retrieves the token status for each configured connection for the given user.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @param userId      The user Id for which token status is retrieved.
     * @return Array of TokenStatus.
     */
    default CompletableFuture<List<TokenStatus>> getTokenStatus(
        TurnContext turnContext,
        String userId
    ) {
        return getTokenStatus(turnContext, userId, null);
    }

    /**
     * Retrieves the token status for each configured connection for the given user.
     *
     * @param turnContext   Context for the current turn of conversation with the
     *                      user.
     * @param userId        The user Id for which token status is retrieved.
     * @param includeFilter Comma separated list of connection's to include. Blank
     *                      will return token status for all configured connections.
     * @return Array of TokenStatus.
     */
    CompletableFuture<List<TokenStatus>> getTokenStatus(
        TurnContext turnContext,
        String userId,
        String includeFilter
    );

    /**
     * Retrieves Azure Active Directory tokens for particular resources on a
     * configured connection.
     *
     * @param turnContext    Context for the current turn of conversation with the
     *                       user.
     * @param connectionName The name of the Azure Active Directory connection
     *                       configured with this bot.
     * @param resourceUrls   The list of resource URLs to retrieve tokens for.
     * @return Dictionary of resourceUrl to the corresponding TokenResponse.
     */
    default CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        TurnContext turnContext,
        String connectionName,
        String[] resourceUrls
    ) {
        return getAadTokens(turnContext, connectionName, resourceUrls, null);
    }

    /**
     * Retrieves Azure Active Directory tokens for particular resources on a
     * configured connection.
     *
     * @param turnContext    Context for the current turn of conversation with the
     *                       user.
     * @param connectionName The name of the Azure Active Directory connection
     *                       configured with this bot.
     * @param resourceUrls   The list of resource URLs to retrieve tokens for.
     * @param userId         The user Id for which tokens are retrieved. If passing
     *                       in null the userId is taken from the Activity in the
     *                       ITurnContext.
     * @return Dictionary of resourceUrl to the corresponding TokenResponse.
     */
    CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        TurnContext turnContext,
        String connectionName,
        String[] resourceUrls,
        String userId
    );


    /**
     * Attempts to retrieve the token for a user that's in a login flow, using
     * customized AppCredentials.
     *
     * @param turnContext          Context for the current turn of
     *                             conversation with the user.
     * @param oAuthAppCredentials  AppCredentials for OAuth.
     * @param connectionName       Name of the auth connection to use.
     * @param magicCode            (Optional) Optional user entered code
     *                             to validate.
     *
     * @return   Token Response.
     */
    CompletableFuture<TokenResponse> getUserToken(
        TurnContext turnContext,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String magicCode);

    /**
     * Get the raw signin link to be sent to the user for signin for a
     * connection name, using customized AppCredentials.
     *
     * @param turnContext          Context for the current turn of
     *                             conversation with the user.
     * @param oAuthAppCredentials  AppCredentials for OAuth.
     * @param connectionName       Name of the auth connection to use.
     *
     * @return   A CompletableFuture that represents the work queued to execute.
     *
     * If the CompletableFuture completes successfully, the result contains the raw signin
     * link.
     */
    CompletableFuture<String> getOAuthSignInLink(
        TurnContext turnContext,
        AppCredentials oAuthAppCredentials,
        String connectionName);

    /**
     * Get the raw signin link to be sent to the user for signin for a
     * connection name, using customized AppCredentials.
     *
     * @param turnContext          Context for the current turn of
     *                             conversation with the user.
     * @param oAuthAppCredentials  AppCredentials for OAuth.
     * @param connectionName       Name of the auth connection to use.
     * @param userId               The user id that will be associated
     *                             with the token.
     * @param finalRedirect        The final URL that the OAuth flow
     *                             will redirect to.
     *
     * @return   A CompletableFuture that represents the work queued to execute.
     *
     * If the CompletableFuture completes successfully, the result contains the raw signin
     * link.
     */
    CompletableFuture<String> getOAuthSignInLink(
        TurnContext turnContext,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String userId,
        String finalRedirect);

    /**
     * Signs the user out with the token server, using customized
     * AppCredentials.
     *
     * @param turnContext          Context for the current turn of
     *                             conversation with the user.
     * @param oAuthAppCredentials  AppCredentials for OAuth.
     * @param connectionName       Name of the auth connection to use.
     * @param userId               User id of user to sign out.
     *
     * @return   A CompletableFuture that represents the work queued to execute.
     */
    CompletableFuture<Void> signOutUser(
        TurnContext turnContext,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String userId);

    /**
     * Retrieves the token status for each configured connection for the given
     * user, using customized AppCredentials.
     *
     * @param context              Context for the current turn of
     *                             conversation with the user.
     * @param oAuthAppCredentials  AppCredentials for OAuth.
     * @param userId               The user Id for which token status is
     *                             retrieved.
     * @param includeFilter        Optional comma separated list of
     *                             connection's to include. Blank will return token status for all
     *                             configured connections.
     *
     * @return   Array of TokenStatus.
     */
    CompletableFuture<List<TokenStatus>> getTokenStatus(
        TurnContext context,
        AppCredentials oAuthAppCredentials,
        String userId,
        String includeFilter);

    /**
     * Retrieves Azure Active Directory tokens for particular resources on a
     * configured connection, using customized AppCredentials.
     *
     * @param context              Context for the current turn of
     *                             conversation with the user.
     * @param oAuthAppCredentials  AppCredentials for OAuth.
     * @param connectionName       The name of the Azure Active
     *                             Directory connection configured with this bot.
     * @param resourceUrls         The list of resource URLs to retrieve
     *                             tokens for.
     * @param userId               The user Id for which tokens are
     *                             retrieved. If passing in null the userId is taken from the Activity in
     *                             the TurnContext.
     *
     * @return   Dictionary of resourceUrl to the corresponding
     *           TokenResponse.
     */
    CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        TurnContext context,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String[] resourceUrls,
        String userId);

    /**
     * Get the raw signin link to be sent to the user for signin for a
     * connection name.
     *
     * @param turnContext     Context for the current turn of
     *                        conversation with the user.
     * @param connectionName  Name of the auth connection to use.
     *
     * @return   A CompletableFuture that represents the work queued to execute.
     *
     * If the CompletableFuture completes successfully, the result contains the raw signin
     * link.
     */
    CompletableFuture<SignInResource> getSignInResource(
        TurnContext turnContext,
        String connectionName);

    /**
     * Get the raw signin link to be sent to the user for signin for a
     * connection name.
     *
     * @param turnContext     Context for the current turn of
     *                        conversation with the user.
     * @param connectionName  Name of the auth connection to use.
     * @param userId          The user id that will be associated with
     *                        the token.
     * @param finalRedirect   The final URL that the OAuth flow will
     *                        redirect to.
     *
     * @return   A CompletableFuture that represents the work queued to execute.
     *
     * If the CompletableFuture completes successfully, the result contains the raw signin
     * link.
     */
    CompletableFuture<SignInResource> getSignInResource(
        TurnContext turnContext,
        String connectionName,
        String userId,
        String finalRedirect);

    /**
     * Get the raw signin link to be sent to the user for signin for a
     * connection name.
     *
     * @param turnContext          Context for the current turn of
     *                             conversation with the user.
     * @param oAuthAppCredentials  Credentials for OAuth.
     * @param connectionName       Name of the auth connection to use.
     * @param userId               The user id that will be associated
     *                             with the token.
     * @param finalRedirect        The final URL that the OAuth flow
     *                             will redirect to.
     *
     * @return   A CompletableFuture that represents the work queued to execute.
     *
     * If the CompletableFuture completes successfully, the result contains the raw signin
     * link.
     */
    CompletableFuture<SignInResource> getSignInResource(
        TurnContext turnContext,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String userId,
        String finalRedirect);

    /**
     * Performs a token exchange operation such as for single sign-on.
     *
     * @param turnContext      Context for the current turn of
     *                         conversation with the user.
     * @param connectionName   Name of the auth connection to use.
     * @param userId           The user id associated with the token..
     * @param exchangeRequest  The exchange request details, either a
     *                         token to exchange or a uri to exchange.
     *
     * @return   If the CompletableFuture completes, the exchanged token is returned.
     */
    CompletableFuture<TokenResponse> exchangeToken(
        TurnContext turnContext,
        String connectionName,
        String userId,
        TokenExchangeRequest exchangeRequest);

    /**
     * Performs a token exchange operation such as for single sign-on.
     *
     * @param turnContext          Context for the current turn of
     *                             conversation with the user.
     * @param oAuthAppCredentials  AppCredentials for OAuth.
     * @param connectionName       Name of the auth connection to use.
     * @param userId               The user id associated with the
     *                             token..
     * @param exchangeRequest      The exchange request details, either
     *                             a token to exchange or a uri to exchange.
     *
     * @return   If the CompletableFuture completes, the exchanged token is returned.
     */
    CompletableFuture<TokenResponse> exchangeToken(
        TurnContext turnContext,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String userId,
        TokenExchangeRequest exchangeRequest);

}
