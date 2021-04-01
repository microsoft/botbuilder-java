// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;

public final class TestUtilities {
    public static TurnContext createEmptyContext() {
        TestAdapter adapter = new TestAdapter();
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setChannelId("EmptyContext");
        ConversationAccount conversation = new ConversationAccount();
        conversation.setId("test");
        activity.setConversation(conversation);
        ChannelAccount from = new ChannelAccount();
        from.setId("empty@empty.context.org");
        activity.setFrom(from);

        return new TurnContextImpl(adapter, activity);
    }
}
