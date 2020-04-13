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
        Activity activity = new Activity() {
            {
                setType(ActivityTypes.MESSAGE);
                setChannelId("EmptyContext");
                setConversation(new ConversationAccount() {
                    {
                        setId("test");
                    }
                });
                setFrom(new ChannelAccount() {
                    {
                        setId("empty@empty.context.org");
                    }
                });
            }
        };

        return new TurnContextImpl(adapter, activity);
    }
}
