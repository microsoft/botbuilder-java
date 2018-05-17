package com.microsoft.bot.builder.core;

import com.ea.async.Async;
import com.microsoft.bot.builder.core.base.TestBase;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.ChannelAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;


// [TestCategory("Russian Doll Middleware, Nested Middleware sets")]
public class MiddlewareSetTests extends TestBase
{
    protected ConnectorClientImpl connector;
    protected ChannelAccount bot;
    protected ChannelAccount user;
    private boolean innerOnreceiveCalled;

    public MiddlewareSetTests() {
        super(RunCondition.BOTH);
    }

    @Override
    protected void initializeClients(RestClient restClient, String botId, String userId) {
        // Initialize async/await support
        Async.init();

        connector = new ConnectorClientImpl(restClient);
        bot = new ChannelAccount().withId(botId);
        user = new ChannelAccount().withId(userId);

        // Test-specific stuff
        innerOnreceiveCalled = false;
    }

    @Override
    protected void cleanUpResources() {
    }


    @Test
    public void NoMiddleware()  {
        MiddlewareSet m = new MiddlewareSet();
        // No middleware. Should not explode.
        try {
            m.ReceiveActivity(null);
            Assert.assertTrue(true);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Assert.fail("No exception expected" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail("No exception expected" + e.getMessage());
        }
    }

/*
    public async Task NestedSet_OnReceive()
    {
        bool innerOnReceiveCalled = false;

        MiddlewareSet inner = new MiddlewareSet();
        inner.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            innerOnReceiveCalled = true;
            await next();
        }));

        MiddlewareSet outer = new MiddlewareSet();
        outer.Use(inner);

        await outer.ReceiveActivity(null);

        Assert.IsTrue(innerOnReceiveCalled, "Inner Middleware Receive was not called.");
    }

*/

    @Test
    public void NestedSet_OnReceive()
    {
        final boolean[] wasCalled = {false};
        MiddlewareSet inner = new MiddlewareSet();
        inner.Use(new AnonymousReceiveMiddleware(new MiddlewareCall() {
            public CompletableFuture<Boolean> requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                wasCalled[0] = true;
                await(nd.next());
                return completedFuture(true);
            }
        }));
        MiddlewareSet outer = new MiddlewareSet();
        outer.Use(inner);
        try {
            await(outer.ReceiveActivity(null));
        } catch (ExecutionException e) {
            Assert.fail(e.getMessage());
            return;
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
            return;
        }

        Assert.assertTrue("Inner Middleware Receive was not called.",  wasCalled[0]);
    }


    @Test
    public void NoMiddlewareWithDelegate() throws ExecutionException, InterruptedException {
        MiddlewareSet m = new MiddlewareSet();
        final boolean wasCalled[] = {false};
        TurnTask tt = new TurnTask() {
            @Override
            public CompletableFuture invoke(TurnContext context) {
                wasCalled[0] = true;
                return CompletableFuture.completedFuture(null);
            }
        };
        // No middleware. Should not explode.
        await(m.ReceiveActivityWithStatus(null, tt));
        Assert.assertTrue("Delegate was not called", wasCalled[0]);
    }

