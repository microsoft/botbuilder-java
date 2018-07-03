package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.authentication.*;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.*;
import com.microsoft.rest.retry.RetryStrategy;
import org.apache.commons.lang3.StringUtils;
import sun.net.www.http.HttpClient;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * A bot adapter that can connect a bot to a service endpoint.
 *  The bot adapter encapsulates authentication processes and sends
 * activities to and receives activities from the Bot Connector Service. When your
 * bot receives an activity, the adapter creates a context object, passes it to your
 * bot's application logic, and sends responses back to the user's channel.
 * <p>Use {@link Use(IMiddleware)} to add {@link IMiddleware} objects
 * to your adapter’s middleware collection. The adapter processes and directs
 * incoming activities in through the bot middleware pipeline to your bot’s logic
 * and then back out again. As each activity flows in and out of the bot, each piece
 * of middleware can inspect or act upon the activity, both before and after the bot
 * logic runs.</p>
 *
 * {@linkalso TurnContext}
 * {@linkalso IActivity}
 * {@linkalso IBot}
 * {@linkalso IMiddleware}
 */
public class BotFrameworkAdapter extends BotAdapter {
    private final CredentialProvider _credentialProvider;
    //private final HttpClient _httpClient;
    private final RetryStrategy _connectorClientRetryStrategy;
    private Map<String, MicrosoftAppCredentials> _appCredentialMap = new HashMap<String, MicrosoftAppCredentials>();

    private final String InvokeReponseKey = "BotFrameworkAdapter.InvokeResponse";
    private boolean _isEmulatingOAuthCards = false;

    /**
     * Initializes a new instance of the {@link BotFrameworkAdapter} class,
     * using a credential provider.
     * @param credentialProvider The credential provider.
     * @param connectorClientRetryStrategy Retry strategy for retrying HTTP operations.
     * @param httpClient The HTTP client.
     * @param middleware The middleware to initially add to the adapter.
     * @throws IllegalArgumentException 
     * {@code credentialProvider} is {@code null}.
     *  Use a {@link MiddlewareSet} object to add multiple middleware
     * components in the conustructor. Use the {@link Use(IMiddleware)} method to
     * add additional middleware to the adapter after construction.
     *
     */
    public BotFrameworkAdapter(CredentialProvider credentialProvider) {
        this(credentialProvider, null, null, null);
    }

    public BotFrameworkAdapter(CredentialProvider credentialProvider, RetryStrategy connectorClientRetryStrategy) {
        this(credentialProvider, connectorClientRetryStrategy, null, null);
    }

    public BotFrameworkAdapter(CredentialProvider credentialProvider, RetryStrategy connectorClientRetryStrategy, HttpClient httpClient) {
        this(credentialProvider, connectorClientRetryStrategy, httpClient, null);
    }

    public BotFrameworkAdapter(CredentialProvider credentialProvider, RetryStrategy connectorClientRetryStrategy, HttpClient httpClient, Middleware middleware) {
        if (credentialProvider == null)
            throw new IllegalArgumentException("credentialProvider");
        _credentialProvider = credentialProvider;
        //_httpClient = httpClient ?? new HttpClient();
        _connectorClientRetryStrategy = connectorClientRetryStrategy;

        if (middleware != null) {
            this.Use(middleware);
        }
    }

