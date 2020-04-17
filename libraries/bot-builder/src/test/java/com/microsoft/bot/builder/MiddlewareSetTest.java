// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class MiddlewareSetTest {
    private boolean wasCalled;

    @Test
    public void NoMiddleware() {
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
    public void NoMiddlewareWithDelegate() {
        MiddlewareSet m = new MiddlewareSet();
        wasCalled = false;

        BotCallbackHandler cb = (ctx) -> {
            wasCalled = true;
            return CompletableFuture.completedFuture(null);
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
            return CompletableFuture.completedFuture(null);
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
            throw new CompletionException(new IllegalStateException("test"));
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

        final int[] called = { 0 };
        BotCallbackHandler cb = (context) -> {
            called[0]++;
            return CompletableFuture.completedFuture(null);
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
    public void TwoMiddlewareItemsInOrder() {
        final boolean[] called1 = { false };
        final boolean[] called2 = { false };

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
        final boolean[] called1 = { false };

        CallMeMiddleware one = new CallMeMiddleware(() -> called1[0] = true);

        MiddlewareSet m = new MiddlewareSet();
        m.use(one);

        // The middleware in this pipeline calls next(), so the delegate should be
        // called
        final boolean[] didAllRun = { false };
        BotCallbackHandler cb = (context) -> {
            didAllRun[0] = true;
            return CompletableFuture.completedFuture(null);
        };
        m.receiveActivityWithStatus(null, cb).join();

        Assert.assertTrue(called1[0]);
        Assert.assertTrue(didAllRun[0]);
    }

    @Test
    public void Status_RunAtEndEmptyPipeline() {
        MiddlewareSet m = new MiddlewareSet();
        final boolean[] didAllRun = { false };
        BotCallbackHandler cb = (context) -> {
            didAllRun[0] = true;
            return CompletableFuture.completedFuture(null);
        };

        // This middleware pipeline has no entries. This should result in
        // the status being TRUE.
        m.receiveActivityWithStatus(null, cb);

        Assert.assertTrue(didAllRun[0]);

    }

    @Test
    public void Status_TwoItemsOneDoesNotCallNext() {
        final boolean[] called1 = { false };
        final boolean[] called2 = { false };

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

        boolean[] didAllRun = { false };
        BotCallbackHandler cb = (context) -> {
            didAllRun[0] = true;
            return CompletableFuture.completedFuture(null);
        };

        m.receiveActivityWithStatus(null, cb).join();

        Assert.assertTrue(called1[0]);
        Assert.assertTrue(called2[0]);

        // The 2nd middleware did not call next, so the "final" action should not have
        // run.
        Assert.assertFalse(didAllRun[0]);
    }

    @Test
    public void Status_OneEntryThatDoesNotCallNext() {
        final boolean[] called1 = { false };

        DoNotCallNextMiddleware one = new DoNotCallNextMiddleware(() -> called1[0] = true);

        MiddlewareSet m = new MiddlewareSet();
        m.use(one);

        // The middleware in this pipeline DOES NOT call next(), so this must not be
        // called
        boolean[] didAllRun = { false };
        BotCallbackHandler cb = (context) -> {
            didAllRun[0] = true;
            return CompletableFuture.completedFuture(null);
        };
        m.receiveActivityWithStatus(null, cb);

        Assert.assertTrue(called1[0]);

        // Our "Final" action MUST NOT have been called, as the Middlware Pipeline
        // didn't complete.
        Assert.assertFalse(didAllRun[0]);
    }

    @Test
    public void AnonymousMiddleware() {
        final boolean[] didRun = { false };

        MiddlewareSet m = new MiddlewareSet();

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            didRun[0] = true;
            return nd.next();
        }));

        Assert.assertFalse(didRun[0]);
        m.receiveActivityWithStatus(null, null).join();
        Assert.assertTrue(didRun[0]);
    }

    @Test
    public void TwoAnonymousMiddleware() {
        final boolean[] didRun1 = { false };
        final boolean[] didRun2 = { false };

        MiddlewareSet m = new MiddlewareSet();

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            didRun1[0] = true;
            return nd.next();
        }));

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            didRun2[0] = true;
            return nd.next();
        }));

        m.receiveActivityWithStatus(null, null).join();

        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void TwoAnonymousMiddlewareInOrder() {
        final boolean[] didRun1 = { false };
        final boolean[] didRun2 = { false };

        MiddlewareSet m = new MiddlewareSet();

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            Assert.assertFalse("Looks like the 2nd one has already run", didRun2[0]);
            didRun1[0] = true;
            return nd.next();
        }));

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            Assert.assertTrue("Looks like the 1nd one has not yet run", didRun1[0]);
            didRun2[0] = true;
            return nd.next();
        }));

        m.receiveActivityWithStatus(null, null).join();

        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void MixedMiddlewareInOrderAnonymousFirst() {
        final boolean[] didRun1 = { false };
        final boolean[] didRun2 = { false };

        MiddlewareSet m = new MiddlewareSet();

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            Assert.assertFalse("First middleware already ran", didRun1[0]);
            Assert.assertFalse("Looks like the second middleware was already run", didRun2[0]);
            didRun1[0] = true;
            CompletableFuture<Void> result = nd.next();
            Assert.assertTrue("Second middleware should have completed running", didRun2[0]);
            return result;
        }));

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
        final boolean[] didRun1 = { false };
        final boolean[] didRun2 = { false };

        MiddlewareSet m = new MiddlewareSet();

        ActionDel act = () -> {
            Assert.assertFalse("First middleware should not have already been called", didRun1[0]);
            Assert.assertFalse("Second middleware should not have been invoked yet", didRun2[0]);
            didRun1[0] = true;
        };
        m.use(new CallMeMiddleware(act));

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            Assert.assertTrue("First middleware has not been run yet", didRun1[0]);
            didRun2[0] = true;
            return nd.next();
        }));

        m.receiveActivityWithStatus(null, null);

        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void RunCodeBeforeAndAfter() {
        final boolean[] didRun1 = { false };
        final boolean[] codeafter2run = { false };
        final boolean[] didRun2 = { false };

        MiddlewareSet m = new MiddlewareSet();

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            Assert.assertFalse("Looks like the 1st middleware has already run", didRun1[0]);
            didRun1[0] = true;
            CompletableFuture<Void> result = nd.next();
            Assert.assertTrue("The 2nd middleware should have run now.", didRun1[0]);
            codeafter2run[0] = true;
            return result;
        }));

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> {
            Assert.assertTrue("Looks like the 1st middleware has not been run", didRun1[0]);
            Assert.assertFalse(
                "The code that runs after middleware 2 is complete has already run.",
                codeafter2run[0]
            );
            didRun2[0] = true;
            return nd.next();
        }));

        m.receiveActivityWithStatus(null, null).join();
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
        Assert.assertTrue(codeafter2run[0]);
    }

    @Test
    public void CatchAnExceptionViaMiddleware() {
        MiddlewareSet m = new MiddlewareSet();
        final boolean[] caughtException = { false };

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> CompletableFuture.supplyAsync(() -> {
            System.out.println("First Middleware");
            return null;
        }).thenCompose((result) -> nd.next()).exceptionally(ex -> {
            Assert.assertTrue(ex instanceof CompletionException);
            Assert.assertTrue(ex.getCause() instanceof InterruptedException);
            System.out.println("First Middleware caught");
            caughtException[0] = true;
            return null;
        })));

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> CompletableFuture.supplyAsync(() -> {
            System.out.println("Second Middleware");
            return null;
        }).thenCompose(result -> nd.next())));

        m.use(new AnonymousReceiveMiddleware((tc, nd) -> CompletableFuture.supplyAsync(() -> {
            System.out.println("Third Middleware will throw");
            throw new CompletionException(new InterruptedException("test"));
        }).thenCompose(result -> nd.next())));

        m.receiveActivityWithStatus(null, null).join();

        Assert.assertTrue(caughtException[0]);
    }

    private static class WasCalledMiddleware implements Middleware {
        boolean called = false;

        public boolean getCalled() {
            return this.called;
        }

        public void setCalled(boolean called) {
            this.called = called;
        }

        public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
            setCalled(true);
            return next.next();
        }
    }

    private static class DoNotCallNextMiddleware implements Middleware {
        private final ActionDel _callMe;

        public DoNotCallNextMiddleware(ActionDel callMe) {
            _callMe = callMe;
        }

        public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
            _callMe.CallMe();
            // DO NOT call NEXT
            return CompletableFuture.completedFuture(null);
        }
    }

    private static class CallMeMiddleware implements Middleware {
        private ActionDel callMe;

        public CallMeMiddleware(ActionDel callme) {
            this.callMe = callme;
        }

        @Override
        public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
            this.callMe.CallMe();
            return next.next();
        }
    }
}
