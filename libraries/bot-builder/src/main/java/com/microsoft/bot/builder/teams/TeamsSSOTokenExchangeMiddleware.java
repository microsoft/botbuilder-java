// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.builder.teams;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.NextDelegate;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.StoreItem;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserTokenProvider;
import com.microsoft.bot.connector.rest.RestOAuthClient;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.SignInConstants;
import com.microsoft.bot.schema.TokenExchangeInvokeRequest;
import com.microsoft.bot.schema.TokenExchangeInvokeResponse;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenResponse;

import org.apache.commons.lang3.StringUtils;

/**
 * If the activity name is signin/tokenExchange, this middleware will attempt
 * toexchange the token, and deduplicate the incoming call, ensuring only
 * oneexchange request is processed.
 *
 * If a user is signed into multiple Teams clients, the Bot could receive a
 * "signin/tokenExchange" from each client. Each token exchange request for a
 * specific user login will have an identical Activity.getValue().getId(). Only
 * one of these token exchange requests should be processed by the bot.The
 * others return PreconditionFailed. For a distributed bot in production, this
 * requires a distributed storage ensuring only one token exchange is processed.
 * This middleware supports CosmosDb storage found in
 * Microsoft.getBot().getBuilder().getAzure(), or MemoryStorage for local
 * development. Storage's ETag implementation for token exchange activity
 * deduplication.
 */
public class TeamsSSOTokenExchangeMiddleware implements Middleware {

    private final Storage storage;
    private final String oAuthConnectionName;

    /**
     * Initializes a new instance of the {@link TeamsSSOTokenExchangeMiddleware}
     * class.
     *
     * @param storage        The {@link Storage} to use for deduplication.
     * @param connectionName The connection name to use for the single sign on token
     *                       exchange.
     */
    public TeamsSSOTokenExchangeMiddleware(Storage storage, String connectionName) {
        if (storage == null) {
            throw new IllegalArgumentException("storage cannot be null.");
        }

        if (StringUtils.isBlank(connectionName)) {
            throw new IllegalArgumentException("connectionName cannot be null.");
        }

        this.oAuthConnectionName = connectionName;
        this.storage = storage;
    }

    /**
     * Processes an incoming activity.
     *
     * @param turnContext The context object for this turn.
     * @param next        The delegate to call to continue the bot middleware
     *                    pipeline.
     * @return A task that represents the work queued to execute. Middleware calls
     *         the {@code next} delegate to pass control to the next middleware in
     *         the pipeline. If middleware doesn’t call the next delegate, the
     *         adapter does not call any of the subsequent middleware’s request
     *         handlers or the bot’s receive handler, and the pipeline short
     *         circuits.
     *         <p>
     *         The {@code context} provides information about the incoming activity,
     *         and other data needed to process the activity.
     *         </p>
     *         <p>
     *         {@link TurnContext} {@link com.microsoft.bot.schema.Activity}
     */
    public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
        if (turnContext.getActivity() != null && turnContext.getActivity().getName() != null
                && turnContext.getActivity().getName().equals(SignInConstants.TOKEN_EXCHANGE_OPERATION_NAME)) {
            // If the TokenExchange is NOT successful, the response will have
            // already been sent by ExchangedTokenAsync
            if (!this.exchangedToken(turnContext).join()) {
                return CompletableFuture.completedFuture(null);
            }

            // Only one token exchange should proceed from here. Deduplication is performed
            // second because in the case of failure due to consent required, every caller
            // needs to receive the
            if (!deDuplicatedTokenExchangeId(turnContext).join()) {
                // If the token is not exchangeable, do not process this activity further.
                return CompletableFuture.completedFuture(null);
            }
        }

