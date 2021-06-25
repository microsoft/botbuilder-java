// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.integration.AdapterIntegration;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.Conversations;
import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.connector.OAuthClient;
import com.microsoft.bot.connector.OAuthClientConfig;
import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.MicrosoftGovernmentAppCredentials;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import com.microsoft.bot.connector.authentication.SkillValidation;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.connector.rest.RestOAuthClient;
import com.microsoft.bot.schema.AadResourceUrls;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityEventNames;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.CallerIdConstants;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ConversationsResult;
import com.microsoft.bot.schema.DeliveryModes;
import com.microsoft.bot.schema.ExpectedReplies;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.SignInResource;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenExchangeState;
import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.bot.schema.TokenStatus;
import com.microsoft.bot.restclient.retry.RetryStrategy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A bot adapter that can connect a bot to a service endpoint.
 *
 * The bot adapter encapsulates authentication processes and sends activities to
 * and receives activities from the Bot Connector Service. When your bot
 * receives an activity, the adapter creates a context object, passes it to your
 * bot's application logic, and sends responses back to the user's channel.
 * <p>
 * Use {@link #use(Middleware)} to add {@link Middleware} objects to your
 * adapter’s middleware collection. The adapter processes and directs incoming
 * activities in through the bot middleware pipeline to your bot’s logic and
 * then back out again. As each activity flows in and out of the bot, each piece
 * of middleware can inspect or act upon the activity, both before and after the
 * bot logic runs.
 * </p>
 * <p>
 * {@link TurnContext} {@link Activity} {@link Bot} {@link Middleware}
 */
public class BotFrameworkAdapter extends BotAdapter
    implements AdapterIntegration, UserTokenProvider, ConnectorClientBuilder {
    /**
     * Key to store InvokeResponse.
     */
    public static final String INVOKE_RESPONSE_KEY = "BotFrameworkAdapter.InvokeResponse";

    /**
     * Key to store ConnectorClient.
     */
    public static final String CONNECTOR_CLIENT_KEY = "ConnectorClient";

    /**
     * Key to store TeamsConnectorClient. For testing only.
     */
    public static final String TEAMSCONNECTOR_CLIENT_KEY = "TeamsConnectorClient";

    private AppCredentials appCredentials;

    /**
     * The credential provider.
     */
    private final CredentialProvider credentialProvider;

    /**
     * The channel provider.
     */
    private ChannelProvider channelProvider;

    /**
     * The authentication configuration.
     */
    private AuthenticationConfiguration authConfiguration;

    /**
     * Rest RetryStrategy.
     */
    private final RetryStrategy connectorClientRetryStrategy;

    /**
     * AppCredentials dictionary.
     */
    private Map<String, AppCredentials> appCredentialMap = new ConcurrentHashMap<>();

    /**
     * ConnectorClient cache.
     */
    private Map<String, ConnectorClient> connectorClients = new ConcurrentHashMap<>();

    /**
     * OAuthClient cache.
     */
    private Map<String, OAuthClient> oAuthClients = new ConcurrentHashMap<>();

    /**
     * Initializes a new instance of the {@link BotFrameworkAdapter} class, using a
     * credential provider.
     *
     * @param withCredentialProvider The credential provider.
     */
    public BotFrameworkAdapter(CredentialProvider withCredentialProvider) {
        this(withCredentialProvider, null, null, null);
    }

    /**
     * Initializes a new instance of the {@link BotFrameworkAdapter} class, using a
     * credential provider.
     *
     * @param withCredentialProvider The credential provider.
     * @param withChannelProvider    The channel provider.
     * @param withRetryStrategy      Retry policy for retrying HTTP operations.
     * @param withMiddleware         The middleware to initially add to the adapter.
     */
    public BotFrameworkAdapter(
        CredentialProvider withCredentialProvider,
        ChannelProvider withChannelProvider,
        RetryStrategy withRetryStrategy,
        Middleware withMiddleware
    ) {
        this(
            withCredentialProvider,
            new AuthenticationConfiguration(),
            withChannelProvider,
            withRetryStrategy,
            withMiddleware
        );
    }

    /**
     * Initializes a new instance of the {@link BotFrameworkAdapter} class, using a
     * credential provider.
     *
     * @param withCredentialProvider The credential provider.
     * @param withAuthConfig         The authentication configuration.
     * @param withChannelProvider    The channel provider.
     * @param withRetryStrategy      Retry policy for retrying HTTP operations.
     * @param withMiddleware         The middleware to initially add to the adapter.
     */
    public BotFrameworkAdapter(
        CredentialProvider withCredentialProvider,
        AuthenticationConfiguration withAuthConfig,
        ChannelProvider withChannelProvider,
        RetryStrategy withRetryStrategy,
        Middleware withMiddleware
    ) {
        if (withCredentialProvider == null) {
            throw new IllegalArgumentException("CredentialProvider cannot be null");
        }

        if (withAuthConfig == null) {
            throw new IllegalArgumentException("AuthenticationConfiguration cannot be null");
        }

        credentialProvider = withCredentialProvider;
        channelProvider = withChannelProvider;
        connectorClientRetryStrategy = withRetryStrategy;
        authConfiguration = withAuthConfig;

        // Relocate the tenantId field used by MS Teams to a new location (from
        // channelData to conversation)
        // This will only occur on activities from teams that include tenant info in
        // channelData but NOT in
        // conversation, thus should be future friendly. However, once the transition is
        // complete. we can
        // remove this.
        use(new TenantIdWorkaroundForTeamsMiddleware());

        if (withMiddleware != null) {
            use(withMiddleware);
        }
    }

    /**
     * Initializes a new instance of the {@link BotFrameworkAdapter} class, using a
     * credential provider.
     *
     * @param withCredentials     The credentials to use.
     * @param withAuthConfig      The authentication configuration.
     * @param withChannelProvider The channel provider.
     * @param withRetryStrategy   Retry policy for retrying HTTP operations.
     * @param withMiddleware      The middleware to initially add to the adapter.
     */
    public BotFrameworkAdapter(
        AppCredentials withCredentials,
        AuthenticationConfiguration withAuthConfig,
        ChannelProvider withChannelProvider,
        RetryStrategy withRetryStrategy,
        Middleware withMiddleware
    ) {
        if (withCredentials == null) {
            throw new IllegalArgumentException("credentials");
        }
        appCredentials = withCredentials;

        credentialProvider = new SimpleCredentialProvider(withCredentials.getAppId(), null);
        channelProvider = withChannelProvider;
        connectorClientRetryStrategy = withRetryStrategy;

        if (withAuthConfig == null) {
            throw new IllegalArgumentException("authConfig");
        }
        authConfiguration = withAuthConfig;

        // Relocate the tenantId field used by MS Teams to a new location (from
        // channelData to conversation)
        // This will only occur on activities from teams that include tenant info in
        // channelData but NOT in
        // conversation, thus should be future friendly. However, once the transition is
        // complete. we can
        // remove this.
        use(new TenantIdWorkaroundForTeamsMiddleware());

        if (withMiddleware != null) {
            use(withMiddleware);
        }
    }

    /**
     * Sends a proactive message from the bot to a conversation.
     *
     * <p>
     * Call this method to proactively send a message to a conversation. Most
     * channels require a user to initiate a conversation with a bot before the bot
     * can send activities to the user.
     * </p>
     * <p>
     * This overload differers from the Node implementation by requiring the BotId
     * to be passed in. The .Net code allows multiple bots to be hosted in a single
     * adapter which isn't something supported by Node.
     * </p>
     *
     * {@link #processActivity(String, Activity, BotCallbackHandler)}
     * {@link BotAdapter#runPipeline(TurnContext, BotCallbackHandler)}
     *
     * @param botAppId  The application ID of the bot. This is the appId returned by
     *                  Portal registration, and is generally found in the
     *                  "MicrosoftAppId" parameter in appSettings.json.
     * @param reference A reference to the conversation to continue.
     * @param callback  The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException botAppId, reference, or callback is null.
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

        if (callback == null) {
            return Async.completeExceptionally(new IllegalArgumentException("callback"));
        }

        botAppId = botAppId == null ? "" : botAppId;

        // Hand craft Claims Identity.
        // Adding claims for both Emulator and Channel.
        HashMap<String, String> claims = new HashMap<String, String>();
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, botAppId);
        claims.put(AuthenticationConstants.APPID_CLAIM, botAppId);

        ClaimsIdentity claimsIdentity = new ClaimsIdentity("ExternalBearer", claims);
        String audience = getBotFrameworkOAuthScope();

        return continueConversation(claimsIdentity, reference, audience, callback);
    }

    /**
     * Sends a proactive message to a conversation.
     *
     * <p>
     * Call this method to proactively send a message to a conversation. Most
     * channels require a user to initiate a conversation with a bot before the bot
     * can send activities to the user.
     * </p>
     *
     * @param claimsIdentity A ClaimsIdentity reference for the conversation.
     * @param reference      A reference to the conversation to continue.
     * @param callback       The method to call for the result bot turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> continueConversation(
        ClaimsIdentity claimsIdentity,
        ConversationReference reference,
        BotCallbackHandler callback
    ) {
        return continueConversation(claimsIdentity, reference, getBotFrameworkOAuthScope(), callback);
    }

    /**
     * Sends a proactive message to a conversation.
     *
     * <p>
     * Call this method to proactively send a message to a conversation. Most
     * channels require a user to initiate a conversation with a bot before the bot
     * can send activities to the user.
     * </p>
     *
     * @param claimsIdentity A ClaimsIdentity reference for the conversation.
     * @param reference      A reference to the conversation to continue.
     * @param audience       A value signifying the recipient of the proactive
     *                       message.
     * @param callback       The method to call for the result bot turn.
     * @return A task that represents the work queued to execute.
     */
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

        if (StringUtils.isEmpty(audience)) {
            return Async.completeExceptionally(new IllegalArgumentException("audience cannot be null or empty"));
        }

        CompletableFuture<Void> pipelineResult = new CompletableFuture<>();

        try (TurnContextImpl context = new TurnContextImpl(this, reference.getContinuationActivity())) {
            context.getTurnState().add(BOT_IDENTITY_KEY, claimsIdentity);
            context.getTurnState().add(OAUTH_SCOPE_KEY, audience);

            return createConnectorClient(reference.getServiceUrl(), claimsIdentity, audience)
                .thenCompose(connectorClient -> {
                    context.getTurnState().add(CONNECTOR_CLIENT_KEY, connectorClient);
                    return runPipeline(context, callback);
                });
        } catch (Exception e) {
            pipelineResult.completeExceptionally(e);
        }

        return pipelineResult;
    }

    /**
     * Adds middleware to the adapter's pipeline.
     *
     * Middleware is added to the adapter at initialization time. For each turn, the
     * adapter calls middleware in the order in which you added it.
     *
     * @param middleware The middleware to add.
     * @return The updated adapter object.
     */
    public BotFrameworkAdapter use(Middleware middleware) {
        getMiddlewareSet().use(middleware);
        return this;
    }

    /**
     * Creates a turn context and runs the middleware pipeline for an incoming
     * activity.
     *
     * @param authHeader The HTTP authentication header of the request.
     * @param activity   The incoming activity.
     * @param callback   The code to run at the end of the adapter's middleware
     *                   pipeline.
     * @return A task that represents the work queued to execute. If the activity
     *         type was 'Invoke' and the corresponding key (channelId + activityId)
     *         was found then an InvokeResponse is returned, otherwise null is
     *         returned.
     * @throws IllegalArgumentException Activity is null.
     */
    public CompletableFuture<InvokeResponse> processActivity(
        String authHeader,
        Activity activity,
        BotCallbackHandler callback
    ) {
        if (activity == null) {
            return Async.completeExceptionally(new IllegalArgumentException("Activity"));
        }

        return JwtTokenValidation
            .authenticateRequest(activity, authHeader, credentialProvider, channelProvider, authConfiguration)
            .thenCompose(claimsIdentity -> processActivity(claimsIdentity, activity, callback));
    }

    /**
     * Creates a turn context and runs the middleware pipeline for an incoming
     * activity.
     *
     * @param identity A {@link ClaimsIdentity} for the request.
     * @param activity The incoming activity.
     * @param callback The code to run at the end of the adapter's middleware
     *                 pipeline.
     * @return A task that represents the work queued to execute. If the activity
     *         type was 'Invoke' and the corresponding key (channelId + activityId)
     *         was found then an InvokeResponse is returned, otherwise null is
     *         returned.
     * @throws IllegalArgumentException Activity is null.
     */
    public CompletableFuture<InvokeResponse> processActivity(
        ClaimsIdentity identity,
        Activity activity,
        BotCallbackHandler callback
    ) {
        if (activity == null) {
            return Async.completeExceptionally(new IllegalArgumentException("Activity"));
        }

        CompletableFuture<InvokeResponse> pipelineResult = new CompletableFuture<>();

        try (TurnContextImpl context = new TurnContextImpl(this, activity)) {
            activity.setCallerId(generateCallerId(identity).join());
            context.getTurnState().add(BOT_IDENTITY_KEY, identity);

            // The OAuthScope is also stored on the TurnState to get the correct
            // AppCredentials if fetching a token is required.
            String scope = SkillValidation.isSkillClaim(identity.claims())
                ? String.format("%s/.default", JwtTokenValidation.getAppIdFromClaims(identity.claims()))
                : getBotFrameworkOAuthScope();

            context.getTurnState().add(OAUTH_SCOPE_KEY, scope);

            pipelineResult = createConnectorClient(activity.getServiceUrl(), identity, scope)

                // run pipeline
                .thenCompose(connectorClient -> {
                    context.getTurnState().add(CONNECTOR_CLIENT_KEY, connectorClient);
                    return runPipeline(context, callback);
                })
                .thenCompose(result -> {
                    // Handle ExpectedReplies scenarios where the all the activities have been
                    // buffered and sent back at once in an invoke response.
                    if (
                        DeliveryModes
                            .fromString(context.getActivity().getDeliveryMode()) == DeliveryModes.EXPECT_REPLIES
                    ) {
                        return CompletableFuture.completedFuture(
                            new InvokeResponse(
                                HttpURLConnection.HTTP_OK,
                                new ExpectedReplies(context.getBufferedReplyActivities())
                            )
                        );
                    }

                    // Handle Invoke scenarios, which deviate from the request/response model in
                    // that the Bot will return a specific body and return code.
                    if (activity.isType(ActivityTypes.INVOKE)) {
                        Activity invokeResponse = context.getTurnState().get(INVOKE_RESPONSE_KEY);
                        if (invokeResponse == null) {
                            return CompletableFuture
                                .completedFuture(new InvokeResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED, null));
                        } else {
                            return CompletableFuture.completedFuture((InvokeResponse) invokeResponse.getValue());
                        }
                    }

                    // For all non-invoke scenarios, the HTTP layers above don't have to mess
                    // with the Body and return codes.
                    return CompletableFuture.completedFuture(null);
                });
        } catch (Exception e) {
            pipelineResult.completeExceptionally(e);
        }

        return pipelineResult;
    }

    @SuppressWarnings("PMD")
    private CompletableFuture<String> generateCallerId(ClaimsIdentity claimsIdentity) {
        return credentialProvider.isAuthenticationDisabled().thenApply(is_auth_disabled -> {
            // Is the bot accepting all incoming messages?
            if (is_auth_disabled) {
                return null;
            }

            // Is the activity from another bot?
            if (SkillValidation.isSkillClaim(claimsIdentity.claims())) {
                return String.format(
                    "%s%s",
                    CallerIdConstants.BOT_TO_BOT_PREFIX,
                    JwtTokenValidation.getAppIdFromClaims(claimsIdentity.claims())
                );
            }

            // Is the activity from Public Azure?
            if (channelProvider == null || channelProvider.isPublicAzure()) {
                return CallerIdConstants.PUBLIC_AZURE_CHANNEL;
            }

            // Is the activity from Azure Gov?
            if (channelProvider != null && channelProvider.isGovernment()) {
                return CallerIdConstants.US_GOV_CHANNEL;
            }

            // Return null so that the callerId is cleared.
            return null;
        });
    }

    /**
     * Sends activities to the conversation.
     *
     * @param context    The context object for the turn.
     * @param activities The activities to send.
     * @return A task that represents the work queued to execute. If the activities
     *         are successfully sent, the task result contains an array of
     *         {@link ResourceResponse} objects containing the IDs that the
     *         receiving channel assigned to the activities.
     *
     *         {@link TurnContext#onSendActivities(SendActivitiesHandler)}
     */
    @SuppressWarnings("checkstyle:EmptyBlock, checkstyle:linelength")
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

        return CompletableFuture.supplyAsync(() -> {
            ResourceResponse[] responses = new ResourceResponse[activities.size()];

            /*
             * NOTE: we're using for here (vs. foreach) because we want to simultaneously
             * index into the activities array to get the activity to process as well as use
             * that index to assign the response to the responses array and this is the most
             * cost effective way to do that.
             */
            for (int index = 0; index < activities.size(); index++) {
                Activity activity = activities.get(index);

                // Clients and bots SHOULD NOT include an id field in activities they generate.
                activity.setId(null);

                ResourceResponse response;
                if (activity.isType(ActivityTypes.DELAY)) {
                    // The Activity Schema doesn't have a delay type build in, so it's simulated
                    // here in the Bot. This matches the behavior in the Node connector.
                    int delayMs = (int) activity.getValue();
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    // No need to create a response. One will be created below.
                    response = null;
                } else if (activity.isType(ActivityTypes.INVOKE_RESPONSE)) {
                    context.getTurnState().add(INVOKE_RESPONSE_KEY, activity);
                    // No need to create a response. One will be created below.
                    response = null;
                } else if (
                    activity.isType(ActivityTypes.TRACE)
                        && !StringUtils.equals(activity.getChannelId(), Channels.EMULATOR)
                ) {
                    // if it is a Trace activity we only send to the channel if it's the emulator.
                    response = null;
                } else if (!StringUtils.isEmpty(activity.getReplyToId())) {
                    ConnectorClient connectorClient = context.getTurnState().get(CONNECTOR_CLIENT_KEY);
                    response = connectorClient.getConversations().replyToActivity(activity).join();
                } else {
                    ConnectorClient connectorClient = context.getTurnState().get(CONNECTOR_CLIENT_KEY);
                    response = connectorClient.getConversations().sendToConversation(activity).join();
                }

                // If No response is set, then default to a "simple" response. This can't really
                // be done above, as there are cases where the ReplyTo/SendTo methods will also
                // return null (See below) so the check has to happen here.
                //
                // Note: In addition to the Invoke / Delay / Activity cases, this code also
                // applies with Skype and Teams with regards to typing events. When sending a
                // typing event in these channels they do not return a RequestResponse which
                // causes the bot to blow up.
                //
                // https://github.com/Microsoft/botbuilder-dotnet/issues/460
                // bug report : https://github.com/Microsoft/botbuilder-dotnet/issues/465
                if (response == null) {
                    response = new ResourceResponse((activity.getId() == null) ? "" : activity.getId());
                }

                responses[index] = response;
            }

            return responses;
        }, ExecutorFactory.getExecutor());
    }

    /**
     * Replaces an existing activity in the conversation.
     *
     * @param context  The context object for the turn.
     * @param activity New replacement activity.
     * @return A task that represents the work queued to execute. If the activity is
     *         successfully sent, the task result contains a
     *         {@link ResourceResponse} object containing the ID that the receiving
     *         channel assigned to the activity.
     *         <p>
     *         Before calling this, set the ID of the replacement activity to the ID
     *         of the activity to replace.
     *         </p>
     *         {@link TurnContext#onUpdateActivity(UpdateActivityHandler)}
     */
    @Override
    public CompletableFuture<ResourceResponse> updateActivity(TurnContext context, Activity activity) {
        ConnectorClient connectorClient = context.getTurnState().get(CONNECTOR_CLIENT_KEY);
        return connectorClient.getConversations().updateActivity(activity);
    }

    /**
     * Deletes an existing activity in the conversation.
     *
     * @param context   The context object for the turn.
     * @param reference Conversation reference for the activity to delete.
     * @return A task that represents the work queued to execute.
     *         {@link TurnContext#onDeleteActivity(DeleteActivityHandler)}
     */
    @Override
    public CompletableFuture<Void> deleteActivity(TurnContext context, ConversationReference reference) {
        ConnectorClient connectorClient = context.getTurnState().get(CONNECTOR_CLIENT_KEY);
        return connectorClient.getConversations()
            .deleteActivity(reference.getConversation().getId(), reference.getActivityId());
    }

    /**
     * Deletes a member from the current conversation.
     *
     * @param context  The context object for the turn.
     * @param memberId ID of the member to delete from the conversation
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> deleteConversationMember(TurnContextImpl context, String memberId) {
        if (context.getActivity().getConversation() == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException("BotFrameworkAdapter.deleteConversationMember(): missing conversation")
            );
        }

        if (StringUtils.isEmpty(context.getActivity().getConversation().getId())) {
            return Async.completeExceptionally(
                new IllegalArgumentException("BotFrameworkAdapter.deleteConversationMember(): missing conversation.id")
            );
        }

        ConnectorClient connectorClient = context.getTurnState().get(CONNECTOR_CLIENT_KEY);
        String conversationId = context.getActivity().getConversation().getId();
        return connectorClient.getConversations().deleteConversationMember(conversationId, memberId);
    }

    /**
     * Lists the members of a given activity.
     *
     * @param context The context object for the turn.
     * @return List of Members of the activity
     */
    public CompletableFuture<List<ChannelAccount>> getActivityMembers(TurnContextImpl context) {
        return getActivityMembers(context, null);
    }

    /**
     * Lists the members of a given activity.
     *
     * @param context    The context object for the turn.
     * @param activityId (Optional) Activity ID to enumerate. If not specified the
     *                   current activities ID will be used.
     * @return List of Members of the activity
     */
    public CompletableFuture<List<ChannelAccount>> getActivityMembers(TurnContextImpl context, String activityId) {
        // If no activity was passed in, use the current activity.
        if (activityId == null) {
            activityId = context.getActivity().getId();
        }

        if (context.getActivity().getConversation() == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException("BotFrameworkAdapter.GetActivityMembers(): missing conversation")
            );
        }

        if (StringUtils.isEmpty(context.getActivity().getConversation().getId())) {
            return Async.completeExceptionally(
                new IllegalArgumentException("BotFrameworkAdapter.GetActivityMembers(): missing conversation.id")
            );
        }

        ConnectorClient connectorClient = context.getTurnState().get(CONNECTOR_CLIENT_KEY);
        String conversationId = context.getActivity().getConversation().getId();

        return connectorClient.getConversations().getActivityMembers(conversationId, activityId);
    }

    /**
     * Lists the members of the current conversation.
     *
     * @param context The context object for the turn.
     * @return List of Members of the current conversation
     */
    public CompletableFuture<List<ChannelAccount>> getConversationMembers(TurnContextImpl context) {
        if (context.getActivity().getConversation() == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException("BotFrameworkAdapter.GetActivityMembers(): missing conversation")
            );
        }

        if (StringUtils.isEmpty(context.getActivity().getConversation().getId())) {
            return Async.completeExceptionally(
                new IllegalArgumentException("BotFrameworkAdapter.GetActivityMembers(): missing conversation.id")
            );
        }

        ConnectorClient connectorClient = context.getTurnState().get(CONNECTOR_CLIENT_KEY);
        String conversationId = context.getActivity().getConversation().getId();

        return connectorClient.getConversations().getConversationMembers(conversationId);
    }

    /**
     * Lists the Conversations in which this bot has participated for a given
     * channel server. The channel server returns results in pages and each page
     * will include a `continuationToken` that can be used to fetch the next page of
     * results from the server.
     *
     * @param serviceUrl  The URL of the channel server to query. This can be
     *                    retrieved from `context.activity.serviceUrl`.
     * @param credentials The credentials needed for the Bot to connect to
     *                    the.services().
     * @return List of Members of the current conversation
     *         <p>
     *         This overload may be called from outside the context of a
     *         conversation, as only the Bot's ServiceUrl and credentials are
     *         required.
     */
    public CompletableFuture<ConversationsResult> getConversations(
        String serviceUrl,
        MicrosoftAppCredentials credentials
    ) {
        return getConversations(serviceUrl, credentials, null);
    }

    /**
     * Lists the Conversations in which this bot has participated for a given
     * channel server. The channel server returns results in pages and each page
     * will include a `continuationToken` that can be used to fetch the next page of
     * results from the server.
     *
     * This overload may be called from outside the context of a conversation, as
     * only the Bot's ServiceUrl and credentials are required.
     *
     * @param serviceUrl        The URL of the channel server to query. This can be
     *                          retrieved from `context.activity.serviceUrl`.
     * @param credentials       The credentials needed for the Bot to connect to
     *                          the.services().
     * @param continuationToken The continuation token from the previous page of
     *                          results.
     * @return List of Members of the current conversation
     */
    public CompletableFuture<ConversationsResult> getConversations(
        String serviceUrl,
        MicrosoftAppCredentials credentials,
        String continuationToken
    ) {
        if (StringUtils.isEmpty(serviceUrl)) {
            return Async.completeExceptionally(new IllegalArgumentException("serviceUrl"));
        }

        if (credentials == null) {
            return Async.completeExceptionally(new IllegalArgumentException("credentials"));
        }

        return getOrCreateConnectorClient(serviceUrl, credentials)
            .thenCompose(connectorClient -> connectorClient.getConversations().getConversations(continuationToken));
    }

    /**
     * Lists the Conversations in which this bot has participated for a given
     * channel server. The channel server returns results in pages and each page
     * will include a `continuationToken` that can be used to fetch the next page of
     * results from the server.
     *
     * This overload may be called during standard Activity processing, at which
     * point the Bot's service URL and credentials that are part of the current
     * activity processing pipeline will be used.
     *
     * @param context The context object for the turn.
     * @return List of Members of the current conversation
     */
    public CompletableFuture<ConversationsResult> getConversations(TurnContextImpl context) {
        ConnectorClient connectorClient = context.getTurnState().get(CONNECTOR_CLIENT_KEY);
        return connectorClient.getConversations().getConversations();
    }

    /**
     * Lists the Conversations in which this bot has participated for a given
     * channel server. The channel server returns results in pages and each page
     * will include a `continuationToken` that can be used to fetch the next page of
     * results from the server.
     *
     * This overload may be called during standard Activity processing, at which
     * point the Bot's service URL and credentials that are part of the current
     * activity processing pipeline will be used.
     *
     * @param context           The context object for the turn.
     * @param continuationToken The continuation token from the previous page of
     *                          results.
     * @return List of Members of the current conversation
     */
    public CompletableFuture<ConversationsResult> getConversations(TurnContextImpl context, String continuationToken) {
        ConnectorClient connectorClient = context.getTurnState().get(CONNECTOR_CLIENT_KEY);
        return connectorClient.getConversations().getConversations(continuationToken);
    }

    /**
     * Attempts to retrieve the token for a user that's in a login flow.
     *
     * @param context        Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @param magicCode      (Optional) Optional user entered code to validate.
     * @return Token Response
     */
    @Override
    public CompletableFuture<TokenResponse> getUserToken(TurnContext context, String connectionName, String magicCode) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }
        if (context.getActivity().getFrom() == null || StringUtils.isEmpty(context.getActivity().getFrom().getId())) {
            return Async.completeExceptionally(
                new IllegalArgumentException("BotFrameworkAdapter.getUserToken(): missing from or from.id")
            );
        }

        if (StringUtils.isEmpty(connectionName)) {
            return Async.completeExceptionally(new IllegalArgumentException("connectionName"));
        }

        return createOAuthAPIClient(context, null).thenCompose(
            oAuthClient -> oAuthClient.getUserToken()
                .getToken(
                    context.getActivity().getFrom().getId(),
                    connectionName,
                    context.getActivity().getChannelId(),
                    magicCode
                )
        );
    }

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name.
     *
     * @param context        Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<String> getOAuthSignInLink(TurnContext context, String connectionName) {
        return getOAuthSignInLink(context, null, connectionName);
    }

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name.
     *
     * @param context        Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @param userId         The user id that will be associated with the token.
     * @param finalRedirect  The final URL that the OAuth flow will redirect to.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<String> getOAuthSignInLink(
        TurnContext context,
        String connectionName,
        String userId,
        String finalRedirect
    ) {
        return getOAuthSignInLink(context, null, connectionName, userId, finalRedirect);
    }

    /**
     * Signs the user out with the token server.
     *
     * @param context        Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> signOutUser(TurnContext context, String connectionName, String userId) {
        return signOutUser(context, null, connectionName, userId);
    }

    /**
     * Retrieves the token status for each configured connection for the given user.
     *
     * @param context       Context for the current turn of conversation with the
     *                      user.
     * @param userId        The user Id for which token status is retrieved.
     * @param includeFilter Optional comma separated list of connection's to
     *                      include. Blank will return token status for all
     *                      configured connections.
     * @return Array of {@link TokenStatus}.
     */
    @Override
    public CompletableFuture<List<TokenStatus>> getTokenStatus(
        TurnContext context,
        String userId,
        String includeFilter
    ) {
        return getTokenStatus(context, null, userId, includeFilter);
    }

    /**
     * Retrieves Azure Active Directory tokens for particular resources on a
     * configured connection.
     *
     * @param context        Context for the current turn of conversation with the
     *                       user.
     * @param connectionName The name of the Azure Active Directory connection
     *                       configured with this bot.
     * @param resourceUrls   The list of resource URLs to retrieve tokens for.
     * @param userId         The user Id for which tokens are retrieved. If passing
     *                       in null the userId is taken from the Activity in the
     *                       TurnContext.
     * @return Map of resourceUrl to the corresponding {@link TokenResponse}.
     */
    @Override
    public CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        TurnContext context,
        String connectionName,
        String[] resourceUrls,
        String userId
    ) {
        return getAadTokens(context, null, connectionName, resourceUrls, userId);
    }

    /**
     * Creates a conversation on the specified channel.
     *
     * To start a conversation, your bot must know its account information and the
     * user's account information on that channel. Most channels only support
     * initiating a direct message (non-group) conversation.
     * <p>
     * The adapter attempts to create a new conversation on the channel, and then
     * sends a {@code conversationUpdate} activity through its middleware pipeline
     * to the {@code callback} method.
     * </p>
     * <p>
     * If the conversation is established with the specified users, the ID of the
     * activity's {@link Activity#getConversation} will contain the ID of the new
     * conversation.
     * </p>
     *
     * @param channelId              The ID for the channel.
     * @param serviceUrl             The channel's service URL endpoint.
     * @param credentials            The application credentials for the bot.
     * @param conversationParameters The conversation information to use to create
     *                               the conversation.
     * @param callback               The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> createConversation(
        String channelId,
        String serviceUrl,
        MicrosoftAppCredentials credentials,
        ConversationParameters conversationParameters,
        BotCallbackHandler callback
    ) {
        return getOrCreateConnectorClient(serviceUrl, credentials).thenCompose(connectorClient -> {
            Conversations conversations = connectorClient.getConversations();
            return conversations.createConversation(conversationParameters)
                .thenCompose(conversationResourceResponse -> {
                    // Create a event activity to represent the result.
                    Activity eventActivity = Activity.createEventActivity();
                    eventActivity.setName(ActivityEventNames.CREATE_CONVERSATION);
                    eventActivity.setChannelId(channelId);
                    eventActivity.setServiceUrl(serviceUrl);
                    eventActivity.setId(
                        (conversationResourceResponse.getActivityId() != null)
                            ? conversationResourceResponse.getActivityId()
                            : UUID.randomUUID().toString()
                    );
                    eventActivity.setConversation(new ConversationAccount(conversationResourceResponse.getId()) {
                        {
                            setTenantId(conversationParameters.getTenantId());
                        }
                    });
                    eventActivity.setChannelData(conversationParameters.getChannelData());
                    eventActivity.setRecipient(conversationParameters.getBot());

                    // run pipeline
                    CompletableFuture<Void> result = new CompletableFuture<>();
                    try (TurnContextImpl context = new TurnContextImpl(this, eventActivity)) {
                        HashMap<String, String> claims = new HashMap<String, String>();
                        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, credentials.getAppId());
                        claims.put(AuthenticationConstants.APPID_CLAIM, credentials.getAppId());
                        claims.put(AuthenticationConstants.SERVICE_URL_CLAIM, serviceUrl);
                        ClaimsIdentity claimsIdentity = new ClaimsIdentity("anonymous", claims);

                        context.getTurnState().add(BOT_IDENTITY_KEY, claimsIdentity);
                        context.getTurnState().add(CONNECTOR_CLIENT_KEY, connectorClient);

                        result = runPipeline(context, callback);
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                    }
                    return result;
                });
        });
    }

    /**
     * Creates a conversation on the specified channel.
     *
     * To start a conversation, your bot must know its account information and the
     * user's account information on that channel. Most channels only support
     * initiating a direct message (non-group) conversation.
     * <p>
     * The adapter attempts to create a new conversation on the channel, and then
     * sends a {@code conversationUpdate} activity through its middleware pipeline
     * to the {@code callback} method.
     * </p>
     * <p>
     * If the conversation is established with the specified users, the ID of the
     * activity's {@link Activity#getConversation} will contain the ID of the new
     * conversation.
     * </p>
     *
     * @param channelId              The ID for the channel.
     * @param serviceUrl             The channel's service URL endpoint.
     * @param credentials            The application credentials for the bot.
     * @param conversationParameters The conversation information to use to create
     *                               the conversation.
     * @param callback               The method to call for the resulting bot turn.
     * @param reference              A conversation reference that contains the
     *                               tenant.
     * @return A task that represents the work queued to execute.
     */
    @SuppressWarnings("checkstyle:InnerAssignment")
    @Deprecated
    public CompletableFuture<Void> createConversation(
        String channelId,
        String serviceUrl,
        MicrosoftAppCredentials credentials,
        ConversationParameters conversationParameters,
        BotCallbackHandler callback,
        ConversationReference reference
    ) {
        if (reference.getConversation() == null) {
            return CompletableFuture.completedFuture(null);
        }

        String tenantId = reference.getConversation().getTenantId();
        if (!StringUtils.isEmpty(tenantId)) {
            // Putting tenantId in channelData is a temporary solution while we wait for the
            // Teams API to be updated
            if (conversationParameters.getChannelData() != null) {
                ((ObjectNode) conversationParameters.getChannelData()).set(
                    "tenantId",
                    JsonNodeFactory.instance.textNode(tenantId)
                );
            } else {
                ObjectNode channelData = JsonNodeFactory.instance.objectNode();
                channelData.set(
                    "tenant",
                    JsonNodeFactory.instance.objectNode()
                        .set("tenantId", JsonNodeFactory.instance.textNode(tenantId))
                );

                conversationParameters.setChannelData(channelData);
            }

            conversationParameters.setTenantId(tenantId);
        }

        return createConversation(channelId, serviceUrl, credentials, conversationParameters, callback);
    }

    /**
     * Creates an OAuth client for the bot.
     *
     * <p>
     * Note: This is protected primarily so that unit tests can override to provide
     * a mock OAuthClient.
     * </p>
     *
     * @param turnContext         The context object for the current turn.
     * @param oAuthAppCredentials The credentials to use when creating the client.
     *                            If null, the default credentials will be used.
     * @return An OAuth client for the bot.
     */
    protected CompletableFuture<OAuthClient> createOAuthAPIClient(
        TurnContext turnContext,
        AppCredentials oAuthAppCredentials
    ) {
        if (
            !OAuthClientConfig.emulateOAuthCards
                && StringUtils.equalsIgnoreCase(turnContext.getActivity().getChannelId(), Channels.EMULATOR)
                && credentialProvider.isAuthenticationDisabled().join()
        ) {
            OAuthClientConfig.emulateOAuthCards = true;
        }
        AtomicBoolean sendEmulateOAuthCards = new AtomicBoolean(false);

        String appId = getBotAppId(turnContext);
        String cacheKey = appId + (oAuthAppCredentials != null ? oAuthAppCredentials.getAppId() : "");

        OAuthClient client = oAuthClients.computeIfAbsent(cacheKey, key -> {
            sendEmulateOAuthCards.set(OAuthClientConfig.emulateOAuthCards);

            String oAuthScope = getBotFrameworkOAuthScope();
            AppCredentials credentials =
                oAuthAppCredentials != null ? oAuthAppCredentials : getAppCredentials(appId, oAuthScope).join();

            return new RestOAuthClient(
                OAuthClientConfig.emulateOAuthCards
                    ? turnContext.getActivity().getServiceUrl()
                    : OAuthClientConfig.OAUTHENDPOINT,
                credentials
            );
        });

        // adding the oAuthClient into the TurnState
        if (turnContext.getTurnState().get(BotAdapter.OAUTH_CLIENT_KEY) == null) {
            turnContext.getTurnState().add(BotAdapter.OAUTH_CLIENT_KEY, client);
        }

        if (sendEmulateOAuthCards.get()) {
            return client.getUserToken().sendEmulateOAuthCards(true).thenApply(voidresult -> client);
        }

        return CompletableFuture.completedFuture(client);
    }

    /**
     * Creates the connector client asynchronous.
     *
     * @param serviceUrl     The service URL.
     * @param claimsIdentity The claims identity.
     * @param audience       The target audience for the connector.
     * @return ConnectorClient instance.
     * @throws UnsupportedOperationException ClaimsIdentity cannot be null. Pass
     *                                       Anonymous ClaimsIdentity if
     *                                       authentication is turned off.
     */
    @SuppressWarnings(value = "PMD")
    public CompletableFuture<ConnectorClient> createConnectorClient(
        String serviceUrl,
        ClaimsIdentity claimsIdentity,
        String audience
    ) {
        if (claimsIdentity == null) {
            return Async.completeExceptionally(
                new UnsupportedOperationException(
                    "ClaimsIdentity cannot be null. Pass Anonymous ClaimsIdentity if authentication is turned off."
                )
            );
        }

        // For requests from channel App Id is in Audience claim of JWT token. For
        // emulator it is in AppId claim.
        // For unauthenticated requests we have anonymous identity provided auth is
        // disabled.
        if (claimsIdentity.claims() == null) {
            return getOrCreateConnectorClient(serviceUrl);
        }

        // For Activities coming from Emulator AppId claim contains the Bot's AAD AppId.
        // For anonymous requests (requests with no header) appId is not set in claims.

        String botAppIdClaim = claimsIdentity.claims().get(AuthenticationConstants.AUDIENCE_CLAIM);
        if (botAppIdClaim == null) {
            botAppIdClaim = claimsIdentity.claims().get(AuthenticationConstants.APPID_CLAIM);
        }

        if (botAppIdClaim != null) {
            String scope = audience;

            if (StringUtils.isBlank(audience)) {
                scope = SkillValidation.isSkillClaim(claimsIdentity.claims())
                    ? String.format("%s/.default", JwtTokenValidation.getAppIdFromClaims(claimsIdentity.claims()))
                    : getBotFrameworkOAuthScope();
            }

            return getAppCredentials(botAppIdClaim, scope)
                .thenCompose(credentials -> getOrCreateConnectorClient(serviceUrl, credentials));
        }

        return getOrCreateConnectorClient(serviceUrl);
    }

    private CompletableFuture<ConnectorClient> getOrCreateConnectorClient(String serviceUrl) {
        return getOrCreateConnectorClient(serviceUrl, null);
    }

    /**
     * Returns a ConnectorClient, either from a cache or newly created.
     *
     * <p>
     * Note: This is protected primarily to allow unit tests to override this to
     * provide a mock ConnectorClient
     * </p>
     *
     * @param serviceUrl          The service URL for the client.
     * @param usingAppCredentials (Optional) The AppCredentials to use.
     * @return A task that will return the ConnectorClient.
     */
    protected CompletableFuture<ConnectorClient> getOrCreateConnectorClient(
        String serviceUrl,
        AppCredentials usingAppCredentials
    ) {
        CompletableFuture<ConnectorClient> result = new CompletableFuture<>();

        String clientKey = keyForConnectorClient(
            serviceUrl,
            usingAppCredentials != null ? usingAppCredentials.getAppId() : null,
            usingAppCredentials != null ? usingAppCredentials.oAuthScope() : null
        );

        result.complete(connectorClients.computeIfAbsent(clientKey, key -> {
            try {
                RestConnectorClient connectorClient;
                if (usingAppCredentials != null) {
                    connectorClient =
                        new RestConnectorClient(new URI(serviceUrl).toURL().toString(), usingAppCredentials);
                } else {
                    AppCredentials emptyCredentials = channelProvider != null && channelProvider.isGovernment()
                        ? MicrosoftGovernmentAppCredentials.empty()
                        : MicrosoftAppCredentials.empty();
                    connectorClient = new RestConnectorClient(new URI(serviceUrl).toURL().toString(), emptyCredentials);
                }

                if (connectorClientRetryStrategy != null) {
                    connectorClient.setRestRetryStrategy(connectorClientRetryStrategy);
                }

                return connectorClient;
            } catch (Throwable t) {
                result.completeExceptionally(
                    new IllegalArgumentException(String.format("Invalid Service URL: %s", serviceUrl), t)
                );
                return null;
            }
        }));

        return result;
    }

    /**
     * Gets the application credentials. App Credentials are cached so as to ensure
     * we are not refreshing token every time.
     *
     * @param appId The application identifier (AAD Id for the bot).
     * @return App credentials.
     */
    private CompletableFuture<AppCredentials> getAppCredentials(String appId, String scope) {
        if (appId == null) {
            return CompletableFuture.completedFuture(MicrosoftAppCredentials.empty());
        }

        String cacheKey = keyForAppCredentials(appId, scope);
        if (appCredentialMap.containsKey(cacheKey)) {
            return CompletableFuture.completedFuture(appCredentialMap.get(cacheKey));
        }

        // If app credentials were provided, use them as they are the preferred choice
        // moving forward
        if (appCredentials != null) {
            appCredentialMap.put(cacheKey, appCredentials);
            return CompletableFuture.completedFuture(appCredentials);
        }

        // Create a new AppCredentials and add it to the cache.
        return buildAppCredentials(appId, scope).thenApply(credentials -> {
            appCredentialMap.put(cacheKey, credentials);
            return credentials;
        });
    }

    /**
     * Creates an AppCredentials object for the specified appId and scope.
     *
     * @param appId The appId.
     * @param scope The scope.
     * @return An AppCredentials object.
     */
    protected CompletableFuture<AppCredentials> buildAppCredentials(String appId, String scope) {
        return credentialProvider.getAppPassword(appId).thenApply(appPassword -> {
            AppCredentials credentials = channelProvider != null && channelProvider.isGovernment()
                ? new MicrosoftGovernmentAppCredentials(appId, appPassword, null, scope)
                : new MicrosoftAppCredentials(appId, appPassword, null, scope);
            return credentials;
        });
    }

    private String getBotAppId(TurnContext turnContext) throws IllegalStateException {
        ClaimsIdentity botIdentity = turnContext.getTurnState().get(BOT_IDENTITY_KEY);
        if (botIdentity == null) {
            throw new IllegalStateException("An IIdentity is required in TurnState for this operation.");
        }

        String appId = botIdentity.claims().get(AuthenticationConstants.AUDIENCE_CLAIM);
        if (StringUtils.isEmpty(appId)) {
            throw new IllegalStateException("Unable to get the bot AppId from the audience claim.");
        }

        return appId;
    }

    private String getBotFrameworkOAuthScope() {
        return channelProvider != null && channelProvider.isGovernment()
            ? GovernmentAuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE
            : AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE;
    }

    /**
     * Generates the key for accessing the app credentials cache.
     *
     * @param appId The appId
     * @param scope The scope.
     * @return The cache key
     */
    protected static String keyForAppCredentials(String appId, String scope) {
        return appId + (StringUtils.isEmpty(scope) ? "" : scope);
    }

    /**
     * Generates the key for accessing the connector client cache.
     *
     * @param serviceUrl The service url
     * @param appId      The app did
     * @param scope      The scope
     * @return The cache key.
     */
    protected static String keyForConnectorClient(String serviceUrl, String appId, String scope) {
        return serviceUrl + (appId == null ? "" : appId) + (scope == null ? "" : scope);
    }

    /**
     * Middleware to assign tenantId from channelData to Conversation.TenantId.
     *
     * MS Teams currently sends the tenant ID in channelData and the correct
     * behavior is to expose this value in Activity.Conversation.TenantId.
     *
     * This code copies the tenant ID from channelData to
     * Activity.Conversation.TenantId. Once MS Teams sends the tenantId in the
     * Conversation property, this middleware can be removed.
     */
    private static class TenantIdWorkaroundForTeamsMiddleware implements Middleware {
        @Override
        public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
            if (
                StringUtils.equalsIgnoreCase(turnContext.getActivity().getChannelId(), Channels.MSTEAMS)
                    && turnContext.getActivity().getConversation() != null
                    && StringUtils.isEmpty(turnContext.getActivity().getConversation().getTenantId())
            ) {

                ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
                JsonNode teamsChannelData = mapper.valueToTree(turnContext.getActivity().getChannelData());
                if (
                    teamsChannelData != null && teamsChannelData.has("tenant")
                        && teamsChannelData.get("tenant").has("id")
                ) {

                    turnContext.getActivity()
                        .getConversation()
                        .setTenantId(teamsChannelData.get("tenant").get("id").asText());
                }
            }

            return next.next();
        }
    }

    /**
     * Get the AppCredentials cache. For unit testing.
     *
     * @return The AppCredentials cache.
     */
    protected Map<String, AppCredentials> getCredentialsCache() {
        return Collections.unmodifiableMap(appCredentialMap);
    }

    /**
     * Get the ConnectorClient cache. FOR UNIT TESTING.
     *
     * @return The ConnectorClient cache.
     */
    protected Map<String, ConnectorClient> getConnectorClientCache() {
        return Collections.unmodifiableMap(connectorClients);
    }

    /**
     * Attempts to retrieve the token for a user that's in a login flow, using
     * customized AppCredentials.
     *
     * @param context             Context for the current turn of conversation with
     *                            the user.
     * @param oAuthAppCredentials AppCredentials for OAuth.
     * @param connectionName      Name of the auth connection to use.
     * @param magicCode           (Optional) Optional user entered code to validate.
     *
     * @return Token Response.
     */
    @Override
    public CompletableFuture<TokenResponse> getUserToken(
        TurnContext context,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String magicCode
    ) {

        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }

        if (context.getActivity().getFrom() == null || StringUtils.isEmpty(context.getActivity().getFrom().getId())) {
            return Async.completeExceptionally(
                new IllegalArgumentException("BotFrameworkAdapter.GetUserTokenAsync(): missing from or from.id")
            );
        }

        if (StringUtils.isEmpty(connectionName)) {
            return Async.completeExceptionally(new IllegalArgumentException("connectionName cannot be null."));
        }

        return createOAuthAPIClient(context, oAuthAppCredentials).thenCompose(client -> {
            return client.getUserToken()
                .getToken(
                    context.getActivity().getFrom().getId(),
                    connectionName,
                    context.getActivity().getChannelId(),
                    magicCode
                );
        });
    }

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name, using customized AppCredentials.
     *
     * @param context             Context for the current turn of conversation with
     *                            the user.
     * @param oAuthAppCredentials AppCredentials for OAuth.
     * @param connectionName      Name of the auth connection to use.
     *
     * @return A task that represents the work queued to execute.
     *
     *         If the task completes successfully, the result contains the raw
     *         signin link.
     */
    @Override
    public CompletableFuture<String> getOAuthSignInLink(
        TurnContext context,
        AppCredentials oAuthAppCredentials,
        String connectionName
    ) {

        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }

        if (StringUtils.isEmpty(connectionName)) {
            Async.completeExceptionally(new IllegalArgumentException("connectionName cannot be null."));
        }

        return createOAuthAPIClient(context, oAuthAppCredentials).thenCompose(oAuthClient -> {
            try {
                Activity activity = context.getActivity();
                String appId = getBotAppId(context);
                ConversationReference conversationReference = new ConversationReference();
                conversationReference.setActivityId(activity.getId());
                conversationReference.setBot(activity.getRecipient());
                conversationReference.setChannelId(activity.getChannelId());
                conversationReference.setConversation(activity.getConversation());
                conversationReference.setServiceUrl(activity.getServiceUrl());
                conversationReference.setUser(activity.getFrom());

                TokenExchangeState tokenExchangeState = new TokenExchangeState();
                tokenExchangeState.setConnectionName(connectionName);
                tokenExchangeState.setConversation(conversationReference);
                tokenExchangeState.setRelatesTo(activity.getRelatesTo());
                tokenExchangeState.setMsAppId(appId);

                String serializedState = Serialization.toString(tokenExchangeState);
                String state = Base64.getEncoder().encodeToString(serializedState.getBytes(StandardCharsets.UTF_8));

                return oAuthClient.getBotSignIn().getSignInUrl(state);
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        });
    }

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name, using the bot's AppCredentials.
     *
     * @param context        Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @param userId         The user id that will be associated with the token.
     * @param finalRedirect  The final URL that the OAuth flow will redirect to.
     *
     * @return A task that represents the work queued to execute.
     *
     *         If the task completes successfully, the result contains the raw
     *         signin link.
     */
    @Override
    public CompletableFuture<String> getOAuthSignInLink(
        TurnContext context,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String userId,
        String finalRedirect
    ) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }
        if (StringUtils.isEmpty(connectionName)) {
            return Async.completeExceptionally(new IllegalArgumentException("connectionName"));
        }
        if (StringUtils.isEmpty(userId)) {
            return Async.completeExceptionally(new IllegalArgumentException("userId"));
        }

        return createOAuthAPIClient(context, oAuthAppCredentials).thenCompose(oAuthClient -> {
            try {
                Activity activity = context.getActivity();
                String appId = getBotAppId(context);

                ConversationReference conversationReference = new ConversationReference();
                conversationReference.setActivityId(activity.getId());
                conversationReference.setBot(activity.getRecipient());
                conversationReference.setChannelId(activity.getChannelId());
                conversationReference.setConversation(activity.getConversation());
                conversationReference.setLocale(activity.getLocale());
                conversationReference.setServiceUrl(activity.getServiceUrl());
                conversationReference.setUser(activity.getFrom());
                TokenExchangeState tokenExchangeState = new TokenExchangeState();
                tokenExchangeState.setConnectionName(connectionName);
                tokenExchangeState.setConversation(conversationReference);
                tokenExchangeState.setRelatesTo(activity.getRelatesTo());
                tokenExchangeState.setMsAppId(appId);

                String serializedState = Serialization.toString(tokenExchangeState);
                String state = Base64.getEncoder().encodeToString(serializedState.getBytes(StandardCharsets.UTF_8));

                return oAuthClient.getBotSignIn().getSignInUrl(state, null, null, finalRedirect);
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        });
    }

    /**
     * Signs the user out with the token server, using customized AppCredentials.
     *
     * @param context             Context for the current turn of conversation with
     *                            the user.
     * @param oAuthAppCredentials AppCredentials for OAuth.
     * @param connectionName      Name of the auth connection to use.
     * @param userId              User id of user to sign out.
     *
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> signOutUser(
        TurnContext context,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String userId
    ) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }
        if (StringUtils.isEmpty(connectionName)) {
            return Async.completeExceptionally(new IllegalArgumentException("connectionName"));
        }

        return createOAuthAPIClient(context, oAuthAppCredentials).thenCompose(oAuthClient -> {
            return oAuthClient.getUserToken()
                .signOut(context.getActivity().getFrom().getId(), connectionName, context.getActivity().getChannelId());
        }).thenApply(signOutResult -> null);
    }

    /**
     * Retrieves the token status for each configured connection for the given user,
     * using customized AppCredentials.
     *
     * @param context             Context for the current turn of conversation with
     *                            the user.
     * @param oAuthAppCredentials AppCredentials for OAuth.
     * @param userId              The user Id for which token status is retrieved.
     * @param includeFilter       Optional comma separated list of connection's to
     *                            include. Blank will return token status for all
     *                            configured connections.
     *
     * @return List of TokenStatus.
     */
    @Override
    public CompletableFuture<List<TokenStatus>> getTokenStatus(
        TurnContext context,
        AppCredentials oAuthAppCredentials,
        String userId,
        String includeFilter
    ) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }
        if (StringUtils.isEmpty(userId)) {
            return Async.completeExceptionally(new IllegalArgumentException("userId"));
        }

        return createOAuthAPIClient(context, oAuthAppCredentials).thenCompose(oAuthClient -> {
            return oAuthClient.getUserToken()
                .getTokenStatus(userId, context.getActivity().getChannelId(), includeFilter);
        });
    }

    /**
     * Retrieves Azure Active Directory tokens for particular resources on a
     * configured connection, using customized AppCredentials.
     *
     * @param context             Context for the current turn of conversation with
     *                            the user.
     * @param oAuthAppCredentials AppCredentials for OAuth.
     * @param connectionName      The name of the Azure Active Directory connection
     *                            configured with this bot.
     * @param resourceUrls        The list of resource URLs to retrieve tokens for.
     * @param userId              The user Id for which tokens are retrieved. If
     *                            passing in null the userId is taken from the
     *                            Activity in the TurnContext.
     *
     * @return Dictionary of resourceUrl to the corresponding TokenResponse.
     */
    @Override
    public CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        TurnContext context,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String[] resourceUrls,
        String userId
    ) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }
        if (StringUtils.isEmpty(connectionName)) {
            return Async.completeExceptionally(new IllegalArgumentException("connectionName"));
        }
        if (resourceUrls == null) {
            return Async.completeExceptionally(new IllegalArgumentException("resourceUrls"));
        }

        return createOAuthAPIClient(context, oAuthAppCredentials).thenCompose(oAuthClient -> {
            String effectiveUserId = userId;
            if (
                StringUtils.isEmpty(effectiveUserId) && context.getActivity() != null
                    && context.getActivity().getFrom() != null
            ) {
                effectiveUserId = context.getActivity().getFrom().getId();
            }

            return oAuthClient.getUserToken()
                .getAadTokens(effectiveUserId, connectionName, new AadResourceUrls(resourceUrls));
        });

    }

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name.
     *
     * @param turnContext    Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     *
     * @return A task that represents the work queued to execute.
     *
     *         If the task completes successfully, the result contains the raw
     *         signin link.
     */
    @Override
    public CompletableFuture<SignInResource> getSignInResource(TurnContext turnContext, String connectionName) {
        return getSignInResource(turnContext, connectionName, turnContext.getActivity().getFrom().getId(), null);
    }

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name.
     *
     * @param turnContext    Context for the current turn of conversation with the
     *                       user.
     * @param connectionName Name of the auth connection to use.
     * @param userId         The user id that will be associated with the token.
     * @param finalRedirect  The final URL that the OAuth flow will redirect to.
     *
     * @return A task that represents the work queued to execute.
     *
     *         If the task completes successfully, the result contains the raw
     *         signin link.
     */
    @Override
    public CompletableFuture<SignInResource> getSignInResource(
        TurnContext turnContext,
        String connectionName,
        String userId,
        String finalRedirect
    ) {
        return getSignInResource(turnContext, null, connectionName, userId, finalRedirect);
    }

    /**
     * Get the raw signin link to be sent to the user for signin for a connection
     * name.
     *
     * @param context             Context for the current turn of conversation with
     *                            the user.
     * @param oAuthAppCredentials AppCredentials for OAuth.
     * @param connectionName      Name of the auth connection to use.
     * @param userId              The user id that will be associated with the
     *                            token.
     * @param finalRedirect       The final URL that the OAuth flow will redirect
     *                            to.
     *
     * @return A task that represents the work queued to execute.
     *
     *         If the task completes successfully, the result contains the raw
     *         signin link.
     */
    @Override
    public CompletableFuture<SignInResource> getSignInResource(
        TurnContext context,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String userId,
        String finalRedirect
    ) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }

        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName cannot be null.");
        }

        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId cannot be null.");
        }

        return createOAuthAPIClient(context, oAuthAppCredentials).thenCompose(oAuthClient -> {
            try {
                Activity activity = context.getActivity();
                String appId = getBotAppId(context);

                ConversationReference conversationReference = new ConversationReference();
                conversationReference.setActivityId(activity.getId());
                conversationReference.setBot(activity.getRecipient());
                conversationReference.setChannelId(activity.getChannelId());
                conversationReference.setConversation(activity.getConversation());
                conversationReference.setLocale(activity.getLocale());
                conversationReference.setServiceUrl(activity.getServiceUrl());
                conversationReference.setUser(activity.getFrom());
                TokenExchangeState tokenExchangeState = new TokenExchangeState();
                tokenExchangeState.setConnectionName(connectionName);
                tokenExchangeState.setConversation(conversationReference);
                tokenExchangeState.setRelatesTo(activity.getRelatesTo());
                tokenExchangeState.setMsAppId(appId);

                String serializedState = Serialization.toString(tokenExchangeState);
                String state = Base64.getEncoder().encodeToString(serializedState.getBytes(StandardCharsets.UTF_8));

                return oAuthClient.getBotSignIn().getSignInResource(state, null, null, finalRedirect);
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        });

    }

    /**
     * Performs a token exchange operation such as for single sign-on.
     *
     * @param turnContext     Context for the current turn of conversation with the
     *                        user.
     * @param connectionName  Name of the auth connection to use.
     * @param userId          The user id associated with the token..
     * @param exchangeRequest The exchange request details, either a token to
     *                        exchange or a uri to exchange.
     *
     * @return If the task completes, the exchanged token is returned.
     */
    @Override
    public CompletableFuture<TokenResponse> exchangeToken(
        TurnContext turnContext,
        String connectionName,
        String userId,
        TokenExchangeRequest exchangeRequest
    ) {
        return exchangeToken(turnContext, null, connectionName, userId, exchangeRequest);
    }

    /**
     * Performs a token exchange operation such as for single sign-on.
     *
     * @param turnContext         Context for the current turn of conversation with
     *                            the user.
     * @param oAuthAppCredentials AppCredentials for OAuth.
     * @param connectionName      Name of the auth connection to use.
     * @param userId              The user id associated with the token..
     * @param exchangeRequest     The exchange request details, either a token to
     *                            exchange or a uri to exchange.
     *
     * @return If the task completes, the exchanged token is returned.
     */
    @Override
    public CompletableFuture<TokenResponse> exchangeToken(
        TurnContext turnContext,
        AppCredentials oAuthAppCredentials,
        String connectionName,
        String userId,
        TokenExchangeRequest exchangeRequest
    ) {

        if (StringUtils.isEmpty(connectionName)) {
            return Async.completeExceptionally(new IllegalArgumentException("connectionName is null or empty"));
        }

        if (StringUtils.isEmpty(userId)) {
            return Async.completeExceptionally(new IllegalArgumentException("userId is null or empty"));
        }

        if (exchangeRequest == null) {
            return Async.completeExceptionally(new IllegalArgumentException("exchangeRequest is null"));
        }

        if (StringUtils.isEmpty(exchangeRequest.getToken()) && StringUtils.isEmpty(exchangeRequest.getUri())) {
            return Async.completeExceptionally(
                new IllegalArgumentException("Either a Token or Uri property is required on the TokenExchangeRequest")
            );
        }

        return createOAuthAPIClient(turnContext, oAuthAppCredentials).thenCompose(oAuthClient -> {
            return oAuthClient.getUserToken()
                .exchangeToken(userId, connectionName, turnContext.getActivity().getChannelId(), exchangeRequest);

        });
    }

    /**
     * Inserts a ConnectorClient into the cache. FOR UNIT TESTING ONLY.
     *
     * @param serviceUrl The service url
     * @param appId      The app did
     * @param scope      The scope
     * @param client     The ConnectorClient to insert.
     */
    protected void addConnectorClientToCache(String serviceUrl, String appId, String scope, ConnectorClient client) {
        String key = BotFrameworkAdapter.keyForConnectorClient(serviceUrl, appId, scope);
        connectorClients.put(key, client);
    }
}
