// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.adapters;

import com.microsoft.bot.builder.*;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.rules.ExpectedException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TestAdapter extends BotAdapter implements UserTokenProvider {

    private final String exceptionExpected = "ExceptionExpected";
    private final Queue<Activity> botReplies = new LinkedList<>();
    private int nextId = 0;
    private ConversationReference conversationReference;
    private String locale = "en-us";
    private boolean sendTraceActivity = false;
    private Map<ExchangableTokenKey, String> exchangableToken = new HashMap<ExchangableTokenKey, String>();


    private static class UserTokenKey {
        private String connectionName;
        private String userId;
        private String channelId;

        public String getConnectionName() {
            return connectionName;
        }

        public void setConnectionName(String withConnectionName) {
            connectionName = withConnectionName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String withUserId) {
            userId = withUserId;
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String withChannelId) {
            channelId = withChannelId;
        }



        @Override
        public boolean equals(Object rhs) {
            if (!(rhs instanceof UserTokenKey))
                return false;
            return StringUtils.equals(connectionName, ((UserTokenKey) rhs).connectionName)
                    && StringUtils.equals(userId, ((UserTokenKey) rhs).userId)
                    && StringUtils.equals(channelId, ((UserTokenKey) rhs).channelId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(connectionName, userId, channelId);
        }
    }

    private static class TokenMagicCode {
        public UserTokenKey key;
        public String magicCode;
        public String userToken;
    }

    private Map<UserTokenKey, String> userTokens = new HashMap<>();
    private List<TokenMagicCode> magicCodes = new ArrayList<>();

    public TestAdapter() {
        this((ConversationReference) null);
    }

    public TestAdapter(String channelId) {
        this(channelId, false);
    }

    public TestAdapter(String channelId, boolean sendTraceActivity) {
        this.sendTraceActivity = sendTraceActivity;

        ConversationReference conversationReference = new ConversationReference();
        conversationReference.setChannelId(channelId);
        conversationReference.setServiceUrl("https://test.com");
        ChannelAccount userAccount = new ChannelAccount();
        userAccount.setId("user1");
        userAccount.setName("User1");
        conversationReference.setUser(userAccount);
        ChannelAccount botAccount = new ChannelAccount();
        botAccount.setId("bot");
        botAccount.setName("Bot");
        conversationReference.setBot(botAccount);
        ConversationAccount conversation = new ConversationAccount();
        conversation.setIsGroup(false);
        conversation.setConversationType("convo1");
        conversation.setId("Conversation1");
        conversationReference.setConversation(conversation);
        conversationReference.setLocale(this.getLocale());

        setConversationReference(conversationReference);
    }

    public TestAdapter(ConversationReference reference) {
        if (reference != null) {
            setConversationReference(reference);
        } else {
            ConversationReference conversationReference = new ConversationReference();
            conversationReference.setChannelId(Channels.TEST);
            conversationReference.setServiceUrl("https://test.com");
            ChannelAccount userAccount = new ChannelAccount();
            userAccount.setId("user1");
            userAccount.setName("User1");
            conversationReference.setUser(userAccount);
            ChannelAccount botAccount = new ChannelAccount();
            botAccount.setId("bot");
            botAccount.setName("Bot");
            conversationReference.setBot(botAccount);
            ConversationAccount conversation = new ConversationAccount();
            conversation.setIsGroup(false);
            conversation.setConversationType("convo1");
            conversation.setId("Conversation1");
            conversationReference.setConversation(conversation);
            conversationReference.setLocale(this.getLocale());
            setConversationReference(conversationReference);
        }
    }
    public TestAdapter(ConversationReference reference, boolean sendTraceActivity) {
        this(reference);
        this.sendTraceActivity = sendTraceActivity;
    }

    public Queue<Activity> activeQueue() {
        return botReplies;
    }

    @Override
    public TestAdapter use(Middleware middleware) {
        super.use(middleware);
        return this;
    }

    /**
     * Adds middleware to the adapter to register an Storage object on the turn
     * context. The middleware registers the state objects on the turn context at
     * the start of each turn.
     *
     * @param storage The storage object to register.
     * @return The updated adapter.
     */
    public TestAdapter useStorage(Storage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage cannot be null");
        }
        return this.use(new RegisterClassMiddleware<Storage>(storage));
    }

    /**
     * Adds middleware to the adapter to register one or more BotState objects on
     * the turn context. The middleware registers the state objects on the turn
     * context at the start of each turn.
     *
     * @param botstates The state objects to register.
     * @return The updated adapter.
     */
    public TestAdapter useBotState(BotState... botstates) {
        if (botstates == null) {
            throw new IllegalArgumentException("botstates cannot be null");
        }
        for (BotState botState : botstates) {
            this.use(new RegisterClassMiddleware<BotState>(botState));
        }
        return this;
    }

    public CompletableFuture<Void> processActivity(Activity activity, BotCallbackHandler callback) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (conversationReference()) {
                // ready for next reply
                if (activity.getType() == null)
                    activity.setType(ActivityTypes.MESSAGE);

                if (activity.getChannelId() == null) {
                    activity.setChannelId(conversationReference().getChannelId());
                }

                if (activity.getFrom() == null || StringUtils.equalsIgnoreCase(activity.getFrom().getId(), "unknown")
                        || activity.getFrom().getRole() == RoleTypes.BOT) {
                    activity.setFrom(conversationReference().getUser());
                }

                activity.setRecipient(conversationReference().getBot());
                activity.setConversation(conversationReference().getConversation());
                activity.setServiceUrl(conversationReference().getServiceUrl());

                Integer next = nextId++;
                activity.setId(next.toString());
            }
            // Assume Default DateTime : DateTime(0)
            if (activity.getTimestamp() == null || activity.getTimestamp().toEpochSecond() == 0)
                activity.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));

            if (activity.getLocalTimestamp() == null || activity.getLocalTimestamp().toEpochSecond() == 0)
                activity.setLocalTimestamp(OffsetDateTime.now());

            return activity;
        }).thenCompose(activity1 -> {
            TurnContextImpl context = new TurnContextImpl(this, activity1);
            return super.runPipeline(context, callback);
        });
    }

    public ConversationReference conversationReference() {
        return conversationReference;
    }

    public void setConversationReference(ConversationReference conversationReference) {
        this.conversationReference = conversationReference;
    }

    @Override
    public CompletableFuture<ResourceResponse[]> sendActivities(TurnContext context, List<Activity> activities) {
        List<ResourceResponse> responses = new LinkedList<ResourceResponse>();

        for (Activity activity : activities) {
            if (StringUtils.isEmpty(activity.getId()))
                activity.setId(UUID.randomUUID().toString());

            if (activity.getTimestamp() == null)
                activity.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));

            responses.add(new ResourceResponse(activity.getId()));

            System.out.println(String.format("TestAdapter:SendActivities, Count:%s (tid:%s)", activities.size(),
                    Thread.currentThread().getId()));
            for (Activity act : activities) {
                System.out.printf(" :--------\n : To:%s\n", (act.getRecipient() == null) ? "No recipient set" : act.getRecipient().getName());
                System.out.printf(" : From:%s\n", (act.getFrom() == null) ? "No from set" : act.getFrom().getName());
                System.out.printf(" : Text:%s\n :---------\n", (act.getText() == null) ? "No text set" : act.getText());
            }

            // This is simulating DELAY
            if (activity.getType().toString().equals("delay")) {
                // The BotFrameworkAdapter and Console adapter implement this
                // hack directly in the POST method. Replicating that here
                // to keep the behavior as close as possible to facilitate
                // more realistic tests.
                int delayMs = (int) activity.getValue();
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                }
            } else if (activity.getType().equals(ActivityTypes.TRACE)) {
                if (sendTraceActivity) {
                    synchronized (botReplies) {
                        botReplies.add(activity);
                    }
                }
            } else {
                synchronized (botReplies) {
                    botReplies.add(activity);
                }
            }
        }
        return CompletableFuture.completedFuture(responses.toArray(new ResourceResponse[responses.size()]));
    }

    @Override
    public CompletableFuture<ResourceResponse> updateActivity(TurnContext context, Activity activity) {
        synchronized (botReplies) {
            List<Activity> replies = new ArrayList<>(botReplies);
            for (int i = 0; i < botReplies.size(); i++) {
                if (replies.get(i).getId().equals(activity.getId())) {
                    replies.set(i, activity);
                    botReplies.clear();

                    for (Activity item : replies) {
                        botReplies.add(item);
                    }
                    return CompletableFuture.completedFuture(new ResourceResponse(activity.getId()));
                }
            }
        }
        return CompletableFuture.completedFuture(new ResourceResponse());
    }

    @Override
    public CompletableFuture<Void> deleteActivity(TurnContext context, ConversationReference reference) {
        synchronized (botReplies) {
            ArrayList<Activity> replies = new ArrayList<>(botReplies);
            for (int i = 0; i < botReplies.size(); i++) {
                if (replies.get(i).getId().equals(reference.getActivityId())) {
                    replies.remove(i);
                    botReplies.clear();
                    for (Activity item : replies) {
                        botReplies.add(item);
                    }
                    break;
                }
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called by TestFlow to check next reply
     *
     * @return
     */
    public Activity getNextReply() {
        synchronized (botReplies) {
            if (botReplies.size() > 0) {
                return botReplies.remove();
            }
        }
        return null;
    }

    /**
     * Called by TestFlow to get appropriate activity for conversationReference of
     * testbot
     *
     * @return
     */
    public Activity makeActivity() {
        return makeActivity(null);
    }

    public Activity makeActivity(String withText) {
        Integer next = nextId++;
        String locale = !getLocale().isEmpty() ? getLocale() : "en-us";
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setLocale(locale);
        activity.setFrom(conversationReference().getUser());
        activity.setRecipient(conversationReference().getBot());
        activity.setConversation(conversationReference().getConversation());
        activity.setServiceUrl(conversationReference().getServiceUrl());
        activity.setId(next.toString());
        activity.setText(withText);
        activity.setLocale(getLocale() != null ? getLocale() : "en-us");

        return activity;
    }

    /**
     * Called by TestFlow to send text to the bot
     *
     * @param userSays
     * @return
     */
    public CompletableFuture<Void> sendTextToBot(String userSays, BotCallbackHandler callback) {
        return processActivity(this.makeActivity(userSays), callback);
    }

    public void addUserToken(String connectionName, String channelId, String userId, String token,
            String withMagicCode) {
        UserTokenKey userKey = new UserTokenKey();
        userKey.connectionName = connectionName;
        userKey.channelId = channelId;
        userKey.userId = userId;

        if (withMagicCode == null) {
            userTokens.put(userKey, token);
        } else {
            TokenMagicCode tokenMagicCode = new TokenMagicCode();
            tokenMagicCode.key = userKey;
            tokenMagicCode.magicCode = withMagicCode;
            tokenMagicCode.userToken = token;
            magicCodes.add(tokenMagicCode);
        }
    }

    public CompletableFuture<TokenResponse> getUserToken(TurnContext turnContext, String connectionName,
            String magicCode) {
        return getUserToken(turnContext, null, connectionName, magicCode);

    }
    public CompletableFuture<Void> signOutUser(TurnContext turnContext, String connectionName, String userId) {
        return signOutUser(turnContext, null, connectionName, userId);
    }

    public CompletableFuture<List<TokenStatus>> getTokenStatus(TurnContext turnContext, String userId,
            String includeFilter) {
        return getTokenStatus(turnContext, null, userId, includeFilter);
    }

    public CompletableFuture<Map<String, TokenResponse>> getAadTokens(TurnContext turnContext, String connectionName,
            String[] resourceUrls, String userId) {
        return getAadTokens(turnContext, null, connectionName, resourceUrls, userId);
    }

    public static ConversationReference createConversationReference(String name, String user, String bot) {
        ConversationReference reference = new ConversationReference();
        reference.setChannelId("test");
        reference.setServiceUrl("https://test.com");
        reference.setConversation(new ConversationAccount(false, name, name, null, null, null, null));
        reference.setUser(new ChannelAccount(user.toLowerCase(), user));
        reference.setBot(new ChannelAccount(bot.toLowerCase(), bot));
        reference.setLocale("en-us");
        return reference;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public void setSendTraceActivity(boolean sendTraceActivity) {
        this.sendTraceActivity = sendTraceActivity;
    }

    public boolean getSendTraceActivity() {
        return sendTraceActivity;
    }

    public CompletableFuture<String> getOAuthSignInLink(TurnContext turnContext, String connectionName) {
        return getOAuthSignInLink(turnContext, null, connectionName);
    }

    public CompletableFuture<String> getOAuthSignInLink(TurnContext turnContext, String connectionName, String userId,
            String finalRedirect) {
        return getOAuthSignInLink(turnContext, null, connectionName, userId, finalRedirect);
    }

    @Override
    public CompletableFuture<String> getOAuthSignInLink(TurnContext turnContext, AppCredentials oAuthAppCredentials,
            String connectionName) {
        return CompletableFuture.completedFuture(
            String.format("https://fake.com/oauthsignin/%s/%s",
                            connectionName,
                            turnContext.getActivity().getChannelId()));
    }

    public CompletableFuture<String> getOAuthSignInLink(TurnContext turnContext, AppCredentials oAuthAppCredentials,
        String connectionName, String userId, String finalRedirect) {
        return CompletableFuture.completedFuture(
            String.format("https://fake.com/oauthsignin/%s/%s/%s",
                            connectionName,
                            turnContext.getActivity().getChannelId(),
                            userId));
    }

    @Override
    public CompletableFuture<TokenResponse> getUserToken(TurnContext turnContext, AppCredentials oAuthAppCredentials,
            String connectionName, String magicCode) {
                UserTokenKey key = new UserTokenKey();
                key.connectionName = connectionName;
                key.channelId = turnContext.getActivity().getChannelId();
                key.userId = turnContext.getActivity().getFrom().getId();

                if (magicCode != null) {
                    TokenMagicCode magicCodeRecord = magicCodes.stream()
                            .filter(tokenMagicCode -> key.equals(tokenMagicCode.key)).findFirst().orElse(null);
                    if (magicCodeRecord != null && StringUtils.equals(magicCodeRecord.magicCode, magicCode)) {
                        addUserToken(connectionName, key.channelId, key.userId, magicCodeRecord.userToken, null);
                    }
                }

                if (userTokens.containsKey(key)) {
                    TokenResponse tokenResponse = new TokenResponse();
                    tokenResponse.setConnectionName(connectionName);
                    tokenResponse.setToken(userTokens.get(key));
                    return CompletableFuture.completedFuture(tokenResponse);
                }

                return CompletableFuture.completedFuture(null);
     }

    /**
     * Adds a fake exchangeable token so it can be exchanged later.
     * @param connectionName he connection name.
     * @param channelId The channel ID.
     * @param userId The user ID.
     * @param exchangableItem The exchangeable token or resource URI.
     * @param token The token to store.
     */
    public void addExchangeableToken(String connectionName,
                                        String channelId,
                                        String userId,
                                        String exchangableItem,
                                        String token
    ) {
        ExchangableTokenKey key = new ExchangableTokenKey();
        key.setConnectionName(connectionName);
        key.setChannelId(channelId);
        key.setUserId(userId);
        key.setExchangableItem(exchangableItem);

        if (exchangableToken.containsKey(key)) {
            exchangableToken.replace(key, token);
        } else {
            exchangableToken.put(key, token);
        }
    }

    public void throwOnExchangeRequest(String connectionName,
    String channelId,
    String userId,
    String exchangableItem) {
        ExchangableTokenKey key = new ExchangableTokenKey();
        key.setConnectionName(connectionName);
        key.setChannelId(channelId);
        key.setUserId(userId);
        key.setExchangableItem(exchangableItem);

        if (exchangableToken.containsKey(key)) {
            exchangableToken.replace(key, exceptionExpected);
        } else {
            exchangableToken.put(key, exceptionExpected);
        }
    }

    @Override
    public CompletableFuture<Void> signOutUser(TurnContext turnContext, AppCredentials oAuthAppCredentials,
            String connectionName, String userId) {
            String channelId = turnContext.getActivity().getChannelId();
            final String effectiveUserId = userId == null ? turnContext.getActivity().getFrom().getId() : userId;

            userTokens.keySet().stream()
                    .filter(t -> StringUtils.equals(t.channelId, channelId)
                            && StringUtils.equals(t.userId, effectiveUserId)
                            && connectionName == null || StringUtils.equals(t.connectionName, connectionName))
                    .collect(Collectors.toList()).forEach(key -> userTokens.remove(key));

            return CompletableFuture.completedFuture(null);
        }

    @Override
    public CompletableFuture<List<TokenStatus>> getTokenStatus(TurnContext turnContext,
                                                               AppCredentials oAuthAppCredentials,
                                                               String userId,
                                                               String includeFilter) {
        String[] filter = includeFilter == null ? null : includeFilter.split(",");
        List<TokenStatus> records = userTokens.keySet().stream()
                .filter(x -> StringUtils.equals(x.channelId, turnContext.getActivity().getChannelId())
                        && StringUtils.equals(x.userId, turnContext.getActivity().getFrom().getId())
                        && (includeFilter == null || Arrays.binarySearch(filter, x.connectionName) != -1))
                .map(r -> {
                    TokenStatus tokenStatus = new TokenStatus();
                    tokenStatus.setConnectionName(r.connectionName);
                    tokenStatus.setHasToken(true);
                    tokenStatus.setServiceProviderDisplayName(r.connectionName);
                    return tokenStatus;
                }).collect(Collectors.toList());

        if (records.size() > 0) {
            return CompletableFuture.completedFuture(records);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Map<String, TokenResponse>> getAadTokens(TurnContext context,
            AppCredentials oAuthAppCredentials, String connectionName, String[] resourceUrls, String userId) {
        return CompletableFuture.completedFuture(new HashMap<>());
    }

    @Override
    public CompletableFuture<SignInResource> getSignInResource(TurnContext turnContext, String connectionName) {
        String id = null;
        if (turnContext != null
            && turnContext.getActivity() != null
            && turnContext.getActivity().getRecipient() != null
            && turnContext.getActivity().getRecipient().getId() != null) {
                id = turnContext.getActivity().getRecipient().getId();
        }

        return getSignInResource(turnContext, connectionName, id, null);
    }

    @Override
    public CompletableFuture<SignInResource> getSignInResource(TurnContext turnContext, String connectionName,
            String userId, String finalRedirect) {
                return getSignInResource(turnContext, null, connectionName, userId, finalRedirect);
    }

    @Override
    public CompletableFuture<SignInResource> getSignInResource(TurnContext turnContext,
            AppCredentials oAuthAppCredentials, String connectionName, String userId, String finalRedirect) {

                SignInResource signInResource = new SignInResource();
                signInResource.setSignInLink(
                    String.format("https://fake.com/oauthsignin/%s/%s/%s",
                    connectionName,
                    turnContext.getActivity().getChannelId(),
                    userId));
                TokenExchangeResource tokenExchangeResource = new TokenExchangeResource();
                tokenExchangeResource.setId(UUID.randomUUID().toString());
                tokenExchangeResource.setProviderId(null);
                tokenExchangeResource.setUri(String.format("api://%s/resource", connectionName));
                signInResource.setTokenExchangeResource(tokenExchangeResource);
                return CompletableFuture.completedFuture(signInResource);
    }

    @Override
    public CompletableFuture<TokenResponse> exchangeToken(TurnContext turnContext, String connectionName, String userId,
            TokenExchangeRequest exchangeRequest) {
        return exchangeToken(turnContext, null, connectionName, userId, exchangeRequest);
    }

    @Override
    public CompletableFuture<TokenResponse> exchangeToken(TurnContext turnContext, AppCredentials oAuthAppCredentials,
            String connectionName, String userId, TokenExchangeRequest exchangeRequest) {

            String exchangableValue = null;
            if (exchangeRequest.getToken() != null) {
                if (StringUtils.isNotBlank(exchangeRequest.getToken())) {
                    exchangableValue = exchangeRequest.getToken();
                }
            } else {
                if (exchangeRequest.getUri() != null) {
                    exchangableValue = exchangeRequest.getUri();
                }
            }

            ExchangableTokenKey key = new ExchangableTokenKey();
            if (turnContext != null
                && turnContext.getActivity() != null
                && turnContext.getActivity().getChannelId() != null) {
                key.setChannelId(turnContext.getActivity().getChannelId());
            }
            key.setConnectionName(connectionName);
            key.setExchangableItem(exchangableValue);
            key.setUserId(userId);

            String token = exchangableToken.get(key);
            if (token != null) {
                if (token.equals(exceptionExpected)) {
                    return Async.completeExceptionally(
                        new RuntimeException("Exception occurred during exchanging tokens")
                    );
                }
                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setChannelId(key.getChannelId());
                tokenResponse.setConnectionName(key.getConnectionName());
                tokenResponse.setToken(token);
                return CompletableFuture.completedFuture(tokenResponse);
            } else {
                return CompletableFuture.completedFuture(null);
            }

    }

    class ExchangableTokenKey extends UserTokenKey {

        private String exchangableItem = "";

        public String getExchangableItem() {
            return exchangableItem;
        }

        public void setExchangableItem(String withExchangableItem) {
            exchangableItem = withExchangableItem;
        }

        @Override
        public boolean equals(Object rhs) {
            if (!(rhs instanceof ExchangableTokenKey)) {
                return false;
            }
            return StringUtils.equals(exchangableItem, ((ExchangableTokenKey) rhs).exchangableItem)
                && super.equals(rhs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(exchangableItem != null ? exchangableItem : "") + super.hashCode();
        }


    }

}
