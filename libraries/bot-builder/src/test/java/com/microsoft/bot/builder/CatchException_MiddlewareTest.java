// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.schema.Activity;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CatchException_MiddlewareTest {

    @Test
    public void CatchException_TestMiddleware_TestStackedErrorMiddleware() throws ExecutionException, InterruptedException {

        TestAdapter adapter = new TestAdapter()
            .use(new CatchExceptionMiddleware<Exception>(new CallOnException() {
                @Override
                public <T> CompletableFuture apply(TurnContext context, T t) {
                    return CompletableFuture.runAsync(() -> {
                        Activity activity = context.getActivity();
                        if (activity instanceof Activity) {
                            try {
                                context.sendActivity(activity.createReply(t.toString())).join();
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(String.format("CatchException_TestMiddleware_TestStackedErrorMiddleware:SendActivity failed %s", e.toString()));
                            }
                        } else
                            Assert.assertTrue("Test was built for ActivityImpl", false);

                    }, ExecutorFactory.getExecutor());

                }
            }, Exception.class))
            // Add middleware to catch NullReferenceExceptions before throwing up to the general exception instance
            .use(new CatchExceptionMiddleware<NullPointerException>(new CallOnException() {
                @Override
                public <T> CompletableFuture apply(TurnContext context, T t) {
                    context.sendActivity("Sorry - Null Reference Exception").join();
                    return CompletableFuture.completedFuture(null);
                }
            }, NullPointerException.class));


        new TestFlow(adapter, (context) -> {
            if (StringUtils.equals(context.getActivity().getText(), "foo")) {
                try {
                    context.sendActivity(context.getActivity().getText()).join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (StringUtils.equals(context.getActivity().getText(), "UnsupportedOperationException")) {
                throw new UnsupportedOperationException("Test");
            }

            return null;
        })
            .send("foo")
            .assertReply("foo", "passthrough")
            .send("UnsupportedOperationException")
            .assertReply("Test")
            .startTest();

    }

/*        @Test
        // [TestCategory("Middleware")]
    public void CatchException_TestMiddleware_SpecificExceptionType()
{
    TestAdapter adapter = new TestAdapter()
            .Use(new CatchExceptionMiddleware<Exception>((context, exception) =>
            {
                    context.SendActivity("Generic Exception Caught");
    return CompletableFuture.CompletedTask;
                }))
                .Use(new CatchExceptionMiddleware<NullReferenceException>((context, exception) =>
        {
                context.SendActivity(exception.Message);
    return CompletableFuture.CompletedTask;
                }));


    await new TestFlow(adapter, (context) =>
        {
    if (context.Activity.AsMessageActivity().Text == "foo")
    {
        context.SendActivity(context.Activity.AsMessageActivity().Text);
    }

    if (context.Activity.AsMessageActivity().Text == "NullReferenceException")
    {
        throw new NullReferenceException("Test");
    }

    return CompletableFuture.CompletedTask;
                })
                .Send("foo")
        .AssertReply("foo", "passthrough")
        .Send("NullReferenceException")
        .AssertReply("Test")
        .StartTest();
}*/
}
