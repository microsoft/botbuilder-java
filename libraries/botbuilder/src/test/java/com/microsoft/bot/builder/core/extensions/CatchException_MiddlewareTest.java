package com.microsoft.bot.builder.core.extensions;

import com.microsoft.bot.builder.core.TurnContext;
import com.microsoft.bot.builder.core.TurnContextImpl;
import com.microsoft.bot.builder.core.adapters.TestAdapter;
import com.microsoft.bot.builder.core.adapters.TestFlow;
import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.Activity;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class CatchException_MiddlewareTest {

    @Test
    public void CatchException_TestMiddleware_TestStackedErrorMiddleware() throws ExecutionException, InterruptedException {

        TestAdapter adapter = new TestAdapter()
                // Add middleware to catch general exceptions

                .Use(new CatchExceptionMiddleware<Exception>(new CallOnException() {
                    @Override
                    public <T> CompletableFuture apply(TurnContext context, T t) throws Exception {
                        ActivityImpl activity = context.getActivity();
                        context.SendActivity(activity.CreateReply(t.toString()));
                        return completedFuture(null);
                    }
                }, Exception.class))
                // Add middleware to catch NullReferenceExceptions before throwing up to the general exception instance
                .Use(new CatchExceptionMiddleware<NullPointerException>(new CallOnException() {
                    @Override
                    public <T> CompletableFuture apply(TurnContext context, T t) throws Exception {
                        context.SendActivity("Sorry - Null Reference Exception");
                        return CompletableFuture.completedFuture(null);
                    }
                }, NullPointerException.class));


        await(new TestFlow(adapter, (context) ->
        {
            CompletableFuture doit = CompletableFuture.runAsync(() -> {
                if (context.getActivity().text() == "foo") {
                    try {
                        context.SendActivity(context.getActivity().text());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (context.getActivity().text() == "UnsupportedOperationException") {
                    throw new UnsupportedOperationException("Test");
                }
            });

            return doit;
        }
        )
                .Send("foo")
                .AssertReply("foo", "passthrough")
                .Send("UnsupportedOperationException")
                .AssertReply("Test")
                .StartTest());

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
