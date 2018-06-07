package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.auth0.jwt.impl.ClaimsHolder;
import com.auth0.jwt.interfaces.Claim;

import com.ea.async.Async;
import com.microsoft.bot.builder.core.*;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.Conversations;
import com.microsoft.bot.connector.authentication.*;

import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.*;
import com.microsoft.rest.retry.RetryStrategy;
import com.sun.jndi.toolkit.url.Uri;
import org.apache.commons.lang3.StringUtils;
import sun.net.www.http.HttpClient;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

/// <summary>
/// A bot adapter that can connect a bot to a service endpoint.
/// </summary>
/// <remarks>The bot adapter encapsulates authentication processes and sends
/// activities to and receives activities from the Bot Connector Service. When your
/// bot receives an activity, the adapter creates a context object, passes it to your
/// bot's application logic, and sends responses back to the user's channel.
/// <para>Use <see cref="Use(IMiddleware)"/> to add <see cref="IMiddleware"/> objects
/// to your adapter’s middleware collection. The adapter processes and directs
/// incoming activities in through the bot middleware pipeline to your bot’s logic
/// and then back out again. As each activity flows in and out of the bot, each piece
/// of middleware can inspect or act upon the activity, both before and after the bot
/// logic runs.</para>
/// </remarks>
/// <seealso cref="TurnContext"/>
/// <seealso cref="IActivity"/>
/// <seealso cref="IBot"/>
/// <seealso cref="IMiddleware"/>
public class BotFrameworkAdapter extends BotAdapter {
    private final CredentialProvider _credentialProvider;
    //private final HttpClient _httpClient;
    private final RetryStrategy _connectorClientRetryStrategy;
    private Map<String, MicrosoftAppCredentials> _appCredentialMap = new HashMap<String, MicrosoftAppCredentials>();

    private final String InvokeReponseKey = "BotFrameworkAdapter.InvokeResponse";
    private boolean _isEmulatingOAuthCards = false;

    /// <summary>
    /// Initializes a new instance of the <see cref="BotFrameworkAdapter"/> class,
    /// using a credential provider.
    /// </summary>
    /// <param name="credentialProvider">The credential provider.</param>
    /// <param name="connectorClientRetryStrategy">Retry strategy for retrying HTTP operations.</param>
    /// <param name="httpClient">The HTTP client.</param>
    /// <param name="middleware">The middleware to initially add to the adapter.</param>
    /// <exception cref="IllegalArgumentException">
    /// <paramref name="credentialProvider"/> is <c>null</c>.</exception>
    /// <remarks>Use a <see cref="MiddlewareSet"/> object to add multiple middleware
    /// components in the conustructor. Use the <see cref="Use(IMiddleware)"/> method to
    /// add additional middleware to the adapter after construction.
    /// </remarks>
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

    /// <summary>
    /// Sends a proactive message from the bot to a conversation.
    /// </summary>
    /// <param name="botAppId">The application ID of the bot. This is the appId returned by Portal registration, and is
    /// generally found in the "MicrosoftAppId" parameter in appSettings.json.</param>
    /// <param name="reference">A reference to the conversation to continue.</param>
    /// <param name="callback">The method to call for the resulting bot turn.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <exception cref="IllegalArgumentException">
    /// <paramref name="botAppId"/>, <paramref name="reference"/>, or
    /// <paramref name="callback"/> is <c>null</c>.</exception>
    /// <remarks>Call this method to proactively send a message to a conversation.
    /// Most channels require a user to initaiate a conversation with a bot
    /// before the bot can send activities to the user.
    /// <para>This method registers the following.services().for the turn.<list type="bullet">
    /// <item><see cref="IIdentity"/> (key = "BotIdentity"), a claims identity for the bot.</item>
    /// <item><see cref="ConnectorClient"/>, the channel connector client to use this turn.</item>
    /// </list></para>
    /// <para>
    /// This overload differers from the Node implementation by requiring the BotId to be
    /// passed in. The .Net code allows multiple bots to be hosted in a single adapter which
    /// isn't something supported by Node.
    /// </para>
    /// </remarks>
    /// <seealso cref="ProcessActivity(String, Activity, Func{TurnContext, Task})"/>
    /// <seealso cref="BotAdapter.RunPipeline(TurnContext, Func{TurnContext, Task}, System.Threading.CancellationTokenSource)"/>
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

