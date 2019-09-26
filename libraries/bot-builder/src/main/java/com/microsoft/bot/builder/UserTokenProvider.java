// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.bot.schema.TokenStatus;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface UserTokenProvider {
    /**
     * Attempts to retrieve the token for a user that's in a login flow.
     *
     * @param turnContext    Context for the current turn of conversation with the user.
     * @param connectionName Name of the auth connection to use.
     * @param magicCode      (Optional) Optional user entered code to validate.
     * @return Token Response.
     */
    CompletableFuture<TokenResponse> getUserToken(TurnContext turnContext,
                                                  String connectionName,
                                                  String magicCode);

    /**
     * Get the raw signin link to be sent to the user for signin for a connection name.
     *
     * @param turnContext    Context for the current turn of conversation with the user.
     * @param connectionName Name of the auth connection to use.
     * @return A task that represents the work queued to execute. If the task completes successfully,
     * the result contains the raw signin link.
     */
    CompletableFuture<String> getOauthSignInLink(TurnContext turnContext, String connectionName);

    /**
     * Get the raw signin link to be sent to the user for signin for a connection name.
     *
     * @param turnContext    Context for the current turn of conversation with the user.
     * @param connectionName Name of the auth connection to use.
     * @param userId         The user id that will be associated with the token.
     * @param finalRedirect  The final URL that the OAuth flow will redirect to.
     * @return A task that represents the work queued to execute. If the task completes successfully,
     * the result contains the raw signin link.
     */
    CompletableFuture<String> getOauthSignInLink(TurnContext turnContext,
                                                 String connectionName,
                                                 String userId,
                                                 String finalRedirect);

    /**
     * Signs the user out with the token server.
     *
     * @param turnContext Context for the current turn of conversation with the user.
     * @return A task that represents the work queued to execute.
     */
    default CompletableFuture<Void> signOutUser(TurnContext turnContext) {
        return signOutUser(turnContext, null, null);
    }

    /**
     * Signs the user out with the token server.
     *
     * @param turnContext    Context for the current turn of conversation with the user.
     * @param connectionName Name of the auth connection to use.
     * @param userId         User id of user to sign out.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> signOutUser(TurnContext turnContext, String connectionName, String userId);

    /**
     * Retrieves the token status for each configured connection for the given user.
     *
     * @param turnContext Context for the current turn of conversation with the user.
     * @param userId      The user Id for which token status is retrieved.
     * @return Array of TokenStatus.
     */
    default CompletableFuture<TokenStatus[]> getTokenStatus(TurnContext turnContext, String userId) {
        return getTokenStatus(turnContext, userId, null);
    }

    /**
     * Retrieves the token status for each configured connection for the given user.
     *
     * @param turnContext   Context for the current turn of conversation with the user.
     * @param userId        The user Id for which token status is retrieved.
     * @param includeFilter Comma separated list of connection's to include. Blank will return token status
     *                      for all configured connections.
     * @return Array of TokenStatus.
     */
    CompletableFuture<TokenStatus[]> getTokenStatus(TurnContext turnContext, String userId, String includeFilter);

    /**
     * Retrieves Azure Active Directory tokens for particular resources on a configured connection.
     *
     * @param turnContext    Context for the current turn of conversation with the user.
     * @param connectionName The name of the Azure Active Directory connection configured with this bot.
     * @param resourceUrls   The list of resource URLs to retrieve tokens for.
     * @return Dictionary of resourceUrl to the corresponding TokenResponse.
     */
    default CompletableFuture<Map<String, TokenResponse>> getAadTokens(TurnContext turnContext,
                                                                       String connectionName,
                                                                       String[] resourceUrls) {
        return getAadTokens(turnContext, connectionName, resourceUrls, null);
    }

    /**
     * Retrieves Azure Active Directory tokens for particular resources on a configured connection.
     *
     * @param turnContext    Context for the current turn of conversation with the user.
     * @param connectionName The name of the Azure Active Directory connection configured with this bot.
     * @param resourceUrls   The list of resource URLs to retrieve tokens for.
     * @param userId         The user Id for which tokens are retrieved. If passing in null the userId is taken from
     *                       the Activity in the ITurnContext.
     * @return Dictionary of resourceUrl to the corresponding TokenResponse.
     */
    CompletableFuture<Map<String, TokenResponse>> getAadTokens(TurnContext turnContext,
                                                               String connectionName,
                                                               String[] resourceUrls,
                                                               String userId);
}
