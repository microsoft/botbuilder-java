// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.builder.CloudAdapterBase;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.Conversations;
import com.microsoft.bot.connector.authentication.AuthenticateRequestResult;
import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthentication;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthenticationFactory;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.ConnectorFactory;
import com.microsoft.bot.connector.authentication.GovernmentAuthenticationConstants;
import com.microsoft.bot.connector.authentication.PasswordServiceClientCredentialFactory;
import com.microsoft.bot.connector.authentication.UserTokenClient;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.restclient.ServiceClient;
import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ConversationResourceResponse;
import com.microsoft.bot.schema.SignInResource;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenExchangeState;
import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.bot.schema.TokenStatus;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class CloudAdapterTests {

    @Test
    public void basicMessageActivity() {
        // Arrange
        Bot botMock = Mockito.mock(Bot.class);
        Mockito.when(
            botMock.onTurn(
                Mockito.any(TurnContext.class)
            )
        ).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        CloudAdapter adapter = new CloudAdapter();
        adapter.processIncomingActivity("", createMessageActivity(), botMock);

        // Assert
        Mockito.verify(botMock, Mockito.times(1)).onTurn(Mockito.any(TurnContext.class));
    }

    @Test
    public void constructorWithConfiguration() {
        Properties appSettings = new Properties();
        appSettings.put("MicrosoftAppId", "appId");
        appSettings.put("MicrosoftAppPassword", "appPassword");
        appSettings.put("ChannelService", GovernmentAuthenticationConstants.CHANNELSERVICE);

        ConfigurationTest configuration = new ConfigurationTest();
        configuration.setProperties(appSettings);

        // Act
        CloudAdapter adapter = new CloudAdapter(configuration);

        // Assert

        // TODO: work out what might be a reasonable side effect
    }

    @Test
    public void injectCloudEnvironment() {
        // Arrange
        Bot botMock = Mockito.mock(Bot.class);
        Mockito.when(
            botMock.onTurn(
                Mockito.any(TurnContext.class)
            )
        ).thenReturn(CompletableFuture.completedFuture(null));

        AuthenticateRequestResult authenticateRequestResult = new AuthenticateRequestResult();
        authenticateRequestResult.setClaimsIdentity(new ClaimsIdentity(""));
        authenticateRequestResult.setConnectorFactory(new TestConnectorFactory());
        authenticateRequestResult.setAudience("audience");
        authenticateRequestResult.setCallerId("callerId");

        TestUserTokenClient userTokenClient = new TestUserTokenClient("appId");

        BotFrameworkAuthentication cloudEnvironmentMock = Mockito.mock(BotFrameworkAuthentication.class);
        Mockito.when(
            cloudEnvironmentMock.authenticateRequest(
                Mockito.any(Activity.class),
                Mockito.any(String.class))
        ).thenReturn(CompletableFuture.completedFuture(authenticateRequestResult));
        Mockito.when(
            cloudEnvironmentMock.createUserTokenClient(
                Mockito.any(ClaimsIdentity.class))
        ).thenReturn(CompletableFuture.completedFuture(userTokenClient));

        // Act
        CloudAdapter adapter = new CloudAdapter(cloudEnvironmentMock);
        adapter.processIncomingActivity("", createMessageActivity(), botMock);

        // Assert
        Mockito.verify(botMock, Mockito.times(1)).onTurn(Mockito.any (TurnContext.class));
        Mockito.verify(cloudEnvironmentMock, Mockito.times(1)).authenticateRequest(Mockito.any(Activity.class), Mockito.anyString());
    }

    @Test
    public void cloudAdapterProvidesUserTokenClient() {
        // this is just a basic test to verify the wire-up of a UserTokenClient in the CloudAdapter
        // there is also some coverage for the internal code that creates the TokenExchangeState string

        // Arrange
        String appId = "appId";
        String userId = "userId";
        String channelId = "channelId";
        String conversationId = "conversationId";
        String recipientId = "botId";
        String relatesToActivityId = "relatesToActivityId";
        String connectionName = "connectionName";

        AuthenticateRequestResult authenticateRequestResult = new AuthenticateRequestResult();
        authenticateRequestResult.setClaimsIdentity(new ClaimsIdentity(""));
        authenticateRequestResult.setConnectorFactory(new TestConnectorFactory());
        authenticateRequestResult.setAudience("audience");
        authenticateRequestResult.setCallerId("callerId");

        TestUserTokenClient userTokenClient = new TestUserTokenClient(appId);

        BotFrameworkAuthentication cloudEnvironmentMock = Mockito.mock(BotFrameworkAuthentication.class);
        Mockito.when(
            cloudEnvironmentMock.authenticateRequest(
                Mockito.any(Activity.class),
                Mockito.anyString())
        ).thenReturn(CompletableFuture.completedFuture(authenticateRequestResult));
        Mockito.when(
            cloudEnvironmentMock.createUserTokenClient(
                Mockito.any(ClaimsIdentity.class))
        ).thenReturn(CompletableFuture.completedFuture(userTokenClient));

        UserTokenClientBot bot = new UserTokenClientBot(connectionName);

        // Act
        Activity activity = createMessageActivity(userId, channelId, conversationId, recipientId, relatesToActivityId);
        CloudAdapter adapter = new CloudAdapter(cloudEnvironmentMock);
        adapter.processIncomingActivity("", activity, bot);

        // Assert
        Object[] args_ExchangeToken = userTokenClient.getRecord().get("exchangeToken");
        Assert.assertEquals(userId, args_ExchangeToken[0]);
        Assert.assertEquals(connectionName, args_ExchangeToken[1]);
        Assert.assertEquals(channelId, args_ExchangeToken[2]);
        Assert.assertEquals("TokenExchangeRequest", args_ExchangeToken[3].getClass().getSimpleName());

        Object[] args_GetAadTokens = userTokenClient.getRecord().get("getAadTokens");
        Assert.assertEquals(userId, args_GetAadTokens[0]);
        Assert.assertEquals(connectionName, args_GetAadTokens[1]);
        Assert.assertEquals("x", ((List)args_GetAadTokens[2]).get(0));
        Assert.assertEquals("y", ((List)args_GetAadTokens[2]).get(1));

        Assert.assertEquals(channelId, args_GetAadTokens[3]);

        Object[] args_GetSignInResource = userTokenClient.getRecord().get("getSignInResource");

        // this code is testing the internal CreateTokenExchangeState function by doing the work in reverse
        String state = (String) args_GetSignInResource[0];
        String json;
        TokenExchangeState tokenExchangeState = null;

        try {
            JacksonAdapter jacksonAdapter = new JacksonAdapter();
            json = new String(Base64.getDecoder().decode(state));
            tokenExchangeState = jacksonAdapter.deserialize(json, TokenExchangeState.class);
        } catch (IOException e) {
        }

        Assert.assertEquals(connectionName, tokenExchangeState.getConnectionName());
        Assert.assertEquals(appId, tokenExchangeState.getMsAppId());
        Assert.assertEquals(conversationId, tokenExchangeState.getConversation().getConversation().getId());
        Assert.assertEquals(recipientId, tokenExchangeState.getConversation().getBot().getId());
        Assert.assertEquals(relatesToActivityId, tokenExchangeState.getRelatesTo().getActivityId());

        Assert.assertEquals("finalRedirect", args_GetSignInResource[1]);

        Object[] args_GetTokenStatus = userTokenClient.getRecord().get("getTokenStatus");
        Assert.assertEquals(userId, args_GetTokenStatus[0]);
        Assert.assertEquals(channelId, args_GetTokenStatus[1]);
        Assert.assertEquals("includeFilter", args_GetTokenStatus[2]);

        Object[] args_GetUserToken = userTokenClient.getRecord().get("getUserToken");
        Assert.assertEquals(userId, args_GetUserToken[0]);
        Assert.assertEquals(connectionName, args_GetUserToken[1]);
        Assert.assertEquals(channelId, args_GetUserToken[2]);
        Assert.assertEquals("magicCode", args_GetUserToken[3]);

        Object[] args_SignOutUser = userTokenClient.getRecord().get("signOutUser");
        Assert.assertEquals(userId, args_SignOutUser[0]);
        Assert.assertEquals(connectionName, args_SignOutUser[1]);
        Assert.assertEquals(channelId, args_SignOutUser[2]);
    }

    @Test
    public void cloudAdapterConnectorFactory() {
        // this is just a basic test to verify the wire-up of a ConnectorFactory in the CloudAdapter

        // Arrange
        ClaimsIdentity claimsIdentity = new ClaimsIdentity("");

        AuthenticateRequestResult authenticateRequestResult = new AuthenticateRequestResult();
        authenticateRequestResult.setClaimsIdentity(claimsIdentity);
        authenticateRequestResult.setConnectorFactory(new TestConnectorFactory());
        authenticateRequestResult.setAudience("audience");
        authenticateRequestResult.setCallerId("callerId");

        TestUserTokenClient userTokenClient = new TestUserTokenClient("appId");

        BotFrameworkAuthentication cloudEnvironmentMock = Mockito.mock(BotFrameworkAuthentication.class);
        Mockito.when(
            cloudEnvironmentMock.authenticateRequest(
                Mockito.any(Activity.class),
                Mockito.anyString())
        ).thenReturn(CompletableFuture.completedFuture(authenticateRequestResult));
        Mockito.when(
            cloudEnvironmentMock.createConnectorFactory(
                Mockito.any(ClaimsIdentity.class))
        ).thenReturn(new TestConnectorFactory());
        Mockito.when(
            cloudEnvironmentMock.createUserTokenClient(
                Mockito.any(ClaimsIdentity.class))
        ).thenReturn(CompletableFuture.completedFuture(userTokenClient));

        ConnectorFactoryBot bot = new ConnectorFactoryBot();

        // Act
        CloudAdapter adapter = new CloudAdapter(cloudEnvironmentMock);
        adapter.processIncomingActivity("", createMessageActivity(), bot);

        // Assert
        Assert.assertEquals("audience", bot.authorization);
        Assert.assertEquals(claimsIdentity, bot.identity);
        Assert.assertEquals(userTokenClient, bot.userTokenClient);
        Assert.assertTrue(bot.connectorClient != null);
        Assert.assertTrue(bot.botCallbackHandler != null);
    }

    @Test
    public void cloudAdapterContinueConversation() {
        // Arrange
        ClaimsIdentity claimsIdentity = new ClaimsIdentity("");

        AuthenticateRequestResult authenticateRequestResult = new AuthenticateRequestResult();
        authenticateRequestResult.setClaimsIdentity(claimsIdentity);
        authenticateRequestResult.setConnectorFactory(new TestConnectorFactory());
        authenticateRequestResult.setAudience("audience");
        authenticateRequestResult.setCallerId("callerId");

        TestUserTokenClient userTokenClient = new TestUserTokenClient("appId");

        BotFrameworkAuthentication cloudEnvironmentMock = Mockito.mock(BotFrameworkAuthentication.class);
        Mockito.when(
            cloudEnvironmentMock.authenticateRequest(
                Mockito.any(Activity.class),
                Mockito.anyString())
        ).thenReturn(CompletableFuture.completedFuture(authenticateRequestResult));
        Mockito.when(
            cloudEnvironmentMock.createConnectorFactory(
                Mockito.any(ClaimsIdentity.class))
        ).thenReturn(new TestConnectorFactory());
        Mockito.when(
            cloudEnvironmentMock.createUserTokenClient(
                Mockito.any(ClaimsIdentity.class))
        ).thenReturn(CompletableFuture.completedFuture(userTokenClient));

        // NOTE: present in C# but not used
        ConnectorFactoryBot bot = new ConnectorFactoryBot();

        String expectedServiceUrl = "http://serviceUrl";

        ConversationAccount conversationAccount = new ConversationAccount();
        conversationAccount.setId("conversation Id");

        Activity continuationActivity = new Activity(ActivityTypes.EVENT);
        continuationActivity.setServiceUrl(expectedServiceUrl);
        continuationActivity.setConversation(conversationAccount);

        ConversationReference conversationReference = new ConversationReference();
        conversationReference.setServiceUrl(expectedServiceUrl);
        conversationReference.setConversation(conversationAccount);

        final String[] actualServiceUrl1 = {""};
        final String[] actualServiceUrl2 = {""};
        final String[] actualServiceUrl3 = {""};
        final String[] actualServiceUrl4 = {""};
        final String[] actualServiceUrl5 = {""};
        final String[] actualServiceUrl6 = {""};

        BotCallbackHandler callback1 = (t) -> {
            actualServiceUrl1[0] = t.getActivity().getServiceUrl();
            return CompletableFuture.completedFuture(null);
        };

        BotCallbackHandler callback2 = (t) -> {
            actualServiceUrl2[0] = t.getActivity().getServiceUrl();
            return CompletableFuture.completedFuture(null);
        };

        BotCallbackHandler callback3 = (t) -> {
            actualServiceUrl3[0] = t.getActivity().getServiceUrl();
            return CompletableFuture.completedFuture(null);
        };

        BotCallbackHandler callback4 = (t) -> {
            actualServiceUrl4[0] = t.getActivity().getServiceUrl();
            return CompletableFuture.completedFuture(null);
        };

        BotCallbackHandler callback5 = (t) -> {
            actualServiceUrl5[0] = t.getActivity().getServiceUrl();
            return CompletableFuture.completedFuture(null);
        };

        BotCallbackHandler callback6 = (t) -> {
            actualServiceUrl6[0] = t.getActivity().getServiceUrl();
            return CompletableFuture.completedFuture(null);
        };

        // Act
        CloudAdapter adapter = new CloudAdapter(cloudEnvironmentMock);
        adapter.continueConversation("botAppId", continuationActivity, callback1);
        adapter.continueConversation(claimsIdentity, continuationActivity, callback2);
        adapter.continueConversation(claimsIdentity, continuationActivity, "audience", callback3);
        adapter.continueConversation("botAppId", conversationReference, callback4);
        adapter.continueConversation(claimsIdentity, conversationReference, callback5);
        adapter.continueConversation(claimsIdentity, conversationReference, "audience", callback6);

        // Assert
        Assert.assertEquals(expectedServiceUrl, actualServiceUrl1[0]);
        Assert.assertEquals(expectedServiceUrl, actualServiceUrl2[0]);
        Assert.assertEquals(expectedServiceUrl, actualServiceUrl3[0]);
        Assert.assertEquals(expectedServiceUrl, actualServiceUrl4[0]);
        Assert.assertEquals(expectedServiceUrl, actualServiceUrl5[0]);
        Assert.assertEquals(expectedServiceUrl, actualServiceUrl6[0]);
    }

    @Test
    public void cloudAdapterDelay() {
        DelayHelper.test(new CloudAdapter());
    }

    @Test
    public void cloudAdapterCreateConversation() {
        // Arrange
        ClaimsIdentity claimsIdentity = new ClaimsIdentity("");

        AuthenticateRequestResult authenticateRequestResult = new AuthenticateRequestResult();
        authenticateRequestResult.setClaimsIdentity(claimsIdentity);
        authenticateRequestResult.setConnectorFactory(new TestConnectorFactory());
        authenticateRequestResult.setAudience("audience");
        authenticateRequestResult.setCallerId("callerId");

        TestUserTokenClient userTokenClient = new TestUserTokenClient("appId");

        ConversationResourceResponse conversationResourceResponse = new ConversationResourceResponse();
        Conversations conversationsMock = Mockito.mock(Conversations.class);
        Mockito.when(
            conversationsMock.createConversation(
                Mockito.any(ConversationParameters.class))
        ).thenReturn(CompletableFuture.completedFuture(conversationResourceResponse));

        ConnectorClient connectorMock = Mockito.mock(ConnectorClient.class);
        Mockito.when(
            connectorMock.getConversations()
        ).thenReturn(conversationsMock);

        String expectedServiceUrl = "http://serviceUrl";
        String expectedAudience = "audience";

        ConnectorFactory connectorFactoryMock = Mockito.mock(ConnectorFactory.class);
        Mockito.when(
            connectorFactoryMock.create(
                expectedServiceUrl,
                expectedAudience)
        ).thenReturn(CompletableFuture.completedFuture(connectorMock));

        BotFrameworkAuthentication cloudEnvironmentMock = Mockito.mock(BotFrameworkAuthentication.class);
        Mockito.when(
            cloudEnvironmentMock.authenticateRequest(
                Mockito.any(Activity.class),
                Mockito.anyString())
        ).thenReturn(CompletableFuture.completedFuture(authenticateRequestResult));
        Mockito.when(
            cloudEnvironmentMock.createConnectorFactory(
                Mockito.any(ClaimsIdentity.class))
        ).thenReturn(connectorFactoryMock);
        Mockito.when(
            cloudEnvironmentMock.createUserTokenClient(
                Mockito.any(ClaimsIdentity.class))
        ).thenReturn(CompletableFuture.completedFuture(userTokenClient));

        String expectedChannelId = "expected-channel-id";
        final String[] actualChannelId = {""};

        BotCallbackHandler callback1 = (t) -> {
            actualChannelId[0] = t.getActivity().getChannelId();
            return CompletableFuture.completedFuture(null);
        };

        ConversationParameters conversationParameters = new ConversationParameters();
        conversationParameters.setIsGroup(false);
        conversationParameters.setBot(new ChannelAccount());
        conversationParameters.setMembers(Arrays.asList(new ChannelAccount()));
        conversationParameters.setTenantId("tenantId");

        // Act
        CloudAdapter adapter = new CloudAdapter(cloudEnvironmentMock);
        adapter.createConversation("botAppId", expectedChannelId, expectedServiceUrl, expectedAudience, conversationParameters, callback1).join();

        // Assert
        Assert.assertEquals(expectedChannelId, actualChannelId[0]);
    }

    private static Activity createMessageActivity() {
        return createMessageActivity("userId", "channelId", "conversationId", "botId", "relatesToActivityId");
    }

    private static Activity createMessageActivity(String userId, String channelId, String conversationId, String recipient, String relatesToActivityId) {
        ConversationAccount conversationAccount = new ConversationAccount();
        conversationAccount.setId(conversationId);

        ChannelAccount fromChannelAccount = new ChannelAccount();
        fromChannelAccount.setId(userId);

        ChannelAccount toChannelAccount = new ChannelAccount();
        toChannelAccount.setId(recipient);

        ConversationReference conversationReference = new ConversationReference();
        conversationReference.setActivityId(relatesToActivityId);

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setText("hi");
        activity.setServiceUrl("http://localhost");
        activity.setChannelId(channelId);
        activity.setConversation(conversationAccount);
        activity.setFrom(fromChannelAccount);
        activity.setLocale("locale");
        activity.setRecipient(toChannelAccount);
        activity.setRelatesTo(conversationReference);

        return activity;
    }

    private static Response createInternalHttpResponse() {
        return new Response.Builder()
            .request(new Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("")
            .body(ResponseBody.create(
                MediaType.get("application/json; charset=utf-8"),
                "{\"id\": \"sendActivityId\"}"))
            .build();
    }

    private class MessageBot implements Bot {
        public CompletableFuture<Void> onTurn(TurnContext turnContext) {
            return turnContext.sendActivity(MessageFactory.text("rage.rage.against.the.dying.of.the.light")).thenApply(result -> null);
        }
    }

    private class UserTokenClientBot implements Bot {
        private String connectionName;

        public UserTokenClientBot(String withConnectionName) {
            connectionName = withConnectionName;
        }

        public CompletableFuture<Void> onTurn(TurnContext turnContext) {
            // in the product the following calls are made from within the sign-in prompt begin and continue methods

            UserTokenClient userTokenClient = turnContext.getTurnState().get(CloudAdapterBase.USER_TOKEN_CLIENT_KEY);

            userTokenClient.exchangeToken(
                turnContext.getActivity().getFrom().getId(),
                connectionName,
                turnContext.getActivity().getChannelId(),
                new TokenExchangeRequest()).join();

            userTokenClient.getAadTokens(
                turnContext.getActivity().getFrom().getId(),
                connectionName,
                Arrays.asList("x", "y"),
                turnContext.getActivity().getChannelId()).join();

            userTokenClient.getSignInResource(
                connectionName,
                turnContext.getActivity(),
                "finalRedirect").join();

            userTokenClient.getTokenStatus(
                turnContext.getActivity().getFrom().getId(),
                turnContext.getActivity().getChannelId(),
                "includeFilter").join();

            userTokenClient.getUserToken(
                turnContext.getActivity().getFrom().getId(),
                connectionName,
                turnContext.getActivity().getChannelId(),
                "magicCode").join();

            // in the product code the sign-out call is generally run as a general intercept before any dialog logic

            userTokenClient.signOutUser(
                turnContext.getActivity().getFrom().getId(),
                connectionName,
                turnContext.getActivity().getChannelId()).join();
            return null;
        }
    }

    private class TestUserTokenClient extends UserTokenClient {
        private String appId;

        public TestUserTokenClient(String withAppId) {
            appId = withAppId;
        }

        private Map<String, Object[]> record = new HashMap<>();

        public Map<String, Object[]> getRecord() {
            return record;
        }

        @Override
        public CompletableFuture<TokenResponse> exchangeToken(String userId, String connectionName, String channelId, TokenExchangeRequest exchangeRequest) {
            capture("exchangeToken", userId, connectionName, channelId, exchangeRequest);
            return CompletableFuture.completedFuture(new TokenResponse() { });
        }

        @Override
        public CompletableFuture<SignInResource> getSignInResource(String connectionName, Activity activity, String finalRedirect) {
            String state = createTokenExchangeState(appId, connectionName, activity);
            capture("getSignInResource", state, finalRedirect);
            return CompletableFuture.completedFuture(new SignInResource() { });
        }

        @Override
        public CompletableFuture<List<TokenStatus>> getTokenStatus(String userId, String channelId, String includeFilter) {
            capture("getTokenStatus", userId, channelId, includeFilter);
            return CompletableFuture.completedFuture(Arrays.asList(new TokenStatus[0]));
        }

        @Override
        public CompletableFuture<Map<String, TokenResponse>> getAadTokens(String userId, String connectionName, List<String> resourceUrls, String channelId) {
            capture("getAadTokens", userId, connectionName, resourceUrls, channelId);
            return CompletableFuture.completedFuture(new HashMap<String, TokenResponse>() { });
        }

        @Override
        public CompletableFuture<TokenResponse> getUserToken(String userId, String connectionName, String channelId, String magicCode) {
            capture("getUserToken", userId, connectionName, channelId, magicCode);
            return CompletableFuture.completedFuture(new TokenResponse());
        }

        @Override
        public CompletableFuture<Void> signOutUser(String userId, String connectionName, String channelId) {
            capture("signOutUser", userId, connectionName, channelId);
            return CompletableFuture.completedFuture(null);
        }

        private void capture(String name, Object... args) {
            record.put(name, args);
        }
    }

    private class ConnectorFactoryBot implements Bot {
        private ClaimsIdentity identity;
        private ConnectorClient connectorClient;
        private UserTokenClient userTokenClient;
        private BotCallbackHandler botCallbackHandler;
        private String oAuthScope;
        private String authorization;

        public CompletableFuture<Void> onTurn(TurnContext turnContext) {
            // verify the bot-framework protocol TurnState has been setup by the adapter
            identity = turnContext.getTurnState().get(BotFrameworkAdapter.BOT_IDENTITY_KEY);
            connectorClient = turnContext.getTurnState().get(BotFrameworkAdapter.CONNECTOR_CLIENT_KEY);
            userTokenClient = turnContext.getTurnState().get(CloudAdapterBase.USER_TOKEN_CLIENT_KEY);
            botCallbackHandler = turnContext.getTurnState().get(TurnContextImpl.BOT_CALLBACK_HANDLER_KEY);
            oAuthScope = turnContext.getTurnState().get(BotAdapter.OAUTH_SCOPE_KEY);

            ConnectorFactory connectorFactory = turnContext.getTurnState().get(CloudAdapterBase.CONNECTOR_FACTORY_KEY);

            return connectorFactory.create("http://localhost/originalServiceUrl", oAuthScope).thenCompose(connector -> {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    connector.credentials().applyCredentialsFilter(builder);
                    ServiceClient serviceClient = new ServiceClient("http://localhost", builder, new Retrofit.Builder()) { };
                try {
                    Response response = serviceClient.httpClient().newCall(new Request.Builder().url("http://localhost").build()).execute();
                    authorization = response.header("Authorization");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
                }
            );
        }
    }

    private class TestCredentials implements ServiceClientCredentials {
        private String testToken;

        public String getTestToken() {
            return testToken;
        }

        public TestCredentials(String withTestToken) {
            testToken = withTestToken;
        }

        @Override
        public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
            clientBuilder.addInterceptor(new TestCredentialsInterceptor(this));
        }
    }
    public class TestCredentialsInterceptor implements Interceptor {
        private TestCredentials credentials;

        public TestCredentialsInterceptor(TestCredentials withCredentials) {
            credentials = withCredentials;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            String header = chain.request().header("Authorization");
            Assert.assertNull(header);
            return new Response.Builder()
                .header("Authorization", credentials.getTestToken())
                .request(chain.request())
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .body(ResponseBody.create(MediaType.parse("text/plain"), "azure rocks"))
                .build();
        }
    }

    private class TestConnectorFactory extends ConnectorFactory {
        @Override
        public CompletableFuture<ConnectorClient> create(String serviceUrl, String audience) {
            TestCredentials credentials = new TestCredentials(StringUtils.isNotBlank(audience) ? audience : "test-token");
            return CompletableFuture.completedFuture(new RestConnectorClient(serviceUrl, credentials));
        }
    }

    private class ConfigurationTest implements Configuration {
        private Properties properties;

        public void setProperties(Properties withProperties) {
            this.properties = withProperties;
        }

        @Override
        public String getProperty(String key) {
            return properties.getProperty(key);
        }

        @Override
        public Properties getProperties() {
            return this.properties;
        }

        @Override
        public String[] getProperties(String key) {
            String baseProperty = properties.getProperty(key);
            if (baseProperty != null) {
                String[] splitProperties = baseProperty.split(",");
                return splitProperties;
            } else {
                return null;
            }
        }
    }
}