        return next.next();
    }

    private CompletableFuture<Boolean> deDuplicatedTokenExchangeId(TurnContext turnContext) {

        // Create a StoreItem with Etag of the unique 'signin/tokenExchange' request
        String idValue = null;
        TokenStoreItem storeItem = new TokenStoreItem();
        TokenExchangeInvokeRequest tokenExchangeRequest = Serialization.getAs(turnContext.getActivity().getValue(),
            TokenExchangeInvokeRequest.class);
        if (tokenExchangeRequest != null) {
            idValue = tokenExchangeRequest.getId();
        }

        storeItem.setETag(idValue);

        Map<String, Object> storeItems = new HashMap<String, Object>();
        storeItems.put(storeItem.getStorageKey(turnContext), storeItem);
        try {
            // Writing the StoreItem with ETag of unique id will succeed only once
            storage.write(storeItems).join();
        } catch (Exception ex) {

            // Memory storage throws a generic exception with a Message of 'etag conflict.
            // [other error info]'
            // CosmosDbPartitionedStorage throws: RuntimeException with a message that contains "precondition is
            // not met")
            if (ex.getMessage().contains("eTag conflict") || ex.getMessage().contains("precondition is not met")) {
                // Do NOT proceed processing this message, some other thread or
                // machine already has processed it.

                // Send 200 invoke response.
                return sendInvokeResponse(turnContext, null, HttpURLConnection.HTTP_OK).thenApply(result -> false);
            }
        }

        return CompletableFuture.completedFuture(true);
    }

    private CompletableFuture<Void> sendInvokeResponse(TurnContext turnContext, Object body, int statusCode) {
        Activity activity = new Activity(ActivityTypes.INVOKE_RESPONSE);
        InvokeResponse response = new InvokeResponse(statusCode, body);
        activity.setValue(response);
        return turnContext.sendActivity(activity).thenApply(result -> null);
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private CompletableFuture<Boolean> exchangedToken(TurnContext turnContext) {
        TokenResponse tokenExchangeResponse = null;
        TokenExchangeInvokeRequest tokenExchangeRequest = Serialization.getAs(turnContext.getActivity().getValue(),
                TokenExchangeInvokeRequest.class);

        try {
            RestOAuthClient userTokenClient = turnContext.getTurnState().get(RestOAuthClient.class);
            TokenExchangeRequest exchangeRequest = new TokenExchangeRequest();
            exchangeRequest.setToken(tokenExchangeRequest.getToken());
            if (userTokenClient != null) {
                tokenExchangeResponse = userTokenClient.getUserToken()
                        .exchangeToken(turnContext.getActivity().getFrom().getId(), oAuthConnectionName,
                                turnContext.getActivity().getChannelId(), exchangeRequest)
                        .join();
            } else if (turnContext.getAdapter() instanceof UserTokenProvider) {
                UserTokenProvider adapter = (UserTokenProvider) turnContext.getAdapter();
                tokenExchangeResponse = adapter.exchangeToken(turnContext, oAuthConnectionName,
                        turnContext.getActivity().getFrom().getId(), exchangeRequest).join();
            } else {
                throw new RuntimeException("Token Exchange is not supported by the current adapter.");
            }
        } catch (Exception ex) {
            // Ignore Exceptions
            // If token exchange failed for any reason, tokenExchangeResponse above stays
            // null,
            // and hence we send back a failure invoke response to the caller.
        }

        if (tokenExchangeResponse != null && StringUtils.isEmpty(tokenExchangeResponse.getToken())) {
            // The token could not be exchanged (which could be due to a consent
            // requirement)
            // Notify the sender that PreconditionFailed so they can respond accordingly.

            TokenExchangeInvokeResponse invokeResponse = new TokenExchangeInvokeResponse();
            invokeResponse.setId(tokenExchangeRequest.getId());
            invokeResponse.setConnectionName(oAuthConnectionName);
            invokeResponse.setFailureDetail("The bot is unable to exchange token. Proceed with regular login.");

            sendInvokeResponse(turnContext, invokeResponse, HttpURLConnection.HTTP_PRECON_FAILED);

            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Class to store the etag for token exchange.
     */
    private class TokenStoreItem implements StoreItem {

        private String etag;

        @Override
        public String getETag() {
            return etag;
        }

        @Override
        public void setETag(String withETag) {
            etag = withETag;
        }

        public String getStorageKey(TurnContext turnContext) {
            Activity activity = turnContext.getActivity();
            if (activity.getChannelId() == null) {
                throw new RuntimeException("invalid activity-missing channelId");
            }
            if (activity.getConversation() == null || activity.getConversation().getId() == null) {
                throw new RuntimeException("invalid activity-missing Conversation.Id");
            }

            String channelId = activity.getChannelId();
            String conversationId = activity.getConversation().getId();

            TokenExchangeInvokeRequest tokenExchangeRequest = Serialization.getAs(turnContext.getActivity().getValue(),
                TokenExchangeInvokeRequest.class);

            if (tokenExchangeRequest != null) {
                    return String.format("%s/%s/%s", channelId, conversationId, tokenExchangeRequest.getId());
            } else {
                throw new RuntimeException("Invalid signin/tokenExchange. Missing activity.getValue().getId().");
            }
        }
    }

}
