// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.ExecutorFactory;

import com.microsoft.bot.connector.authentication.AuthenticateRequestResult;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthentication;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.ConnectorFactory;
import com.microsoft.bot.connector.authentication.UserTokenClient;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityEventNames;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ConversationResourceResponse;
import com.microsoft.bot.schema.DeliveryModes;
import com.microsoft.bot.schema.ExpectedReplies;
import com.microsoft.bot.schema.InvokeResponse;
import com.microsoft.bot.schema.ResourceResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An adapter that implements the Bot Framework Protocol and can be hosted in different cloud environments
 * both public and private.
 */
public abstract class CloudAdapterBase extends BotAdapter {
    public static final String CONNECTOR_FACTORY_KEY = "ConnectorFactory";
    public static final String USER_TOKEN_CLIENT_KEY = "UserTokenClient";

    private static final Integer DEFAULT_MS_DELAY = 1000;

    private BotFrameworkAuthentication botFrameworkAuthentication;
    private Logger logger = LoggerFactory.getLogger(CloudAdapterBase.class);

    /**
     * Gets a {@link Logger} to use within this adapter and its subclasses.
     * @return The {@link Logger} instance for this adapter.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the {@link BotFrameworkAuthentication} instance for this adapter.
     * @return The {@link BotFrameworkAuthentication} instance for this adapter.
     */
    public BotFrameworkAuthentication getBotFrameworkAuthentication() {
        return botFrameworkAuthentication;
    }