    /**
     * Sends a proactive message from the bot to a conversation.
     * @param botAppId The application ID of the bot. This is the appId returned by Portal registration, and is
     * generally found in the "MicrosoftAppId" parameter in appSettings.json.
     * @param reference A reference to the conversation to continue.
     * @param callback The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute.
     * @throws IllegalArgumentException 
     * {@code botAppId}, {@code reference}, or
     * {@code callback} is {@code null}.
     *  Call this method to proactively send a message to a conversation.
     * Most channels require a user to initaiate a conversation with a bot
     * before the bot can send activities to the user.
     * <p>This method registers the following.services().for the turn.<list type="bullet 
     * <item>{@link IIdentity} (key = "BotIdentity"), a claims identity for the bot.</item>
     * <item>{@link ConnectorClient}, the channel connector client to use this turn.</item>
     * </list></p>
     * <p>
     * This overload differers from the Node implementation by requiring the BotId to be
     * passed in. The .Net code allows multiple bots to be hosted in a single adapter which
     * isn't something supported by Node.
     * </p>
     *
     * {@linkalso ProcessActivity(String, Activity, Func{TurnContext, Task})}
     * {@linkalso BotAdapter.RunPipeline(TurnContext, Func{TurnContext, Task}}
     */
    @Override
    public CompletableFuture ContinueConversation(String botAppId, ConversationReference reference, Function<TurnContext, CompletableFuture> callback) throws Exception, ServiceKeyAlreadyRegisteredException {
        if (StringUtils.isEmpty(botAppId))
            throw new IllegalArgumentException("botAppId");

        if (reference == null)
            throw new IllegalArgumentException("reference");

        if (callback == null)
            throw new IllegalArgumentException("callback");

        try (TurnContextImpl context = new TurnContextImpl(this, new ConversationReferenceHelper(reference).GetPostToBotMessage())) {
            // Hand craft Claims Identity.
//            Claim[] claims = {
//                    new Claim(AuthenticationConstants.AudienceClaim, botAppId), new Claim(AuthenticationConstants.AppIdClaim, botAppId)};
//            ClaimsHolder claimsIdentity = new ClaimsHolder(new LinkedList<Claim>(claims));
//
//            context.getServices().Add("BotIdentity", claimsIdentity);
            //ConnectorClient connectorClient = await(this.CreateConnectorClientAsync(reference.serviceUrl(), claimsIdentity));
//            context.getServices().Add("ConnectorClient", connectorClient);
            //await(RunPipeline (context, callback));
        }
        return completedFuture(null);
    }

    /**
     * Initializes a new instance of the {@link BotFrameworkAdapter} class,
     * using an application ID and secret.
     * @param appId The application ID of the bot.
     * @param appPassword The application secret for the bot.
     * @param connectorClientRetryStrategy Retry policy for retrying HTTP operations.
     * @param httpClient The HTTP client.
     * @param middleware The middleware to initially add to the adapter.
     *  Use a {@link MiddlewareSet} object to add multiple middleware
     * components in the conustructor. Use the {@link Use(IMiddleware)} method to
     * add additional middleware to the adapter after construction.
     *
     */
    public BotFrameworkAdapter(String appId, String appPassword) {
        this(appId, appPassword, null, null, null);
    }

    public BotFrameworkAdapter(String appId, String appPassword, RetryStrategy connectorClientRetryStrategy) {
        this(appId, appPassword, connectorClientRetryStrategy, null, null);
    }

    public BotFrameworkAdapter(String appId, String appPassword, RetryStrategy connectorClientRetryStrategy, HttpClient httpClient) {
        this(appId, appPassword, connectorClientRetryStrategy, httpClient, null);
    }

    public BotFrameworkAdapter(String appId, String appPassword, RetryStrategy connectorClientRetryStrategy, HttpClient httpClient, Middleware middleware) {
        this(new SimpleCredentialProvider(appId, appPassword), connectorClientRetryStrategy, httpClient, middleware);
    }

    /**
     * Adds middleware to the adapter's pipeline.
     * @param middleware The middleware to add.
     * @return The updated adapter object.
     *  Middleware is added to the adapter at initialization time.
     * For each turn, the adapter calls middleware in the order in which you added it.
     *
     */

    public BotFrameworkAdapter Use(Middleware middleware) {
        super._middlewareSet.Use(middleware);
        return this;
    }

