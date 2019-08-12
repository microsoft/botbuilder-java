package com.microsoft.bot.schema.models;

import com.microsoft.bot.schema.ActivityImpl;

import java.util.UUID;

public class ConversationReferenceHelper {
    private ConversationReference reference;
    public ConversationReferenceHelper(ConversationReference reference) {
        this.reference = reference;
    }
    /**
     * Creates {@link Activity} from conversation reference as it is posted to bot.
     */
    public ActivityImpl GetPostToBotMessage()
    {
        return (ActivityImpl) new ActivityImpl()
                .withType(ActivityTypes.MESSAGE)
                .withId(UUID.randomUUID().toString())
                .withRecipient(new ChannelAccount()
                        .withId(reference.bot().id())
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

    /**
     * Creates {@link Activity} from conversation reference that can be posted to user as reply.
     */
    public ActivityImpl GetPostToUserMessage()
    {
        Activity msg = this.GetPostToBotMessage();

        // swap from and recipient
        ChannelAccount bot = msg.recipient();
        ChannelAccount user = msg.from();
        msg.withFrom(bot);
        msg.withRecipient(user);
        return (ActivityImpl) msg;
    }
}


