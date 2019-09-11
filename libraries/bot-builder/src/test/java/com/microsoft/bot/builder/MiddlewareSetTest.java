// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;


import com.microsoft.bot.builder.base.TestBase;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


// [TestCategory("Russian Doll Middleware, Nested Middleware sets")]
public class MiddlewareSetTest extends TestBase {
    protected RestConnectorClient connector;
    protected ChannelAccount bot;
    protected ChannelAccount user;
    private boolean wasCalled;

    public MiddlewareSetTest() {
        super(RunCondition.BOTH);
    }

    @Override
    protected void initializeClients(RestClient restClient, String botId, String userId) {

        connector = new RestConnectorClient(restClient);
        bot = new ChannelAccount(botId);
        user = new ChannelAccount(userId);
    }

    @Override
    protected void cleanUpResources() {
    }


    @Test
    public void NoMiddleware() throws Exception {
        try {
            MiddlewareSet m = new MiddlewareSet();
            // No middleware. Should not explode.
            m.receiveActivityWithStatus(null, null).join();
            Assert.assertTrue(true);
        } catch (Throwable t) {
            Assert.fail("No exception expected" + t.getMessage());
        }
    }


    @Test
    public void NestedSet_OnReceive() {
        wasCalled = false;

        MiddlewareSet inner = new MiddlewareSet();
        inner.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            wasCalled = true;
            return nd.next();
        }));

        MiddlewareSet outer = new MiddlewareSet();
        outer.use(inner);

        outer.receiveActivityWithStatus(null, null).join();

        Assert.assertTrue("Inner Middleware Receive was not called.", wasCalled);
    }

    @Test
    public void NoMiddlewareWithDelegate() throws Exception {
        MiddlewareSet m = new MiddlewareSet();
        wasCalled = false;

        BotCallbackHandler cb = (ctx) -> {
            wasCalled = true;
            return null;
        };

        // No middleware. Should not explode.
        m.receiveActivityWithStatus(null, cb).join();
        Assert.assertTrue("Delegate was not called", wasCalled);
    }

    @Test
    public void OneMiddlewareItem() {
        WasCalledMiddleware simple = new WasCalledMiddleware();

        wasCalled = false;
        BotCallbackHandler cb = (ctx) -> {
            wasCalled = true;
            return null;
        };

        MiddlewareSet m = new MiddlewareSet();
        m.use(simple);

        Assert.assertFalse(simple.getCalled());
        m.receiveActivityWithStatus(null, cb).join();
        Assert.assertTrue(simple.getCalled());
        Assert.assertTrue("Delegate was not called", wasCalled);
    }

    @Test
    public void OneMiddlewareItemWithDelegate() {
        WasCalledMiddleware simple = new WasCalledMiddleware();

        MiddlewareSet m = new MiddlewareSet();
        m.use(simple);

        Assert.assertFalse(simple.getCalled());
        m.receiveActivityWithStatus(null, null).join();
        Assert.assertTrue(simple.getCalled());
    }

    @Test
    public void BubbleUncaughtException() {
        MiddlewareSet m = new MiddlewareSet();
        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            throw new IllegalStateException("test");
        }));

        try {
            m.receiveActivityWithStatus(null, null).join();
            Assert.assertFalse("Should never have gotten here", true);
        } catch (CompletionException ce) {
            Assert.assertTrue(ce.getCause() instanceof IllegalStateException);
        }
    }

    @Test
    public void TwoMiddlewareItems() {
        WasCalledMiddleware one = new WasCalledMiddleware();
        WasCalledMiddleware two = new WasCalledMiddleware();

        MiddlewareSet m = new MiddlewareSet();
        m.use(one);
        m.use(two);

        m.receiveActivityWithStatus(null, null).join();
        Assert.assertTrue(one.getCalled());
        Assert.assertTrue(two.getCalled());
    }

    @Test
    public void TwoMiddlewareItemsWithDelegate() {
        WasCalledMiddleware one = new WasCalledMiddleware();
        WasCalledMiddleware two = new WasCalledMiddleware();

        final int[] called = {0};
        BotCallbackHandler cb = (context) -> {
            called[0]++;
            return null;
        };

        MiddlewareSet m = new MiddlewareSet();
        m.use(one);
        m.use(two);

        m.receiveActivityWithStatus(null, cb).join();
        Assert.assertTrue(one.getCalled());
        Assert.assertTrue(two.getCalled());
        Assert.assertTrue("Incorrect number of calls to Delegate", called[0] == 1);
    }

    @Test
    public void TwoMiddlewareItemsInOrder() throws Exception {
        final boolean[] called1 = {false};
        final boolean[] called2 = {false};

        CallMeMiddleware one = new CallMeMiddleware(() -> {
            Assert.assertFalse("Second Middleware was called", called2[0]);
            called1[0] = true;
        });

        CallMeMiddleware two = new CallMeMiddleware(() -> {
            Assert.assertTrue("First Middleware was not called", called1[0]);
            called2[0] = true;
        });

        MiddlewareSet m = new MiddlewareSet();
        m.use(one);
        m.use(two);

        m.receiveActivityWithStatus(null, null).join();

        Assert.assertTrue(called1[0]);
        Assert.assertTrue(called2[0]);
    }

    @Test
    public void Status_OneMiddlewareRan() {
        final boolean[] called1 = {false};

        CallMeMiddleware one = new CallMeMiddleware(() -> called1[0] = true);

        MiddlewareSet m = new MiddlewareSet();
        m.use(one);

        // The middlware in this pipeline calls next(), so the delegate should be called
        final boolean[] didAllRun = {false};
        BotCallbackHandler cb = (context) -> {
            didAllRun[0] = true;
            return null;
        };
        m.receiveActivityWithStatus(null, cb).join();

        Assert.assertTrue(called1[0]);
        Assert.assertTrue(didAllRun[0]);
    }

    @Test
    public void Status_RunAtEndEmptyPipeline() {
        MiddlewareSet m = new MiddlewareSet();
        final boolean[] didAllRun = {false};
        BotCallbackHandler cb = (context) -> {
            didAllRun[0] = true;
            return null;
        };

        // This middlware pipeline has no entries. This should result in
        // the status being TRUE.
        m.receiveActivityWithStatus(null, cb);

        Assert.assertTrue(didAllRun[0]);

    }

    @Test
    public void Status_TwoItemsOneDoesNotCallNext() {
        final boolean[] called1 = {false};
        final boolean[] called2 = {false};

        CallMeMiddleware one = new CallMeMiddleware(() -> {
            Assert.assertFalse("Second Middleware was called", called2[0]);
            called1[0] = true;
        });

        DoNotCallNextMiddleware two = new DoNotCallNextMiddleware(() -> {
            Assert.assertTrue("First Middleware was not called", called1[0]);
            called2[0] = true;
        });

        MiddlewareSet m = new MiddlewareSet();
        m.use(one);
        m.use(two);

        boolean[] didAllRun = {false};
        BotCallbackHandler cb = (context) -> {
            didAllRun[0] = true;
            return null;
        };

        m.receiveActivityWithStatus(null, cb).join();

        Assert.assertTrue(called1[0]);
        Assert.assertTrue(called2[0]);

        // The 2nd middleware did not call next, so the "final" action should not have run.
        Assert.assertFalse(didAllRun[0]);
    }

    @Test
    public void Status_OneEntryThatDoesNotCallNext() {
        final boolean[] called1 = {false};

        DoNotCallNextMiddleware one = new DoNotCallNextMiddleware(() -> called1[0] = true);

        MiddlewareSet m = new MiddlewareSet();
        m.use(one);

        // The middleware in this pipeline DOES NOT call next(), so this must not be called
        boolean[] didAllRun = {false};
        BotCallbackHandler cb = (context) -> {
            didAllRun[0] = true;
            return null;
        };
        m.receiveActivityWithStatus(null, cb);

        Assert.assertTrue(called1[0]);

        // Our "Final" action MUST NOT have been called, as the Middlware Pipeline
        // didn't complete.
        Assert.assertFalse(didAllRun[0]);
    }

    @Test
    public void AnonymousMiddleware() {
        final boolean[] didRun = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc = (tc, nd) -> {
            didRun[0] = true;
            return nd.next();
        };
        m.use(new AnonymousReceiveMiddleware(mwc));

        Assert.assertFalse(didRun[0]);
        m.receiveActivityWithStatus(null, null).join();
        Assert.assertTrue(didRun[0]);
    }

    @Test
    public void TwoAnonymousMiddleware() throws Exception {
        final boolean[] didRun1 = {false};
        final boolean[] didRun2 = {false};

        MiddlewareSet m = new MiddlewareSet();

        MiddlewareCall mwc1 = (tc, nd) -> {
            didRun1[0] = true;
            return nd.next();
        };
        m.use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = (tc, nd) -> {
            didRun2[0] = true;
            return nd.next();
        };
        m.use(new AnonymousReceiveMiddleware(mwc2));

        m.receiveActivityWithStatus(null, null).join();

        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void TwoAnonymousMiddlewareInOrder() {
        final boolean[] didRun1 = {false};
        final boolean[] didRun2 = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc1 = (tc, nd) -> {
            Assert.assertFalse("Looks like the 2nd one has already run", didRun2[0]);
            didRun1[0] = true;
            return nd.next();
        };
        m.use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = (tc, nd) -> {
            Assert.assertTrue("Looks like the 1nd one has not yet run", didRun1[0]);
            didRun2[0] = true;
            return nd.next();
        };
        m.use(new AnonymousReceiveMiddleware(mwc2));

        m.receiveActivityWithStatus(null, null).join();

        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void MixedMiddlewareInOrderAnonymousFirst() throws Exception {
        final boolean[] didRun1 = {false};
        final boolean[] didRun2 = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc1 = new MiddlewareCall() {
            public CompletableFuture<Void> requestHandler(TurnContext tc, NextDelegate nd) {
                Assert.assertFalse("First middleware already ran", didRun1[0]);
                Assert.assertFalse("Looks like the second middleware was already run", didRun2[0]);
                didRun1[0] = true;
                CompletableFuture<Void> result = nd.next();
                Assert.assertTrue("Second middleware should have completed running", didRun2[0]);
                return result;
            }
        };
        m.use(new AnonymousReceiveMiddleware(mwc1));

        ActionDel act = () -> {
            Assert.assertTrue("First middleware should have already been called", didRun1[0]);
            Assert.assertFalse("Second middleware should not have been invoked yet", didRun2[0]);
            didRun2[0] = true;
        };
        m.use(new CallMeMiddleware(act));

        m.receiveActivityWithStatus(null, null).join();
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void MixedMiddlewareInOrderAnonymousLast() {
        final boolean[] didRun1 = {false};
        final boolean[] didRun2 = {false};

        MiddlewareSet m = new MiddlewareSet();

        ActionDel act = () -> {
            Assert.assertFalse("First middleware should not have already been called", didRun1[0]);
            Assert.assertFalse("Second middleware should not have been invoked yet", didRun2[0]);
            didRun1[0] = true;
        };
        m.use(new CallMeMiddleware(act));

        MiddlewareCall mwc1 = (tc, nd) -> {
            Assert.assertTrue("First middleware has not been run yet", didRun1[0]);
            didRun2[0] = true;
            return nd.next();
        };
        m.use(new AnonymousReceiveMiddleware(mwc1));

        m.receiveActivityWithStatus(null, null);

        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void RunCodeBeforeAndAfter() throws Exception {
        final boolean[] didRun1 = {false};
        final boolean[] codeafter2run = {false};
        final boolean[] didRun2 = {false};

        MiddlewareSet m = new MiddlewareSet();

        MiddlewareCall mwc1 = (tc, nd) -> {
            Assert.assertFalse("Looks like the 1st middleware has already run", didRun1[0]);
            didRun1[0] = true;
            CompletableFuture<Void> result = nd.next();
            Assert.assertTrue("The 2nd middleware should have run now.", didRun1[0]);
            codeafter2run[0] = true;
            return result;
        };
        m.use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = (tc, nd) -> {
            Assert.assertTrue("Looks like the 1st middleware has not been run", didRun1[0]);
            Assert.assertFalse("The code that runs after middleware 2 is complete has already run.", codeafter2run[0]);
            didRun2[0] = true;
            return nd.next();
        };
        m.use(new AnonymousReceiveMiddleware(mwc2));

        m.receiveActivityWithStatus(null, null).join();
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
        Assert.assertTrue(codeafter2run[0]);
    }

    @Test
    public void CatchAnExceptionViaMiddlware() {
        MiddlewareSet m = new MiddlewareSet();
        final boolean[] caughtException = {false};

        MiddlewareCall mwc1 = new MiddlewareCall() {
            public CompletableFuture<Void> requestHandler(TurnContext tc, NextDelegate nd) {
                return nd.next()
                    .exceptionally(exception -> {
                        Assert.assertTrue(exception instanceof InterruptedException);
                        caughtException[0] = true;
                        return null;
                    });
            }
        };

        m.use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = (tc, nd) -> {
            throw new CompletionException(new InterruptedException("test"));
        };

        m.use(new AnonymousReceiveMiddleware(mwc2));

        m.receiveActivityWithStatus(null, null);

        Assert.assertTrue(caughtException[0]);
    }
}
