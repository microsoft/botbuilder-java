// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class ShowTypingMiddlewareTests {
    @Test
    public void ShowTyping_TestMiddleware_1_Second_Interval() {
        TestAdapter adapter = new TestAdapter().use(new ShowTypingMiddleware(100, 1000));

        new TestFlow(adapter, (turnContext -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                // do nothing
            }

            Assert.assertFalse(turnContext.getResponded());

            turnContext.sendActivity("Message send after delay").join();
            return CompletableFuture.completedFuture(null);
        })
        ).send("foo").assertReply(this::validateTypingActivity).assertReply(
            this::validateTypingActivity
        ).assertReply(this::validateTypingActivity).assertReply(
            "Message send after delay"
        ).startTest().join();
    }

    @Test
    public void ShowTyping_TestMiddleware_Context_Completes_Before_Typing_Interval() {
        TestAdapter adapter = new TestAdapter().use(new ShowTypingMiddleware(100, 5000));

        new TestFlow(adapter, (turnContext -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // do nothing
            }

            turnContext.sendActivity("Message send after delay").join();
            return CompletableFuture.completedFuture(null);
        })
        ).send("foo").assertReply(this::validateTypingActivity).assertReply(
            "Message send after delay"
        ).startTest().join();
    }

    @Test
    public void ShowTyping_TestMiddleware_ImmediateResponse_5SecondInterval() {
        TestAdapter adapter = new TestAdapter().use(new ShowTypingMiddleware(2000, 5000));

        new TestFlow(adapter, (turnContext -> {
            turnContext.sendActivity("Message send after delay").join();
            return CompletableFuture.completedFuture(null);
        })).send("foo").assertReply("Message send after delay").startTest().join();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShowTyping_TestMiddleware_NegativeDelay() {
        TestAdapter adapter = new TestAdapter().use(new ShowTypingMiddleware(-100, 5000));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShowTyping_TestMiddleware_ZeroFrequency() {
        TestAdapter adapter = new TestAdapter().use(new ShowTypingMiddleware(-100, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ShowTyping_TestMiddleware_NegativePerion() {
        TestAdapter adapter = new TestAdapter().use(new ShowTypingMiddleware(500, -500));
    }

    private void validateTypingActivity(Activity obj) {
        if (!obj.isType(ActivityTypes.TYPING)) {
            throw new RuntimeException("Activity was not of type TypingActivity");
        }
    }
}