    /**
     * Creates a turn context and runs the middleware pipeline for an incoming activity.
     * @param authHeader The HTTP authentication header of the request.
     * @param activity The incoming activity.
     * @param callback The code to run at the end of the adapter's middleware
     * pipeline.
     * @return A task that represents the work queued to execute. If the activity type
     * was 'Invoke' and the corresponding key (channelId + activityId) was found
     * then an InvokeResponse is returned, otherwise null is returned.
     * @throws IllegalArgumentException 
     * {@code activity} is {@code null}.
     * @throws UnauthorizedAccessException 
     * authentication failed.
     *  Call this method to reactively send a message to a conversation.
     * <p>This method registers the following.services().for the turn.<list type="bullet 
     * <item>{@link IIdentity} (key = "BotIdentity"), a claims identity for the bot.</item>
     * <item>{@link ConnectorClient}, the channel connector client to use this turn.</item>
     * </list></p>
     *
     * {@linkalso ContinueConversation(String, ConversationReference, Func{TurnContext, Task})}
     * {@linkalso BotAdapter.RunPipeline(TurnContext, Func{TurnContext, Task})}
     */
    public CompletableFuture<InvokeResponse> ProcessActivity(String authHeader, ActivityImpl activity, Function<TurnContext, CompletableFuture> callback) throws ServiceKeyAlreadyRegisteredException, Exception {
        BotAssert.ActivityNotNull(activity);

        //ClaimsIdentity claimsIdentity = await(JwtTokenValidation.validateAuthHeader(activity, authHeader, _credentialProvider));

        //return completedFuture(await(ProcessActivity(claimsIdentity, activity, callback)));
        return completedFuture(null);
    }

    public CompletableFuture<InvokeResponse> ProcessActivity(ClaimsIdentity identity, ActivityImpl activity, Function<TurnContext, CompletableFuture> callback) throws Exception, ServiceKeyAlreadyRegisteredException {
        BotAssert.ActivityNotNull(activity);

        try (TurnContextImpl context = new TurnContextImpl(this, activity)) {
            context.getServices().Add("BotIdentity", identity);

            ConnectorClient connectorClient = await(this.CreateConnectorClientAsync(activity.serviceUrl(), identity));
            // TODO: Verify key that C# uses
            context.getServices().Add("ConnectorClient", connectorClient);

            await(super.RunPipeline(context, callback));

            // Handle Invoke scenarios, which deviate from the request/response model in that
            // the Bot will return a specific body and return code.
            if (activity.type() == ActivityTypes.INVOKE) {
                Activity invokeResponse = context.getServices().Get(InvokeReponseKey);
                if (invokeResponse == null) {
                    // ToDo: Trace Here
                    throw new IllegalStateException("Bot failed to return a valid 'invokeResponse' activity.");
                } else {
                    return completedFuture((InvokeResponse) invokeResponse.value());
                }
            }

            // For all non-invoke scenarios, the HTTP layers above don't have to mess
            // withthe Body and return codes.
            return null;
        }
    }

