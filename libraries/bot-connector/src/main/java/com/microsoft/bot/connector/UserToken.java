/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector;

import com.microsoft.bot.schema.AadResourceUrls;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.bot.schema.TokenStatus;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * An instance of this class provides access to all the operations defined in
 * UserTokens.
 */
public interface UserToken {
    /**
     *
     * @param userId         the String value
     * @param connectionName the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TokenResponse object
     */
    CompletableFuture<TokenResponse> getToken(String userId, String connectionName);

    /**
     *
     * @param userId         the String value
     * @param connectionName the String value
     * @param channelId      the String value
     * @param code           the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TokenResponse object
     */
    CompletableFuture<TokenResponse> getToken(
        String userId,
        String connectionName,
        String channelId,
        String code
    );

    /**
     *
     * @param userId            the String value
     * @param connectionName    the String value
     * @param channelId         the String value
     * @param exchangeRequest   a TokenExchangeRequest
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TokenResponse object
     */
    CompletableFuture<TokenResponse> exchangeToken(
        String userId,
        String connectionName,
        String channelId,
        TokenExchangeRequest exchangeRequest
    );

    /**
     *
     * @param userId          the String value
     * @param connectionName  the String value
     * @param aadResourceUrls the AadResourceUrls value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Map&lt;String, TokenResponse&gt; object
     */
    CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        String userId,
        String connectionName,
        AadResourceUrls aadResourceUrls
    );

    /**
     *
     * @param userId          the String value
     * @param connectionName  the String value
     * @param aadResourceUrls the AadResourceUrls value
     * @param channelId       the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Map&lt;String, TokenResponse&gt; object
     */
    CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        String userId,
        String connectionName,
        AadResourceUrls aadResourceUrls,
        String channelId
    );

    /**
     *
     * @param userId the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Object object
     */
    CompletableFuture<Object> signOut(String userId);

    /**
     *
     * @param userId         the String value
     * @param connectionName the String value
     * @param channelId      the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Object object
     */
    CompletableFuture<Object> signOut(String userId, String connectionName, String channelId);

    /**
     *
     * @param userId the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;TokenStatus&gt; object
     */
    CompletableFuture<List<TokenStatus>> getTokenStatus(String userId);

    /**
     *
     * @param userId    the String value
     * @param channelId the String value
     * @param include   the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;TokenStatus&gt; object
     */
    CompletableFuture<List<TokenStatus>> getTokenStatus(
        String userId,
        String channelId,
        String include
    );

    /**
     * Send a dummy OAuth card when the bot is being used on the Emulator for testing without fetching a real token.
     *
     * @param emulateOAuthCards Indicates whether the Emulator should emulate the OAuth card.
     * @return A task that represents the work queued to execute.
     */
    CompletableFuture<Void> sendEmulateOAuthCards(boolean emulateOAuthCards);
}
