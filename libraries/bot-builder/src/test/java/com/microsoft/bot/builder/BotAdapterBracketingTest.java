// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BotAdapterBracketingTest {
    @Test
    public void Middleware_BracketingValidation() {
        TestAdapter adapter = new TestAdapter().use(new BeforeAfterMiddleware());

        BotCallbackHandler echo = (turnContext -> {
            String toEcho = "ECHO:" + turnContext.getActivity().getText();
            return turnContext.sendActivity(
                turnContext.getActivity().createReply(toEcho)
            ).thenApply(resourceResponse -> null);
        });

        new TestFlow(adapter, echo).send("test").assertReply("BEFORE").assertReply(
            "ECHO:test"
        ).assertReply("AFTER").startTest().join();
    }

    @Test
    public void Middleware_ThrowException() {
        String uniqueId = UUID.randomUUID().toString();

        TestAdapter adapter = new TestAdapter().use(new CatchExceptionMiddleware());

        BotCallbackHandler echoWithException = (turnContext -> {
            String toEcho = "ECHO:" + turnContext.getActivity().getText();
            return turnContext.sendActivity(
                turnContext.getActivity().createReply(toEcho)
            ).thenCompose(resourceResponse -> {
                CompletableFuture<Void> result = new CompletableFuture();
                result.completeExceptionally(new RuntimeException(uniqueId));
                return result;
            });
        });

        new TestFlow(adapter, echoWithException).send("test").assertReply("BEFORE").assertReply(
            "ECHO:test"
        ).assertReply("CAUGHT: " + uniqueId).assertReply("AFTER").startTest().join();
    }

    private static class CatchExceptionMiddleware implements Middleware {
        @Override
        public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
            return turnContext.sendActivity(
                turnContext.getActivity().createReply("BEFORE")
            ).thenCompose(resourceResponse -> next.next()).exceptionally(exception -> {
                turnContext.sendActivity(
                    turnContext.getActivity().createReply(
                        "CAUGHT: " + exception.getCause().getMessage()
                    )
                ).join();
                return null;
            }).thenCompose(
                result -> turnContext.sendActivity(turnContext.getActivity().createReply("AFTER")).thenApply(resourceResponse -> null)
            );
        }
    }

    private static class BeforeAfterMiddleware implements Middleware {
        @Override
        public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
            return turnContext.sendActivity(
                turnContext.getActivity().createReply("BEFORE")
            ).thenCompose(result -> next.next()).thenCompose(
                result -> turnContext.sendActivity(turnContext.getActivity().createReply("AFTER")).thenApply(resourceResponse -> null)
            );
        }
    }
}