    /**
     * Sends activities to the conversation.
     * @param context The context object for the turn.
     * @param activities The activities to send.
     * @return A task that represents the work queued to execute.
     *  If the activities are successfully sent, the task result contains
     * an array of {@link ResourceResponse} objects containing the IDs that
     * the receiving channel assigned to the activities.
     * {@linkalso TurnContext.OnSendActivities(SendActivitiesHandler)}
     */
    public CompletableFuture<ResourceResponse[]> SendActivities(TurnContext context, Activity[] activities) throws ServiceKeyAlreadyRegisteredException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context");
        }

        if (activities == null) {
            throw new IllegalArgumentException("activities");
        }

        if (activities.length == 0) {
            throw new IllegalArgumentException("Expecting one or more activities, but the array was empty.");
        }

        ResourceResponse[] responses = new ResourceResponse[activities.length];

        /*
         * NOTE: we're using for here (vs. foreach) because we want to simultaneously index into the
         * activities array to get the activity to process as well as use that index to assign
         * the response to the responses array and this is the most cost effective way to do that.
         */
        for (int index = 0; index < activities.length; index++) {
            Activity activity = activities[index];
            ResourceResponse response = new ResourceResponse();

            if (activity.type().toString().equals("delay")) {
                // The Activity Schema doesn't have a delay type build in, so it's simulated
                // here in the Bot. This matches the behavior in the Node connector.
                int delayMs = (int) activity.value();
                Thread.sleep(delayMs);
                //await(Task.Delay(delayMs));
                // No need to create a response. One will be created below.
            } else if (activity.type().toString().equals("invokeResponse")) // Aligning name with Node
            {
                context.getServices().Add(InvokeReponseKey, activity);
                // No need to create a response. One will be created below.
            } else if (activity.type() == ActivityTypes.TRACE && !activity.channelId().equals("emulator")) {
                // if it is a Trace activity we only send to the channel if it's the emulator.
            } else if (!StringUtils.isEmpty(activity.replyToId())) {
                ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
                // TODO
                //response = await(connectorClient.conversations().ReplyToActivityAsync(activity.conversation().id(), activity.id(), activity));
            } else {
                ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
                // TODO
                //response = Async.await(connectorClient.conversations().SendToConversationAsync(activity.conversation().id(), activity));
            }

            // If No response is set, then defult to a "simple" response. This can't really be done
            // above, as there are cases where the ReplyTo/SendTo methods will also return null
            // (See below) so the check has to happen here.

            // Note: In addition to the Invoke / Delay / Activity cases, this code also applies
            // with Skype and Teams with regards to typing events.  When sending a typing event in
            // these channels they do not return a RequestResponse which causes the bot to blow up.
            // https://github.com/Microsoft/botbuilder-dotnet/issues/460
            // bug report : https://github.com/Microsoft/botbuilder-dotnet/issues/465
            if (response == null) {
                response = new ResourceResponse().withId((activity.id() == null) ? "" : activity.id());
            }

            responses[index] = response;
        }

        return completedFuture(responses);
    }

    /**
     * Replaces an existing activity in the conversation.
     * @param context The context object for the turn.
     * @param activity New replacement activity.
     * @return A task that represents the work queued to execute.
     *  If the activity is successfully sent, the task result contains
     * a {@link ResourceResponse} object containing the ID that the receiving
     * channel assigned to the activity.
     * <p>Before calling this, set the ID of the replacement activity to the ID
     * of the activity to replace.</p>
     * {@linkalso TurnContext.OnUpdateActivity(UpdateActivityHandler)}
     */
    @Override
    public CompletableFuture<ResourceResponse> UpdateActivity(TurnContext context, Activity activity) {
        ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
        // TODO
        //return await(connectorClient.conversations().updateActivityAsync(activity));
        return completedFuture(null);
    }

    /**
     * Deletes an existing activity in the conversation.
     * @param context The context object for the turn.
     * @param reference Conversation reference for the activity to delete.
     * @return A task that represents the work queued to execute.
     *  The {@link ConversationReference.ActivityId} of the conversation
     * reference identifies the activity to delete.
     * {@linkalso TurnContext.OnDeleteActivity(DeleteActivityHandler)}
     */
    public CompletableFuture DeleteActivity(TurnContext context, ConversationReference reference) {
        ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
        // TODO
        //await(connectorClient.conversations().DeleteActivityAsync(reference.conversation().id(), reference.activityId()));
        return completedFuture(null);
    }

    /**
     * Deletes a member from the current conversation
     * @param context The context object for the turn.
     * @param memberId ID of the member to delete from the conversation
     * @return 
     */
    public CompletableFuture DeleteConversationMember(TurnContext context, String memberId) {
        if (context.getActivity().conversation() == null)
            throw new IllegalArgumentException("BotFrameworkAdapter.deleteConversationMember(): missing conversation");

        if (StringUtils.isEmpty(context.getActivity().conversation().id()))
            throw new IllegalArgumentException("BotFrameworkAdapter.deleteConversationMember(): missing conversation.id");

        ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");

        String conversationId = context.getActivity().conversation().id();

        // TODO:
        //await (connectorClient.conversations().DeleteConversationMemberAsync(conversationId, memberId));
        return completedFuture(null);
    }

    /**
     * Lists the members of a given activity.
     * @param context The context object for the turn.
     * @param activityId (Optional) Activity ID to enumerate. If not specified the current activities ID will be used.
     * @return List of Members of the activity
     */
    public CompletableFuture<List<ChannelAccount>> GetActivityMembers(TurnContext context) {
        return GetActivityMembers(context, null);
    }

    public CompletableFuture<List<ChannelAccount>> GetActivityMembers(TurnContext context, String activityId) {
        // If no activity was passed in, use the current activity.
        if (activityId == null)
            activityId = context.getActivity().id();

        if (context.getActivity().conversation() == null)
            throw new IllegalArgumentException("BotFrameworkAdapter.GetActivityMembers(): missing conversation");

        if (StringUtils.isEmpty((context.getActivity().conversation().id())))
            throw new IllegalArgumentException("BotFrameworkAdapter.GetActivityMembers(): missing conversation.id");

        ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
        String conversationId = context.getActivity().conversation().id();

        // TODO:
        //List<ChannelAccount> accounts = await(connectorClient.conversations().GetActivityMembersAsync(conversationId, activityId));

        return completedFuture(null);
    }

    /**
     * Lists the members of the current conversation.
     * @param context The context object for the turn.
     * @return List of Members of the current conversation
     */
    public CompletableFuture<List<ChannelAccount>> GetConversationMembers(TurnContext context) {
        if (context.getActivity().conversation() == null)
            throw new IllegalArgumentException("BotFrameworkAdapter.GetActivityMembers(): missing conversation");

        if (StringUtils.isEmpty(context.getActivity().conversation().id()))
            throw new IllegalArgumentException("BotFrameworkAdapter.GetActivityMembers(): missing conversation.id");

        ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
        String conversationId = context.getActivity().conversation().id();

        // TODO
        //List<ChannelAccount> accounts = await(connectorClient.conversations().getConversationMembersAsync(conversationId));
        return completedFuture(null);
    }

    /**
     * Lists the Conversations in which this bot has participated for a given channel server. The
     * channel server returns results in pages and each page will include a `continuationToken`
     * that can be used to fetch the next page of results from the server.
     * @param serviceUrl The URL of the channel server to query.  This can be retrieved
     * from `context.activity.serviceUrl`. 
     * @param credentials The credentials needed for the Bot to connect to the.services().
     * @param continuationToken (Optional) token used to fetch the next page of results
     * from the channel server. This should be left as `null` to retrieve the first page
     * of results.
     * @return List of Members of the current conversation
     *
     * This overload may be called from outside the context of a conversation, as only the
     * Bot's ServiceUrl and credentials are required.
     *
     */
    public CompletableFuture<ConversationsResult> GetConversations(String serviceUrl, MicrosoftAppCredentials credentials) throws MalformedURLException, URISyntaxException {
        return GetConversations(serviceUrl, credentials, null);
    }

    public CompletableFuture<ConversationsResult> GetConversations(String serviceUrl, MicrosoftAppCredentials credentials, String continuationToken) throws MalformedURLException, URISyntaxException {
        if (StringUtils.isEmpty(serviceUrl))
            throw new IllegalArgumentException("serviceUrl");

        if (credentials == null)
            throw new IllegalArgumentException("credentials");

        ConnectorClient connectorClient = this.CreateConnectorClient(serviceUrl, credentials);
        // TODO
        //ConversationsResult results = await(connectorClient.conversations().getConversationsAsync(continuationToken));
        return completedFuture(null);
    }

    /**
     * Lists the Conversations in which this bot has participated for a given channel server. The
     * channel server returns results in pages and each page will include a `continuationToken`
     * that can be used to fetch the next page of results from the server.
     * @param context The context object for the turn.
     * @param continuationToken (Optional) token used to fetch the next page of results
     * from the channel server. This should be left as `null` to retrieve the first page
     * of results.
     * @return List of Members of the current conversation
     *
     * This overload may be called during standard Activity processing, at which point the Bot's
     * service URL and credentials that are part of the current activity processing pipeline
     * will be used.
     *
     */
    public CompletableFuture<ConversationsResult> GetConversations(TurnContext context) {
        return GetConversations(context, null);
    }

    public CompletableFuture<ConversationsResult> GetConversations(TurnContext context, String continuationToken) {
        ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
        // TODO
        //ConversationsResult results = await(connectorClient.conversations().getConversationsAsync());
        return completedFuture(null);
    }


    /**
     * Attempts to retrieve the token for a user that's in a login flow.
     * @param context Context for the current turn of conversation with the user.
     * @param connectionName Name of the auth connection to use.
     * @param magicCode (Optional) Optional user entered code to validate.
     * @return Token Response
     */
    public CompletableFuture<TokenResponse> GetUserToken(TurnContext context, String connectionName, String magicCode) {
        BotAssert.ContextNotNull(context);
        if (context.getActivity().from() == null || StringUtils.isEmpty(context.getActivity().from().id()))
            throw new IllegalArgumentException("BotFrameworkAdapter.GetuserToken(): missing from or from.id");

        if (StringUtils.isEmpty(connectionName))
            throw new IllegalArgumentException("connectionName");

        //OAuthClient client = this.CreateOAuthApiClient(context);
        //return await(client.GetUserTokenAsync(context.getActivity().from().id(), connectionName, magicCode));
        return completedFuture(null);
    }

    /**
     * Get the raw signin link to be sent to the user for signin for a connection name.
     * @param context Context for the current turn of conversation with the user.
     * @param connectionName Name of the auth connection to use.
     * @return 
     */
    public CompletableFuture<String> GetOauthSignInLink(TurnContext context, String connectionName) {
        BotAssert.ContextNotNull(context);
        if (StringUtils.isEmpty(connectionName))
            throw new IllegalArgumentException("connectionName");

        //OAuthClient client = this.CreateOAuthApiClient(context);
        //return await(client.GetSignInLinkAsync(context.getActivity(), connectionName));
        return completedFuture(null);
    }

    /**
     * Signs the user out with the token server.
     * @param context Context for the current turn of conversation with the user.
     * @param connectionName Name of the auth connection to use.
     * @return 
     */
    public CompletableFuture SignOutUser(TurnContext context, String connectionName) {
        BotAssert.ContextNotNull(context);
        if (StringUtils.isEmpty(connectionName))
            throw new IllegalArgumentException("connectionName");

        //OAuthClient client = this.CreateOAuthApiClient(context);
        //await(client.SignOutUserAsync(context.Activity.From.Id, connectionName));
        return completedFuture(null);
    }

    /**
     * Creates a conversation on the specified channel.
     * @param channelId The ID for the channel.
     * @param serviceUrl The channel's service URL endpoint.
     * @param credentials The application credentials for the bot.
     * @param conversationParameters The conversation information to use to
     * create the conversation.
     * @param callback The method to call for the resulting bot turn.
     * @return A task that represents the work queued to execute.
     *  To start a conversation, your bot must know its account information
     * and the user's account information on that channel.
     * Most channels only support initiating a direct message (non-group) conversation.
     * <p>The adapter attempts to create a new conversation on the channel, and
     * then sends a {@code conversationUpdate} activity through its middleware pipeline
     * to the {@code callback} method.</p>
     * <p>If the conversation is established with the
     * specified users, the ID of the activity's {@link IActivity.Conversation}
     * will contain the ID of the new conversation.</p>
     *
     */
    public CompletableFuture CreateConversation(String channelId, String serviceUrl, MicrosoftAppCredentials
            credentials, ConversationParameters conversationParameters, Function<TurnContext, CompletableFuture> callback) throws Exception, ServiceKeyAlreadyRegisteredException {
        ConnectorClient connectorClient = this.CreateConnectorClient(serviceUrl, credentials);

        // TODO
        //ConversationResourceResponse result = await(connectorClient.conversations().CreateConversationAsync(conversationParameters));

        // Create a conversation update activity to represent the result.

//            ConversationUpdateActivity conversationUpdate = (ConversationUpdateActivity) MessageActivity.CreateConversationUpdateActivity()
//                                    .withChannelId(channelId)
//                                    .withTopicName(conversationParameters.topicName())
//                                    .withServiceUrl(serviceUrl)
//                                    .withMembersAdded(conversationParameters.members())
//                                    .withId((result.activityId() != null) ? result.activityId() : UUID.randomUUID().toString())
//                                    .withConversation(new ConversationAccount().withId(result.id()))
//                                    .withRecipient(conversationParameters.bot());
//
//            try (TurnContextImpl context = new TurnContextImpl(this, (Activity) conversationUpdate))
//            {
//                await( this.RunPipeline(context, callback));
//            }
        return completedFuture(null);
    }

    protected CompletableFuture<Boolean> TrySetEmulatingOAuthCards(TurnContext turnContext) {
        if (!_isEmulatingOAuthCards &&
                turnContext.getActivity().channelId().equals("emulator") &&
                (await(_credentialProvider.isAuthenticationDisabledAsync()))) {
            _isEmulatingOAuthCards = true;
        }
        return completedFuture(_isEmulatingOAuthCards);

    }

    protected OAuthClient CreateOAuthApiClient(TurnContext context) throws MalformedURLException, URISyntaxException {
        ConnectorClientImpl client = context.getServices().Get("ConnectorClient");
        if (client == null) {
            throw new IllegalArgumentException("CreateOAuthApiClient: OAuth requires a valid ConnectorClient instance");
        }
        if (_isEmulatingOAuthCards) {
            return new OAuthClient(client, context.getActivity().serviceUrl());
        }
        return new OAuthClient(client, AuthenticationConstants.OAuthUrl);
    }

    /**
     * Creates the connector client asynchronous.
     * @param serviceUrl The service URL.
     * @param claimsIdentity The claims identity.
     * @return ConnectorClient instance.
     * @throws UnsupportedOperationException ClaimsIdemtity cannot be null. Pass Anonymous ClaimsIdentity if authentication is turned off.
     */
    private CompletableFuture<ConnectorClient> CreateConnectorClientAsync(String serviceUrl, ClaimsIdentity claimsIdentity) {
        if (claimsIdentity == null) {
            throw new UnsupportedOperationException("ClaimsIdemtity cannot be null. Pass Anonymous ClaimsIdentity if authentication is turned off.");
        }

        // For requests from channel App Id is in Audience claim of JWT token. For emulator it is in AppId claim. For
        // unauthenticated requests we have anonymouse identity provided auth is disabled.
//            var botAppIdClaim = (claimsIdentity.Claims ?.SingleOrDefault(claim = > claim.Type == AuthenticationConstants.AudienceClaim) ?? claimsIdentity.Claims ?.SingleOrDefault(claim = > claim.Type == AuthenticationConstants.AppIdClaim));
//          /**
//           */ For Activities coming from Emulator AppId claim contains the Bot's AAD AppId.
//           */
//
//
//          /**
//           */ For anonymous requests (requests with no header) appId is not set in claims.
//           */
//            if (botAppIdClaim != null) {
//                String botId = botAppIdClaim.Value;
//                var appCredentials = await(this.GetAppCredentialsAsync(botId));
//                return this.CreateConnectorClient(serviceUrl, appCredentials);
//            } else {
//                return this.CreateConnectorClient(serviceUrl);
//            }
        return completedFuture(null);
    }

    /**
     * Creates the connector client.
     * @param serviceUrl The service URL.
     * @param appCredentials The application credentials for the bot.
     * @return Connector client instance.
     */
    private ConnectorClient CreateConnectorClient(String serviceUrl) throws MalformedURLException, URISyntaxException {
        return CreateConnectorClient(serviceUrl, null);
    }

    private ConnectorClient CreateConnectorClient(String serviceUrl, MicrosoftAppCredentials appCredentials) throws MalformedURLException, URISyntaxException {
        ConnectorClient connectorClient;
        if (appCredentials != null) {
            connectorClient = new ConnectorClientImpl(new URI(serviceUrl).toURL().toString(), appCredentials);
        }
//            else {
//
//                connectorClient = new ConnectorClientImpl(new URI(serviceUrl).toURL().toString());
//            }
//
//            if (this._connectorClientRetryStrategy != null) {
//                connectorClient.reetRetryStrategy(this._connectorClientRetryStrategy);
//            }

//            return connectorClient;
        return null;
    }

    /**
     * Gets the application credentials. App Credentials are cached so as to ensure we are not refreshing
     * token everytime.
     * @param appId The application identifier (AAD Id for the bot).
     * @return App credentials.
     */
    private CompletableFuture<MicrosoftAppCredentials> GetAppCredentialsAsync(String appId) {
        if (appId == null) {
            return completedFuture(MicrosoftAppCredentials.Empty);
        }

        // TODO
//            if (!_appCredentialMap.TryGetValue(appId, out var appCredentials)) {
//                String appPassword = await(_credentialProvider.GetAppPasswordAsync(appId));
//                appCredentials = new MicrosoftAppCredentials(appId, appPassword);
//                _appCredentialMap[appId] = appCredentials;
//            }

        //return appCredentials;
        return completedFuture(null);
    }

}
