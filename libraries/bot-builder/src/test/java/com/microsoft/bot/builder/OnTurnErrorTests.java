// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class OnTurnErrorTests {
    @Test
    public void OnTurnError_Test() {
        TestAdapter adapter = new TestAdapter();
        adapter.setOnTurnError(((turnContext, exception) -> {
            if (exception instanceof NotImplementedException) {
                return turnContext.sendActivity(
                    turnContext.getActivity().createReply(exception.getMessage())
                ).thenApply(resourceResponse -> null);
            } else {
                return turnContext.sendActivity("Unexpected exception").thenApply(
                    resourceResponse -> null
                );
            }
        }));

        new TestFlow(adapter, (turnContext -> {
            if (StringUtils.equals(turnContext.getActivity().getText(), "foo")) {
                turnContext.sendActivity(turnContext.getActivity().getText()).join();
            }

            if (
                StringUtils.equals(turnContext.getActivity().getText(), "NotImplementedException")
            ) {
                CompletableFuture<Void> result = new CompletableFuture<>();
                result.completeExceptionally(new NotImplementedException("Test"));
                return result;
            }

            return CompletableFuture.completedFuture(null);
        })
        ).send("foo").assertReply("foo", "passthrough").send("NotImplementedException").assertReply(
            "Test"
        ).startTest().join();
    }
}
