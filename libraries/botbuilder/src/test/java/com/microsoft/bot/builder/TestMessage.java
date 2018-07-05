package com.microsoft.bot.builder;

import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.ActivityTypes;
import com.microsoft.bot.schema.models.ChannelAccount;
import com.microsoft.bot.schema.models.ConversationAccount;

public class TestMessage {
    public static ActivityImpl Message() {
        return TestMessage.Message("1234");
    }

    public static ActivityImpl Message(String id) {
        ActivityImpl a = new ActivityImpl()
                .withType(ActivityTypes.MESSAGE)
                .withId(id)
                .withText("test")
                .withFrom(new ChannelAccount()
                        .withId("user")
                        .withName("User Name"))
                .withRecipient(new ChannelAccount()
                        .withId("bot")
                        .withName("Bot Name"))
                .withConversation(new ConversationAccount()
                        .withId("convo")
                        .withName("Convo Name"))
                .withChannelId("UnitTest")
                .withServiceUrl("https://example.org");
        return a;
    }

}