    /// <summary>
    /// Initializes a new instance of the <see cref="BotFrameworkAdapter"/> class,
    /// using an application ID and secret.
    /// </summary>
    /// <param name="appId">The application ID of the bot.</param>
    /// <param name="appPassword">The application secret for the bot.</param>
    /// <param name="connectorClientRetryStrategy">Retry policy for retrying HTTP operations.</param>
    /// <param name="httpClient">The HTTP client.</param>
    /// <param name="middleware">The middleware to initially add to the adapter.</param>
    /// <remarks>Use a <see cref="MiddlewareSet"/> object to add multiple middleware
    /// components in the conustructor. Use the <see cref="Use(IMiddleware)"/> method to
    /// add additional middleware to the adapter after construction.
    /// </remarks>
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

    /// <summary>
    /// Adds middleware to the adapter's pipeline.
    /// </summary>
    /// <param name="middleware">The middleware to add.</param>
    /// <returns>The updated adapter object.</returns>
    /// <remarks>Middleware is added to the adapter at initialization time.
    /// For each turn, the adapter calls middleware in the order in which you added it.
    /// </remarks>

    public BotFrameworkAdapter Use(Middleware middleware) {
        super._middlewareSet.Use(middleware);
        return this;
    }

    /// <summary>
    /// Creates a turn context and runs the middleware pipeline for an incoming activity.
    /// </summary>
    /// <param name="authHeader">The HTTP authentication header of the request.</param>
    /// <param name="activity">The incoming activity.</param>
    /// <param name="callback">The code to run at the end of the adapter's middleware
    /// pipeline.</param>
    /// <returns>A task that represents the work queued to execute. If the activity type
    /// was 'Invoke' and the corresponding key (channelId + activityId) was found
    /// then an InvokeResponse is returned, otherwise null is returned.</returns>
    /// <exception cref="IllegalArgumentException">
    /// <paramref name="activity"/> is <c>null</c>.</exception>
    /// <exception cref="UnauthorizedAccessException">
    /// authentication failed.</exception>
    /// <remarks>Call this method to reactively send a message to a conversation.
    /// <para>This method registers the following.services().for the turn.<list type="bullet">
    /// <item><see cref="IIdentity"/> (key = "BotIdentity"), a claims identity for the bot.</item>
    /// <item><see cref="ConnectorClient"/>, the channel connector client to use this turn.</item>
    /// </list></para>
    /// </remarks>
    /// <seealso cref="ContinueConversation(String, ConversationReference, Func{TurnContext, Task})"/>
    /// <seealso cref="BotAdapter.RunPipeline(TurnContext, Func{TurnContext, Task}, System.Threading.CancellationTokenSource)"/>
    public CompletableFuture<InvokeResponse> ProcessActivity(String authHeader, Activity activity, Function<TurnContext, CompletableFuture> callback) throws ServiceKeyAlreadyRegisteredException, Exception {
        BotAssert.ActivityNotNull(activity);

        //ClaimsIdentity claimsIdentity = await(JwtTokenValidation.validateAuthHeader(activity, authHeader, _credentialProvider));

        //return completedFuture(await(ProcessActivity(claimsIdentity, activity, callback)));
        return completedFuture(null);
    }