    /**
     * Initializes a new instance of the {@link CloudAdapterBase} class.
     * @param withBotFrameworkAuthentication The cloud environment used for validating and creating tokens.
     */
    protected CloudAdapterBase(BotFrameworkAuthentication withBotFrameworkAuthentication) {
        if (withBotFrameworkAuthentication == null) {
            throw new IllegalArgumentException("withBotFrameworkAuthentication cannot be null");
        }
        this.botFrameworkAuthentication = withBotFrameworkAuthentication;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ResourceResponse[]> sendActivities(TurnContext context, List<Activity> activities) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("context"));
        }

        if (activities == null) {
            return Async.completeExceptionally(new IllegalArgumentException("activities"));
        }

        if (activities.size() == 0) {
            return Async.completeExceptionally(
                new IllegalArgumentException("Expecting one or more activities, but the array was empty.")
            );
        }

        logger.info(String.format("sendActivities for %d activities.", activities.size()));

        return CompletableFuture.supplyAsync(() -> {
            ResourceResponse[] responses = new ResourceResponse[activities.size()];

            for (int index = 0; index < activities.size(); index++) {
                Activity activity = activities.get(index);

                activity.setId(null);
                ResourceResponse response;

                logger.info(String.format("Sending activity. ReplyToId: %s", activity.getReplyToId()));

                if (activity.isType(ActivityTypes.DELAY)) {
                    int delayMs = activity.getValue() != null
                        ? ((Number) activity.getValue()).intValue()
                        : DEFAULT_MS_DELAY;
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    response = null;
                } else if (activity.isType(ActivityTypes.INVOKE_RESPONSE)) {
                    context.getTurnState().add(BotFrameworkAdapter.INVOKE_RESPONSE_KEY, activity);
                    response = null;
                } else if (
                    activity.isType(ActivityTypes.TRACE)
                        && !StringUtils.equals(activity.getChannelId(), Channels.EMULATOR)
                ) {
                    // no-op
                    response = null;
                } else if (StringUtils.isNotBlank(activity.getReplyToId())) {
                    ConnectorClient connectorClient = context.getTurnState()
                        .get(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY);
                    response = connectorClient.getConversations().replyToActivity(activity).join();
                } else {
                    ConnectorClient connectorClient = context.getTurnState()
                        .get(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY);
                    response = connectorClient.getConversations().sendToConversation(activity).join();
                }
                if (response == null) {
                    response = new ResourceResponse((activity.getId() == null) ? "" : activity.getId());
                }

                responses[index] = response;
            }

            return responses;
        }, ExecutorFactory.getExecutor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ResourceResponse> updateActivity(TurnContext context, Activity activity) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("context"));
        }

        if (activity == null) {
            return Async.completeExceptionally(new IllegalArgumentException("activity"));
        }

        logger.info(String.format("updateActivity activityId: %d", activity.getId()));

        ConnectorClient connectorClient = context.getTurnState().get(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY);
        return connectorClient.getConversations().updateActivity(activity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> deleteActivity(TurnContext context, ConversationReference reference) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("context"));
        }

        if (reference == null) {
            return Async.completeExceptionally(new IllegalArgumentException("reference"));
        }

        logger.info(String.format("deleteActivity Conversation id: %d, activityId: %d",
            reference.getConversation().getId(),
            reference.getActivityId()));

        ConnectorClient connectorClient = context.getTurnState().get(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY);
        return connectorClient.getConversations()
            .deleteActivity(reference.getConversation().getId(), reference.getActivityId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> continueConversation(
        String botAppId,
        ConversationReference reference,
        BotCallbackHandler callback
    ) {
        if (reference == null) {
            return Async.completeExceptionally(new IllegalArgumentException("reference"));
        }

        return processProactive(createClaimsIdentity(botAppId, null),
                                reference.getContinuationActivity(),
                                null,
                                callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> continueConversation(
        ClaimsIdentity claimsIdentity,
        ConversationReference reference,
        BotCallbackHandler callback
    ) {
        if (reference == null) {
            return Async.completeExceptionally(new IllegalArgumentException("reference"));
        }

        return processProactive(claimsIdentity, reference.getContinuationActivity(), null, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> continueConversation(
        ClaimsIdentity claimsIdentity,
        ConversationReference reference,
        String audience,
        BotCallbackHandler callback
    ) {
        if (claimsIdentity == null) {
            return Async.completeExceptionally(new IllegalArgumentException("claimsIdentity"));
        }
        if (reference == null) {
            return Async.completeExceptionally(new IllegalArgumentException("reference"));
        }
        if (callback == null) {
            return Async.completeExceptionally(new IllegalArgumentException("callback"));
        }

        return processProactive(claimsIdentity, reference.getContinuationActivity(), audience, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> continueConversation(
        String botAppId,
        Activity continuationActivity,
        BotCallbackHandler callback
    ) {
        if (callback == null) {
            return Async.completeExceptionally(new IllegalArgumentException("callback"));
        }
        validateContinuationActivity(continuationActivity);

        return processProactive(createClaimsIdentity(botAppId, null), continuationActivity, null, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> continueConversation(
        ClaimsIdentity claimsIdentity,
        Activity continuationActivity,
        BotCallbackHandler callback
    ) {
        if (claimsIdentity == null) {
            return Async.completeExceptionally(new IllegalArgumentException("claimsIdentity"));
        }
        if (callback == null) {
            return Async.completeExceptionally(new IllegalArgumentException("callback"));
        }
        validateContinuationActivity(continuationActivity);

        return processProactive(claimsIdentity, continuationActivity, null, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> continueConversation(
        ClaimsIdentity claimsIdentity,
        Activity continuationActivity,
        String audience,
        BotCallbackHandler callback
    ) {
        if (claimsIdentity == null) {
            return Async.completeExceptionally(new IllegalArgumentException("claimsIdentity"));
        }
        if (callback == null) {
            return Async.completeExceptionally(new IllegalArgumentException("callback"));
        }
        validateContinuationActivity(continuationActivity);

        return processProactive(claimsIdentity, continuationActivity, audience, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> createConversation(String botAppId, String channelId, String serviceUrl,
                                                      String audience, ConversationParameters conversationParameters,
                                                      BotCallbackHandler callback) {
        if (StringUtils.isBlank(serviceUrl)) {
            throw new IllegalArgumentException("serviceUrl cannot be null or empty");
        }
        if (conversationParameters == null) {
            throw new IllegalArgumentException("conversationParameters cannot be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }

        logger.info(
            String.format(
                "createConversation for channel: %s",
                channelId
            )
        );

        // Create a ClaimsIdentity, to create the connector and for adding to the turn context.
        ClaimsIdentity claimsIdentity = this.createClaimsIdentity(botAppId, audience);
        claimsIdentity.claims().put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);

        // Create the connector factory.
        ConnectorFactory connectorFactory = this.botFrameworkAuthentication.createConnectorFactory(claimsIdentity);

        // Create the connector client to use for outbound requests.
        return connectorFactory.create(serviceUrl, audience).thenCompose(connectorClient -> {
            // Make the actual create conversation call using the connector.
            return connectorClient.getConversations().createConversation(conversationParameters).
                thenCompose(createConversationResult -> {
                // Create the create activity to communicate the results to the application.
                Activity createActivity = this.createCreateActivity(createConversationResult,
                                                                    channelId,
                                                                    serviceUrl,
                                                                    conversationParameters);

                // Create a UserTokenClient instance for the application to use. (For example, in the OAuthPrompt.)
                return this.botFrameworkAuthentication.createUserTokenClient(claimsIdentity).
                    thenCompose(userTokenClient -> {
                    TurnContextImpl context = this.createTurnContext(createActivity, claimsIdentity, null,
                                                                    connectorClient, userTokenClient, callback,
                                                                    connectorFactory);

                    // Run the pipeline
                    return this.runPipeline(context, callback).thenApply(result -> null);
                });
            });
        });
    }

    /**
     * The implementation for continue conversation.
     * @param claimsIdentity A {@link ClaimsIdentity} for the conversation.
     * @param continuationActivity The continuation {@link Activity} used to create the {@link TurnContext}.
     * @param audience The audience for the call.
     * @param callback The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> processProactive(
        ClaimsIdentity claimsIdentity,
        Activity continuationActivity,
        String audience,
        BotCallbackHandler callback
    ) {
        logger.info(
            String.format(
                "processProactive for Conversation Id: %s",
                continuationActivity.getConversation().getId()));

        // Create the connector factory and  the inbound request, extracting parameters
        // and then create a connector for outbound requests.
        ConnectorFactory connectorFactory = this.botFrameworkAuthentication.createConnectorFactory(claimsIdentity);

        // Create the connector client to use for outbound requests.
        return connectorFactory.create(continuationActivity.getServiceUrl(), audience).thenCompose(connectorClient -> {
            // Create a UserTokenClient instance for the application to use. (For example, in the OAuthPrompt.)
            return this.botFrameworkAuthentication.createUserTokenClient(claimsIdentity).thenCompose(userTokenClient
                -> {
                    // Create a turn context and run the pipeline.
                    TurnContext context = createTurnContext(
                        continuationActivity,
                        claimsIdentity,
                        audience,
                        connectorClient,
                        userTokenClient,
                        callback,
                        connectorFactory);
                    // Run the pipeline
                    return runPipeline(context, callback);
            });
        });
    }

    /**
     * The implementation for processing an Activity sent to this bot.
     * @param authHeader The authorization header from the http request.
     * @param activity The {@link Activity} to process.
     * @param callback The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute. Containing the InvokeResponse if there is one.
     */
    protected CompletableFuture<InvokeResponse> processActivity(
        String authHeader,
        Activity activity,
        BotCallbackHandler callback) {
        logger.info("processActivity");

        // Authenticate the inbound request,
        // extracting parameters and create a ConnectorFactory for creating a Connector for outbound requests.
        return this.botFrameworkAuthentication.authenticateRequest(activity, authHeader).thenCompose(
            authenticateRequestResult -> processActivity(authenticateRequestResult, activity, callback));
    }

    /**
     * The implementation for processing an Activity sent to this bot.
     * @param authenticateRequestResult The authentication results for this turn.
     * @param activity The {@link Activity} to process.
     * @param callbackHandler The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute. Containing the InvokeResponse if there is one.
     */
    protected CompletableFuture<InvokeResponse> processActivity(
        AuthenticateRequestResult authenticateRequestResult,
        Activity activity,
        BotCallbackHandler callbackHandler
    ) {
        // Set the callerId on the activity.
        activity.setCallerId(authenticateRequestResult.getCallerId());

        // Create the connector client to use for outbound requests.
        return authenticateRequestResult.getConnectorFactory().create(
            activity.getServiceUrl(),
            authenticateRequestResult.getAudience())
            .thenCompose(connectorClient -> {
            // Create a UserTokenClient instance for the application to use.
            // (For example, it would be used in a sign-in prompt.)
            return this.botFrameworkAuthentication.createUserTokenClient(authenticateRequestResult.getClaimsIdentity())
                .thenCompose(userTokenClient -> {
                    // Create a turn context and run the pipeline.
                    TurnContextImpl context = createTurnContext(
                        activity,
                        authenticateRequestResult.getClaimsIdentity(),
                        authenticateRequestResult.getAudience(),
                        connectorClient,
                        userTokenClient,
                        callbackHandler,
                        authenticateRequestResult.getConnectorFactory());

                    // Run the pipeline
                    return runPipeline(context, callbackHandler).thenApply(task -> {
                        // If there are any results they will have been left on the TurnContext.
                        return CompletableFuture.completedFuture(processTurnResults(context));
                    }).thenApply(null);
                });
        });
    }

    /**
     * This is a helper to create the ClaimsIdentity structure from an appId that will be added to the TurnContext.
     * It is intended for use in proactive and named-pipe scenarios.
     * @param botAppId The bot's application id.
     * @param audience The audience for the claims identity
     * @return A {@link ClaimsIdentity} with the audience and appId claims set to the appId.
     */
    protected ClaimsIdentity createClaimsIdentity(String botAppId, String audience) {
        if (botAppId == null) {
            botAppId = "";
        }
        // Related to issue: https://github.com/microsoft/botframework-sdk/issues/6331
        if (audience == null) {
            audience = botAppId;
        }

        // Hand craft Claims Identity.
        HashMap<String, String> claims = new HashMap<String, String>();
        // Adding claims for both Emulator and Channel.
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, audience);
        claims.put(AuthenticationConstants.APPID_CLAIM, botAppId);
        ClaimsIdentity claimsIdentity = new ClaimsIdentity("anonymous", claims);

        return claimsIdentity;
    }

    private Activity createCreateActivity(ConversationResourceResponse createConversationResult, String channelId,
                                          String serviceUrl, ConversationParameters conversationParameters) {
        // Create a conversation update activity to represent the result.
        Activity activity = Activity.createEventActivity();
        activity.setName(ActivityEventNames.CREATE_CONVERSATION);
        activity.setChannelId(channelId);
        activity.setServiceUrl(serviceUrl);
        String activityId = createConversationResult.getActivityId() != null
            ? createConversationResult.getActivityId()
            : UUID.randomUUID().toString();
        activity.setId(activityId);
        ConversationAccount conversation = new ConversationAccount();
        conversation.setId(createConversationResult.getId());
        conversation.setTenantId(conversationParameters.getTenantId());
        activity.setConversation(conversation);
        activity.setChannelData(conversationParameters.getChannelData());
        activity.setRecipient(conversationParameters.getBot());
        return activity;
    }

    private TurnContextImpl createTurnContext(
        Activity activity,
        ClaimsIdentity claimsIdentity,
        String oauthScope,
        ConnectorClient connectorClient,
        UserTokenClient userTokenClient,
        BotCallbackHandler callback,
        ConnectorFactory connectorFactory
    ) {
        TurnContextImpl turnContext = new TurnContextImpl(this, activity);
        turnContext.getTurnState().add(BotAdapter.BOT_IDENTITY_KEY, claimsIdentity);
        turnContext.getTurnState().add(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY, connectorClient);
        turnContext.getTurnState().add(CloudAdapterBase.USER_TOKEN_CLIENT_KEY, userTokenClient);
        turnContext.getTurnState().add(TurnContextImpl.BOT_CALLBACK_HANDLER_KEY, callback);
        turnContext.getTurnState().add(CloudAdapterBase.CONNECTOR_FACTORY_KEY, connectorFactory);
        // in non-skills scenarios the oauth scope value here will be null, so we are checking the value
        if (oauthScope != null) {
            turnContext.getTurnState().add(BotAdapter.OAUTH_SCOPE_KEY, oauthScope);
        }

        return turnContext;
    }

    private void validateContinuationActivity(Activity continuationActivity) {
        if (continuationActivity == null) {
            throw new IllegalArgumentException("continuationActivity");
        }
        if (continuationActivity.getConversation() == null) {
            throw new IllegalArgumentException("The continuation Activity should contain a Conversation value.");
        }
        if (continuationActivity.getServiceUrl() == null) {
            throw new IllegalArgumentException("The continuation Activity should contain a ServiceUrl value.");
        }
    }

    private InvokeResponse processTurnResults(TurnContextImpl turnContext) {
        // Handle ExpectedReplies scenarios where the all the activities have been buffered
        // and sent back at once in an invoke response.
        if (turnContext.getActivity().getDeliveryMode().equals(DeliveryModes.EXPECT_REPLIES)) {
            return new InvokeResponse(
                HttpURLConnection.HTTP_OK,
                new ExpectedReplies(turnContext.getBufferedReplyActivities()));
        }

        // Handle Invoke scenarios where the Bot will return a specific body and return code.
        if (turnContext.getActivity().isType(ActivityTypes.INVOKE)) {
            Activity activityInvokeResponse = turnContext.getTurnState().get(BotFrameworkAdapter.INVOKE_RESPONSE_KEY);
            if (activityInvokeResponse == null) {
                return new InvokeResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED, null);
            }

            return (InvokeResponse) activityInvokeResponse.getValue();
        }

        // No body to return.
        return null;
    }
}
