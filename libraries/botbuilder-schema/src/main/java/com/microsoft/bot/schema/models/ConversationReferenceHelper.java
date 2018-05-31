package com.microsoft.bot.schema.models;

import java.util.UUID;

public class ConversationReferenceHelper {
    private ConversationReference reference;
    public ConversationReferenceHelper(ConversationReference reference) {
        this.reference = reference;
    }
    /// <summary>
    /// Creates <see cref="Activity"/> from conversation reference as it is posted to bot.
    /// </summary>
    public Activity GetPostToBotMessage()
    {
        return new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withId(UUID.randomUUID().toString())
                .withRecipient(new ChannelAccount()
                        .withId((reference.bot().id()))
                        .withName(reference.bot().name()))
                .withChannelId(reference.channelId())
                .withServiceUrl(reference.serviceUrl())
                .withConversation(new ConversationAccount()
                        .withId(reference.conversation().id())
                        .withIsGroup(reference.conversation().isGroup())
                        .withName(reference.conversation().name()))
                .withFrom(new ChannelAccount()
                    .withId(reference.user().id())
                    .withName(reference.user().name()));
    }

    /// <summary>
    /// Creates <see cref="Activity"/> from conversation reference that can be posted to user as reply.
    /// </summary>
    public Activity GetPostToUserMessage()
    {
        Activity msg = this.GetPostToBotMessage();

        // swap from and recipient
        ChannelAccount bot = msg.recipient();
        ChannelAccount user = msg.from();
        msg.withFrom(bot);
        msg.withRecipient(user);
        return msg;
    }
}