    public CompletableFuture<InvokeResponse> ProcessActivity(ClaimsIdentity identity, Activity activity, Function<TurnContext, CompletableFuture> callback) throws Exception, ServiceKeyAlreadyRegisteredException {
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

    /// <summary>
    /// Sends activities to the conversation.
    /// </summary>
    /// <param name="context">The context object for the turn.</param>
    /// <param name="activities">The activities to send.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>If the activities are successfully sent, the task result contains
    /// an array of <see cref="ResourceResponse"/> objects containing the IDs that
    /// the receiving channel assigned to the activities.</remarks>
    /// <seealso cref="TurnContext.OnSendActivities(SendActivitiesHandler)"/>
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

    /// <summary>
    /// Replaces an existing activity in the conversation.
    /// </summary>
    /// <param name="context">The context object for the turn.</param>
    /// <param name="activity">New replacement activity.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>If the activity is successfully sent, the task result contains
    /// a <see cref="ResourceResponse"/> object containing the ID that the receiving
    /// channel assigned to the activity.
    /// <para>Before calling this, set the ID of the replacement activity to the ID
    /// of the activity to replace.</para></remarks>
    /// <seealso cref="TurnContext.OnUpdateActivity(UpdateActivityHandler)"/>
    @Override
    public CompletableFuture<ResourceResponse> UpdateActivity(TurnContext context, Activity activity) {
        ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
        // TODO
        //return await(connectorClient.conversations().updateActivityAsync(activity));
        return completedFuture(null);
    }

    /// <summary>
    /// Deletes an existing activity in the conversation.
    /// </summary>
    /// <param name="context">The context object for the turn.</param>
    /// <param name="reference">Conversation reference for the activity to delete.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>The <see cref="ConversationReference.ActivityId"/> of the conversation
    /// reference identifies the activity to delete.</remarks>
    /// <seealso cref="TurnContext.OnDeleteActivity(DeleteActivityHandler)"/>
    public CompletableFuture DeleteActivity(TurnContext context, ConversationReference reference) {
        ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
        // TODO
        //await(connectorClient.conversations().DeleteActivityAsync(reference.conversation().id(), reference.activityId()));
        return completedFuture(null);
    }

    /// <summary>
    /// Deletes a member from the current conversation
    /// </summary>
    /// <param name="context">The context object for the turn.</param>
    /// <param name="memberId">ID of the member to delete from the conversation</param>
    /// <returns></returns>
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

    /// <summary>
    /// Lists the members of a given activity.
    /// </summary>
    /// <param name="context">The context object for the turn.</param>
    /// <param name="activityId">(Optional) Activity ID to enumerate. If not specified the current activities ID will be used.</param>
    /// <returns>List of Members of the activity</returns>
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

    /// <summary>
    /// Lists the members of the current conversation.
    /// </summary>
    /// <param name="context">The context object for the turn.</param>
    /// <returns>List of Members of the current conversation</returns>
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

    /// <summary>
    /// Lists the Conversations in which this bot has participated for a given channel server. The
    /// channel server returns results in pages and each page will include a `continuationToken`
    /// that can be used to fetch the next page of results from the server.
    /// </summary>
    /// <param name="serviceUrl">The URL of the channel server to query.  This can be retrieved
    /// from `context.activity.serviceUrl`. </param>
    /// <param name="credentials">The credentials needed for the Bot to connect to the.services().</param>
    /// <param name="continuationToken">(Optional) token used to fetch the next page of results
    /// from the channel server. This should be left as `null` to retrieve the first page
    /// of results.</param>
    /// <returns>List of Members of the current conversation</returns>
    /// <remarks>
    /// This overload may be called from outside the context of a conversation, as only the
    /// Bot's ServiceUrl and credentials are required.
    /// </remarks>
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

    /// <summary>
    /// Lists the Conversations in which this bot has participated for a given channel server. The
    /// channel server returns results in pages and each page will include a `continuationToken`
    /// that can be used to fetch the next page of results from the server.
    /// </summary>
    /// <param name="context">The context object for the turn.</param>
    /// <param name="continuationToken">(Optional) token used to fetch the next page of results
    /// from the channel server. This should be left as `null` to retrieve the first page
    /// of results.</param>
    /// <returns>List of Members of the current conversation</returns>
    /// <remarks>
    /// This overload may be called during standard Activity processing, at which point the Bot's
    /// service URL and credentials that are part of the current activity processing pipeline
    /// will be used.
    /// </remarks>
    public CompletableFuture<ConversationsResult> GetConversations(TurnContext context) {
        return GetConversations(context, null);
    }

    public CompletableFuture<ConversationsResult> GetConversations(TurnContext context, String continuationToken) {
        ConnectorClient connectorClient = context.getServices().Get("ConnectorClient");
        // TODO
        //ConversationsResult results = await(connectorClient.conversations().getConversationsAsync());
        return completedFuture(null);
    }


    /// Attempts to retrieve the token for a user that's in a login flow.
    /// </summary>
    /// <param name="context">Context for the current turn of conversation with the user.</param>
    /// <param name="connectionName">Name of the auth connection to use.</param>
    /// <param name="magicCode">(Optional) Optional user entered code to validate.</param>
    /// <returns>Token Response</returns>
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

    /// <summary>
    /// Get the raw signin link to be sent to the user for signin for a connection name.
    /// </summary>
    /// <param name="context">Context for the current turn of conversation with the user.</param>
    /// <param name="connectionName">Name of the auth connection to use.</param>
    /// <returns></returns>
    public CompletableFuture<String> GetOauthSignInLink(TurnContext context, String connectionName) {
        BotAssert.ContextNotNull(context);
        if (StringUtils.isEmpty(connectionName))
            throw new IllegalArgumentException("connectionName");

        //OAuthClient client = this.CreateOAuthApiClient(context);
        //return await(client.GetSignInLinkAsync(context.getActivity(), connectionName));
        return completedFuture(null);
    }

    /// <summary>
    /// Signs the user out with the token server.
    /// </summary>
    /// <param name="context">Context for the current turn of conversation with the user.</param>
    /// <param name="connectionName">Name of the auth connection to use.</param>
    /// <returns></returns>
    public CompletableFuture SignOutUser(TurnContext context, String connectionName) {
        BotAssert.ContextNotNull(context);
        if (StringUtils.isEmpty(connectionName))
            throw new IllegalArgumentException("connectionName");

        //OAuthClient client = this.CreateOAuthApiClient(context);
        //await(client.SignOutUserAsync(context.Activity.From.Id, connectionName));
        return completedFuture(null);
    }

    /// <summary>
    /// Creates a conversation on the specified channel.
    /// </summary>
    /// <param name="channelId">The ID for the channel.</param>
    /// <param name="serviceUrl">The channel's service URL endpoint.</param>
    /// <param name="credentials">The application credentials for the bot.</param>
    /// <param name="conversationParameters">The conversation information to use to
    /// create the conversation.</param>
    /// <param name="callback">The method to call for the resulting bot turn.</param>
    /// <returns>A task that represents the work queued to execute.</returns>
    /// <remarks>To start a conversation, your bot must know its account information
    /// and the user's account information on that channel.
    /// Most channels only support initiating a direct message (non-group) conversation.
    /// <para>The adapter attempts to create a new conversation on the channel, and
    /// then sends a <c>conversationUpdate</c> activity through its middleware pipeline
    /// to the <paramref name="callback"/> method.</para>
    /// <para>If the conversation is established with the
    /// specified users, the ID of the activity's <see cref="IActivity.Conversation"/>
    /// will contain the ID of the new conversation.</para>
    /// </remarks>
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
        ConnectorClient client = context.getServices().Get("ConnectorClient");
        if (client == null) {
            throw new IllegalArgumentException("CreateOAuthApiClient: OAuth requires a valid ConnectorClient instance");
        }
        if (_isEmulatingOAuthCards) {
            return new OAuthClient(client, context.getActivity().serviceUrl());
        }
        return new OAuthClient(client, AuthenticationConstants.OAuthUrl);
    }