    @Test
    public void OneMiddlewareItem() throws ExecutionException, InterruptedException {
        WasCalledMiddlware simple = new WasCalledMiddlware();

        final boolean wasCalled[] = {false};
        TurnTask tt = new TurnTask() {
            @Override
            public CompletableFuture invoke(TurnContext context) {
                wasCalled[0] = true;
                return CompletableFuture.completedFuture(null);
            }
        };

        MiddlewareSet m = new MiddlewareSet();
        m.Use(simple);

        Assert.assertFalse(simple.getCalled());
        await(m.ReceiveActivityWithStatus(null, tt));
        Assert.assertTrue(simple.getCalled());
        Assert.assertTrue( "Delegate was not called", wasCalled[0]);
    }
/*
    @Test
    public CompletableFuture OneMiddlewareItemWithDelegate()
    {
        WasCalledMiddlware simple = new WasCalledMiddlware();

        MiddlewareSet m = new MiddlewareSet();
        m.Use(simple);

        Assert.IsFalse(simple.Called);
        await m.ReceiveActivity(null);
        Assert.IsTrue(simple.Called);
    }

    @Test
    [ExpectedException(typeof(InvalidOperationException))]
    public CompletableFuture BubbleUncaughtException()
    {
        MiddlewareSet m = new MiddlewareSet();
        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            throw new InvalidOperationException("test");
        }));

        await m.ReceiveActivity(null);
        Assert.Fail("Should never have gotten here");
    }

    @Test
    public CompletableFuture TwoMiddlewareItems()
    {
        WasCalledMiddlware one = new WasCalledMiddlware();
        WasCalledMiddlware two = new WasCalledMiddlware();

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        await m.ReceiveActivity(null);
        Assert.IsTrue(one.Called);
        Assert.IsTrue(two.Called);
    }

    @Test
    public CompletableFuture TwoMiddlewareItemsWithDelegate()
    {
        WasCalledMiddlware one = new WasCalledMiddlware();
        WasCalledMiddlware two = new WasCalledMiddlware();

        int called = 0;
        CompletableFuture CallMe(ITurnContext context)
        {
            called++;
        }

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        await m.ReceiveActivityWithStatus(null, CallMe);
        Assert.IsTrue(one.Called);
        Assert.IsTrue(two.Called);
        Assert.IsTrue(called == 1, "Incorrect number of calls to Delegate");
    }

    @Test
    public CompletableFuture TwoMiddlewareItemsInOrder()
    {
        bool called1 = false;
        bool called2 = false;

        CallMeMiddlware one = new CallMeMiddlware(() =>
        {
            Assert.IsFalse(called2, "Second Middleware was called");
            called1 = true;
        });

        CallMeMiddlware two = new CallMeMiddlware(() =>
        {
            Assert.IsTrue(called1, "First Middleware was not called");
            called2 = true;
        });

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        await m.ReceiveActivity(null);
        Assert.IsTrue(called1);
        Assert.IsTrue(called2);
    }

    @Test
    public CompletableFuture Status_OneMiddlewareRan()
    {
        bool called1 = false;

        CallMeMiddlware one = new CallMeMiddlware(() => { called1 = true; });

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);

        // The middlware in this pipeline calls next(), so the delegate should be called
        bool didAllRun = false;
        await m.ReceiveActivityWithStatus(null, async (ctx) => didAllRun = true);

        Assert.IsTrue(called1);
        Assert.IsTrue(didAllRun);
    }

    @Test
    public CompletableFuture Status_RunAtEndEmptyPipeline()
    {
        MiddlewareSet m = new MiddlewareSet();
        bool didAllRun = false;

        // This middlware pipeline has no entries. This should result in
        // the status being TRUE.
        await m.ReceiveActivityWithStatus(null, async (ctx) => didAllRun = true);
        Assert.IsTrue(didAllRun);

    }

    @Test
    public CompletableFuture Status_TwoItemsOneDoesNotCallNext()
    {
        bool called1 = false;
        bool called2 = false;

        CallMeMiddlware one = new CallMeMiddlware(() =>
        {
            Assert.IsFalse(called2, "Second Middleware was called");
            called1 = true;
        });

        DoNotCallNextMiddleware two = new DoNotCallNextMiddleware(() =>
        {
            Assert.IsTrue(called1, "First Middleware was not called");
            called2 = true;
        });

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        bool didAllRun = false;
        await m.ReceiveActivityWithStatus(null, async (ctx) => didAllRun = true);
        Assert.IsTrue(called1);
        Assert.IsTrue(called2);

        // The 2nd middleware did not call next, so the "final" action should not have run.
        Assert.IsFalse(didAllRun);
    }

    @Test
    public CompletableFuture Status_OneEntryThatDoesNotCallNext()
    {
        bool called1 = false;

        DoNotCallNextMiddleware one = new DoNotCallNextMiddleware(() => { called1 = true; });

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);

        // The middlware in this pipeline DOES NOT call next(), so this must not be called
        bool didAllRun = false;
        await m.ReceiveActivityWithStatus(null, async (ctx) => didAllRun = true);

        Assert.IsTrue(called1);

        // Our "Final" action MUST NOT have been called, as the Middlware Pipeline
        // didn't complete.
        Assert.IsFalse(didAllRun);
    }

    @Test
    public CompletableFuture AnonymousMiddleware()
    {
        bool didRun = false;

        MiddlewareSet m = new MiddlewareSet();
        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            didRun = true;
            await next();
        }));

        Assert.IsFalse(didRun);
        await m.ReceiveActivity(null);
        Assert.IsTrue(didRun);
    }

    @Test
    public CompletableFuture TwoAnonymousMiddleware()
    {
        bool didRun1 = false;
        bool didRun2 = false;

        MiddlewareSet m = new MiddlewareSet();
        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            didRun1 = true;
            await next();
        }));
        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            didRun2 = true;
            await next();
        }));

        await m.ReceiveActivity(null);
        Assert.IsTrue(didRun1);
        Assert.IsTrue(didRun2);
    }

    @Test
    public CompletableFuture TwoAnonymousMiddlewareInOrder()
    {
        bool didRun1 = false;
        bool didRun2 = false;

        MiddlewareSet m = new MiddlewareSet();
        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            Assert.IsFalse(didRun2, "Looks like the 2nd one has already run");
            didRun1 = true;
            await next();
        }));
        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            Assert.IsTrue(didRun1, "Looks like the 1nd one has not yet run");
            didRun2 = true;
            await next();
        }));

        await m.ReceiveActivity(null);
        Assert.IsTrue(didRun1);
        Assert.IsTrue(didRun2);
    }

    @Test
    public CompletableFuture MixedMiddlewareInOrderAnonymousFirst()
    {
        bool didRun1 = false;
        bool didRun2 = false;

        MiddlewareSet m = new MiddlewareSet();

        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            Assert.IsFalse(didRun1, "First middleware already ran");
            Assert.IsFalse(didRun2, "Looks like the second middleware was already run");
            didRun1 = true;
            await next();
            Assert.IsTrue(didRun2, "Second middleware should have completed running");
        }));

        m.Use(
            new CallMeMiddlware(() =>
            {
                Assert.IsTrue(didRun1, "First middleware should have already been called");
                Assert.IsFalse(didRun2, "Second middleware should not have been invoked yet");
                didRun2 = true;
            }));

        await m.ReceiveActivity(null);
        Assert.IsTrue(didRun1);
        Assert.IsTrue(didRun2);
    }

    @Test
    public CompletableFuture MixedMiddlewareInOrderAnonymousLast()
    {
        bool didRun1 = false;
        bool didRun2 = false;

        MiddlewareSet m = new MiddlewareSet();

        m.Use(
            new CallMeMiddlware(() =>
            {
                Assert.IsFalse(didRun1, "First middleware should not have been called yet");
                Assert.IsFalse(didRun2, "Second Middleware should not have been called yet");
                didRun1 = true;
            }));

        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            Assert.IsTrue(didRun1, "First middleware has not been run yet");
            didRun2 = true;
            await next();

        }));

        await m.ReceiveActivity(null);
        Assert.IsTrue(didRun1);
        Assert.IsTrue(didRun2);
    }

    @Test
    public CompletableFuture RunCodeBeforeAndAfter()
    {
        bool didRun1 = false;
        bool codeafter2run = false;
        bool didRun2 = false;

        MiddlewareSet m = new MiddlewareSet();

        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            Assert.IsFalse(didRun1, "Looks like the 1st middleware has already run");
            didRun1 = true;
            await next();
            Assert.IsTrue(didRun1, "The 2nd middleware should have run now.");
            codeafter2run = true;
        }));

        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            Assert.IsTrue(didRun1, "Looks like the 1st middleware has not been run");
            Assert.IsFalse(codeafter2run, "The code that runs after middleware 2 is complete has already run.");
            didRun2 = true;
            await next();
        }));

        await m.ReceiveActivity(null);
        Assert.IsTrue(didRun1);
        Assert.IsTrue(didRun2);
        Assert.IsTrue(codeafter2run);
    }

    @Test
    public CompletableFuture CatchAnExceptionViaMiddlware()
    {
        MiddlewareSet m = new MiddlewareSet();
        bool caughtException = false;

        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            try
            {
                await next();
                Assert.Fail("Should not get here");
            }
            catch (Exception ex)
            {
                Assert.IsTrue(ex.Message == "test");
                caughtException = true;
            }
        }));

        m.Use(new AnonymousReceiveMiddleware(async (context, next) =>
        {
            throw new Exception("test");
        }));

        await m.ReceiveActivity(null);
        Assert.IsTrue(caughtException);
    }

    public class WasCalledMiddlware : IMiddleware
    {
        public bool Called { get; set; } = false;

        public Task OnTurn(ITurnContext context, MiddlewareSet.NextDelegate next)
        {
            Called = true;
            return next();
        }


    }

    public class DoNotCallNextMiddleware : IMiddleware
    {
        private readonly Action _callMe;
        public DoNotCallNextMiddleware(Action callMe)
        {
            _callMe = callMe;
        }
        public Task OnTurn(ITurnContext context, MiddlewareSet.NextDelegate next)
        {
            _callMe();
            // DO NOT call NEXT
            return Task.CompletedTask;
        }


    }

    public class CallMeMiddlware : IMiddleware
    {
        private readonly Action _callMe;
        public CallMeMiddlware(Action callMe)
        {
            _callMe = callMe;
        }
        public Task OnTurn(ITurnContext context, MiddlewareSet.NextDelegate next)
        {
            _callMe();
            return next();
        }

    } */
}
