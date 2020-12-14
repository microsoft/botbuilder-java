// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.adapters;

import com.microsoft.bot.builder.*;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.UserToken;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TestAdapter extends BotAdapter {
    private final Queue<Activity> botReplies = new LinkedList<>();
    private int nextId = 0;
    private ConversationReference conversationReference;

    private static class UserTokenKey {
        public String connectionName;
        public String userId;
        public String channelId;

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
        setConversationReference(new ConversationReference() {
            {
                setChannelId(channelId);
                setServiceUrl("https://test.com");
                setUser(new ChannelAccount() {
                    {
                        setId("user1");
                        setName("User1");
                    }
                });
                setBot(new ChannelAccount() {
                    {
                        setId("bot");
                        setName("Bot");
                    }
                });
                setConversation(new ConversationAccount() {
                    {
                        setIsGroup(false);
                        setConversationType("convo1");
                        setId("Conversation1");
                    }
                });
            }
        });
    }

    public TestAdapter(ConversationReference reference) {
        if (reference != null) {
            setConversationReference(reference);
        } else {
            setConversationReference(new ConversationReference() {
                {
                    setChannelId(Channels.TEST);
                    setServiceUrl("https://test.com");
                    setUser(new ChannelAccount() {
                        {
                            setId("user1");
                            setName("User1");
                        }
                    });
                    setBot(new ChannelAccount() {
                        {
                            setId("bot");
                            setName("Bot");
                        }
                    });
                    setConversation(new ConversationAccount() {
                        {
                            setIsGroup(false);
                            setConversationType("convo1");
                            setId("Conversation1");
                        }
                    });
                }
            });
        }
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
     * Adds middleware to the adapter to register an Storage object on the turn context.
     * The middleware registers the state objects on the turn context at the start of each turn.
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
     * Adds middleware to the adapter to register one or more BotState objects on the turn context.
     * The middleware registers the state objects on the turn context at the start of each turn.
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
                activity.setChannelId(conversationReference().getChannelId());

                if (activity.getFrom() == null
                    || StringUtils.equalsIgnoreCase(activity.getFrom().getId(), "unknown")
                    || activity.getFrom().getRole() == RoleTypes.BOT
                ) {
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
    public CompletableFuture<ResourceResponse[]> sendActivities(
        TurnContext context,
        List<Activity> activities
    ) {
        List<ResourceResponse> responses = new LinkedList<ResourceResponse>();

        for (Activity activity : activities) {
            if (StringUtils.isEmpty(activity.getId()))
                activity.setId(UUID.randomUUID().toString());

            if (activity.getTimestamp() == null)
                activity.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));

            responses.add(new ResourceResponse(activity.getId()));

            System.out.println(
                String.format(
                    "TestAdapter:SendActivities, Count:%s (tid:%s)",
                    activities.size(),
                    Thread.currentThread().getId()
                )
            );
            for (Activity act : activities) {
                System.out.printf(" :--------\n : To:%s\n", act.getRecipient().getName());
                System.out.printf(
                    " : From:%s\n",
                    (act.getFrom() == null) ? "No from set" : act.getFrom().getName()
                );
                System.out.printf(
                    " : Text:%s\n :---------\n",
                    (act.getText() == null) ? "No text set" : act.getText()
                );
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
            } else {
                synchronized (botReplies) {
                    botReplies.add(activity);
                }
            }
        }
        return CompletableFuture.completedFuture(
            responses.toArray(new ResourceResponse[responses.size()])
        );
    }

    @Override
    public CompletableFuture<ResourceResponse> updateActivity(
        TurnContext context,
        Activity activity
    ) {
        synchronized (botReplies) {
            List<Activity> replies = new ArrayList<>(botReplies);
            for (int i = 0; i < botReplies.size(); i++) {
                if (replies.get(i).getId().equals(activity.getId())) {
                    replies.set(i, activity);
                    botReplies.clear();

                    for (Activity item : replies) {
                        botReplies.add(item);
                    }
                    return CompletableFuture.completedFuture(
                        new ResourceResponse(activity.getId())
                    );
                }
            }
        }
        return CompletableFuture.completedFuture(new ResourceResponse());
    }

    @Override
    public CompletableFuture<Void> deleteActivity(
        TurnContext context,
        ConversationReference reference
    ) {
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
        Activity activity = new Activity(ActivityTypes.MESSAGE) {
            {
                setFrom(conversationReference().getUser());
                setRecipient(conversationReference().getBot());
                setConversation(conversationReference().getConversation());
                setServiceUrl(conversationReference().getServiceUrl());
                setId(next.toString());
                setText(withText);
            }
        };

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

    public void addUserToken(
        String connectionName,
        String channelId,
        String userId,
        String token,
        String withMagicCode
    ) {
        UserTokenKey userKey = new UserTokenKey();
        userKey.connectionName = connectionName;
        userKey.channelId = channelId;
        userKey.userId = userId;

        if (withMagicCode == null) {
            userTokens.put(userKey, token);
        } else {
            magicCodes.add(new TokenMagicCode() {
                {
                    key = userKey;
                    magicCode = withMagicCode;
                    userToken = token;
                }
            });
        }
    }

    public CompletableFuture<TokenResponse> getUserToken(
        TurnContext turnContext,
        String connectionName,
        String magicCode
    ) {
        UserTokenKey key = new UserTokenKey();
        key.connectionName = connectionName;
        key.channelId = turnContext.getActivity().getChannelId();
        key.userId = turnContext.getActivity().getFrom().getId();

        if (magicCode != null) {
            TokenMagicCode magicCodeRecord = magicCodes.stream().filter(
                tokenMagicCode -> key.equals(tokenMagicCode.key)
            ).findFirst().orElse(null);
            if (
                magicCodeRecord != null && StringUtils.equals(magicCodeRecord.magicCode, magicCode)
            ) {
                addUserToken(
                    connectionName,
                    key.channelId,
                    key.userId,
                    magicCodeRecord.userToken,
                    null
                );
            }
        }

        if (userTokens.containsKey(key)) {
            return CompletableFuture.completedFuture(new TokenResponse() {
                {
                    setConnectionName(connectionName);
                    setToken(userTokens.get(key));
                }
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<String> getOAuthSignInLink(
        TurnContext turnContext,
        String connectionName
    ) {
        return getOAuthSignInLink(
            turnContext,
            connectionName,
            turnContext.getActivity().getFrom().getId(),
            null
        );
    }

    public CompletableFuture<String> getOAuthSignInLink(
        TurnContext turnContext,
        String connectionName,
        String userId,
        String finalRedirect
    ) {
        String link = String.format(
            "https://fake.com/oauthsignin/%s/{turnContext.Activity.ChannelId}/%s",
            connectionName,
            userId == null ? "" : userId
        );
        return CompletableFuture.completedFuture(link);
    }

    public CompletableFuture<Void> signOutUser(
        TurnContext turnContext,
        String connectionName,
        String userId
    ) {
        String channelId = turnContext.getActivity().getChannelId();
        final String effectiveUserId = userId == null
            ? turnContext.getActivity().getFrom().getId()
            : userId;

        userTokens.keySet().stream().filter(
            t -> StringUtils.equals(t.channelId, channelId) && StringUtils.equals(t.userId, effectiveUserId) && connectionName == null || StringUtils.equals(t.connectionName, connectionName)
        ).collect(Collectors.toList()).forEach(key -> userTokens.remove(key));

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<TokenStatus[]> getTokenStatus(
        TurnContext turnContext,
        String userId,
        String includeFilter
    ) {
        String[] filter = includeFilter == null ? null : includeFilter.split(",");
        List<TokenStatus> records = userTokens.keySet().stream().filter(
            x -> StringUtils.equals(x.channelId, turnContext.getActivity().getChannelId()) && StringUtils.equals(x.userId, turnContext.getActivity().getFrom().getId()) && (includeFilter == null || Arrays.binarySearch(filter, x.connectionName) != -1)
        ).map(r -> new TokenStatus() {
            {
                setConnectionName(r.connectionName);
                setHasToken(true);
                setServiceProviderDisplayName(r.connectionName);
            }
        }).collect(Collectors.toList());

        if (records.size() > 0)
            return CompletableFuture.completedFuture(records.toArray(new TokenStatus[0]));
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        TurnContext turnContext,
        String connectionName,
        String[] resourceUrls,
        String userId
    ) {
        return CompletableFuture.completedFuture(new HashMap<>());
    }

    public static ConversationReference createConversationReference(String name, String user, String bot) {
        ConversationReference reference = new ConversationReference();
        reference.setChannelId("test");
        reference.setServiceUrl("https://test.com");
        reference.setConversation(new ConversationAccount(false, name, name, null, null, null, null));
        reference.setUser(new ChannelAccount(user.toLowerCase(), user.toLowerCase()));
        reference.setBot(new ChannelAccount(bot.toLowerCase(), bot.toLowerCase()));
        return reference;
    }
}