    /// <summary>
    /// Creates the connector client asynchronous.
    /// </summary>
    /// <param name="serviceUrl">The service URL.</param>
    /// <param name="claimsIdentity">The claims identity.</param>
    /// <returns>ConnectorClient instance.</returns>
    /// <exception cref="UnsupportedOperationException">ClaimsIdemtity cannot be null. Pass Anonymous ClaimsIdentity if authentication is turned off.</exception>
    private CompletableFuture<ConnectorClient> CreateConnectorClientAsync(String serviceUrl, ClaimsIdentity claimsIdentity) {
        if (claimsIdentity == null) {
            throw new UnsupportedOperationException("ClaimsIdemtity cannot be null. Pass Anonymous ClaimsIdentity if authentication is turned off.");
        }

        // For requests from channel App Id is in Audience claim of JWT token. For emulator it is in AppId claim. For
        // unauthenticated requests we have anonymouse identity provided auth is disabled.
//            var botAppIdClaim = (claimsIdentity.Claims ?.SingleOrDefault(claim = > claim.Type == AuthenticationConstants.AudienceClaim) ?? claimsIdentity.Claims ?.SingleOrDefault(claim = > claim.Type == AuthenticationConstants.AppIdClaim));
//            // For Activities coming from Emulator AppId claim contains the Bot's AAD AppId.
//
//
//            // For anonymous requests (requests with no header) appId is not set in claims.
//            if (botAppIdClaim != null) {
//                String botId = botAppIdClaim.Value;
//                var appCredentials = await(this.GetAppCredentialsAsync(botId));
//                return this.CreateConnectorClient(serviceUrl, appCredentials);
//            } else {
//                return this.CreateConnectorClient(serviceUrl);
//            }
        return completedFuture(null);
    }

    /// <summary>
    /// Creates the connector client.
    /// </summary>
    /// <param name="serviceUrl">The service URL.</param>
    /// <param name="appCredentials">The application credentials for the bot.</param>
    /// <returns>Connector client instance.</returns>
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

    /// <summary>
    /// Gets the application credentials. App Credentials are cached so as to ensure we are not refreshing
    /// token everytime.
    /// </summary>
    /// <param name="appId">The application identifier (AAD Id for the bot).</param>
    /// <returns>App credentials.</returns>
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
