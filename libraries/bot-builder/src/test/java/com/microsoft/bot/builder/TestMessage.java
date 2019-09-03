package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;

public class TestMessage {
    public static Activity Message() {
        return TestMessage.Message("1234");
    }

    public static Activity Message(String id) {
        Activity a = new Activity(ActivityTypes.MESSAGE) {{
            setId(id);
            setText("test");
            setFrom(new ChannelAccount("user", "User Name"));
            setRecipient(new ChannelAccount("bot", "Bot Name"));
            setConversation(new ConversationAccount() {{
                setId("convo");
                setName("Convo Name");
            }});
            setChannelId("UnitTest");
            setServiceUrl("https://example.org");
        }};
        return a;
    }

}
