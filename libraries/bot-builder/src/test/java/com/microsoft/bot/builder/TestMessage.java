// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
        Activity a = new Activity(ActivityTypes.MESSAGE);
        a.setId(id);
        a.setText("test");
        a.setFrom(new ChannelAccount("user", "User Name"));
        a.setRecipient(new ChannelAccount("bot", "Bot Name"));
        ConversationAccount conversationAccount = new ConversationAccount();
        conversationAccount.setId("convo");
        conversationAccount.setName("Convo Name");
        a.setConversation(conversationAccount);
        a.setChannelId("UnitTest");
        a.setServiceUrl("https://example.org");
        return a;
    }
}
