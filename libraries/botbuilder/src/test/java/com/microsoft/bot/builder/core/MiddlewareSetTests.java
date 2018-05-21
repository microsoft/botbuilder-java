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


    @Test
    public void NestedSet_OnReceive()
    {
        final boolean[] wasCalled = {false};
        MiddlewareSet inner = new MiddlewareSet();
        inner.Use(new AnonymousReceiveMiddleware(new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
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

    @Test
    public void OneMiddlewareItemWithDelegate() throws ExecutionException, InterruptedException {
        WasCalledMiddlware simple = new WasCalledMiddlware();

        MiddlewareSet m = new MiddlewareSet();
        m.Use(simple);

        Assert.assertFalse(simple.getCalled());
        await(m.ReceiveActivity(null));
        Assert.assertTrue(simple.getCalled());
    }

    @Test(expected = IllegalStateException.class)
    //[ExpectedException(typeof(InvalidOperationException))]
    public void BubbleUncaughtException() throws ExecutionException, InterruptedException {
        MiddlewareSet m = new MiddlewareSet();
        m.Use(new AnonymousReceiveMiddleware(new MiddlewareCall() {
            public CompletableFuture<Boolean> requestHandler(TurnContext tc, NextDelegate nd) throws IllegalStateException {
                throw new IllegalStateException("test");
            }}
            ));

        await(m.ReceiveActivity(null));
        Assert.assertFalse("Should never have gotten here", true);
    }

    @Test
    public void TwoMiddlewareItems() throws ExecutionException, InterruptedException
    {
        WasCalledMiddlware one = new WasCalledMiddlware();
        WasCalledMiddlware two = new WasCalledMiddlware();

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        await(m.ReceiveActivity(null));
        Assert.assertTrue(one.getCalled());
        Assert.assertTrue(two.getCalled());
    }

    @Test
    public void TwoMiddlewareItemsWithDelegate() throws ExecutionException, InterruptedException
    {
        WasCalledMiddlware one = new WasCalledMiddlware();
        WasCalledMiddlware two = new WasCalledMiddlware();

        final int called[] = {0};
        TurnTask tt = new TurnTask() {
            @Override
            public CompletableFuture invoke(TurnContext context) {
                called[0]++;
                return completedFuture(null);
            }
        };

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        await(m.ReceiveActivityWithStatus(null, tt));
        Assert.assertTrue(one.getCalled());
        Assert.assertTrue(two.getCalled());
        Assert.assertTrue("Incorrect number of calls to Delegate", called[0] == 1 );
    }

    @Test
    public void TwoMiddlewareItemsInOrder() throws ExecutionException, InterruptedException
    {
        final boolean called1[] = {false};
        final boolean called2[] = {false};

        CallMeMiddlware one = new CallMeMiddlware(new ActionDel() {
            @Override
            public void CallMe() {
                Assert.assertFalse( "Second Middleware was called", called2[0]);
                called1[0] = true;
            }
        });

        CallMeMiddlware two = new CallMeMiddlware(new ActionDel() {
            @Override
            public void CallMe() {
                Assert.assertTrue("First Middleware was not called", called1[0]);
                called2[0] = true;
            }
        });

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        await(m.ReceiveActivity(null));
        Assert.assertTrue(called1[0]);
        Assert.assertTrue(called2[0]);
    }

    @Test
    public void Status_OneMiddlewareRan() throws ExecutionException, InterruptedException {
        final boolean called1[] = {false};

        CallMeMiddlware one = new CallMeMiddlware(new ActionDel() {
            @Override
            public void CallMe() {
                called1[0] = true;
            }
        });

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);

        // The middlware in this pipeline calls next(), so the delegate should be called
        final boolean didAllRun[] = {false};
        TurnTask tt = new TurnTask() {
            @Override
            public CompletableFuture invoke(TurnContext context) {
                didAllRun[0] = true;
                return completedFuture(null);
            }
        };
        await(m.ReceiveActivityWithStatus(null, tt));

        Assert.assertTrue(called1[0]);
        Assert.assertTrue(didAllRun[0]);
    }

    @Test
    public void Status_RunAtEndEmptyPipeline() throws ExecutionException, InterruptedException
    {
        MiddlewareSet m = new MiddlewareSet();
        final boolean didAllRun[] = {false};
        TurnTask tt = new TurnTask() {
            @Override
            public CompletableFuture invoke(TurnContext context) {
                didAllRun[0] = true;
                return completedFuture(null);
            }
        };

        // This middlware pipeline has no entries. This should result in
        // the status being TRUE.
        await(m.ReceiveActivityWithStatus(null, tt));
        Assert.assertTrue(didAllRun[0]);

    }

    @Test
    public void Status_TwoItemsOneDoesNotCallNext() throws ExecutionException, InterruptedException
    {
        final boolean called1[] = {false};
        final boolean called2[] = {false};

        CallMeMiddlware one = new CallMeMiddlware(new ActionDel() {
            @Override
            public void CallMe() {
                Assert.assertFalse("Second Middleware was called", called2[0]);
                called1[0] = true;
            }
        });

        DoNotCallNextMiddleware two = new DoNotCallNextMiddleware(new ActionDel() {
            @Override
            public void CallMe() {
                Assert.assertTrue("First Middleware was not called", called1[0]);
                called2[0] = true;
        }});

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        boolean didAllRun[] = {false};
        TurnTask tt = new TurnTask() {
            @Override
            public CompletableFuture invoke(TurnContext context) {
                didAllRun[0] = true;
                return completedFuture(null);
            }
        };
        await(m.ReceiveActivityWithStatus(null, tt));
        Assert.assertTrue(called1[0]);
        Assert.assertTrue(called2[0]);

        // The 2nd middleware did not call next, so the "final" action should not have run.
        Assert.assertFalse(didAllRun[0]);
    }

    @Test
    public void Status_OneEntryThatDoesNotCallNext() throws ExecutionException, InterruptedException
    {
        final boolean called1[] = {false};

        DoNotCallNextMiddleware one = new DoNotCallNextMiddleware(new ActionDel() {
            @Override
            public void CallMe() {
                called1[0] = true;
            }
        });

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);

        // The middlware in this pipeline DOES NOT call next(), so this must not be called
        boolean didAllRun[] = {false};
        TurnTask tt = new TurnTask() {
            @Override
            public CompletableFuture invoke(TurnContext context) {
                didAllRun[0] = true;
                return completedFuture(null);
            }
        };
        await(m.ReceiveActivityWithStatus(null, tt));

        Assert.assertTrue(called1[0]);

        // Our "Final" action MUST NOT have been called, as the Middlware Pipeline
        // didn't complete.
        Assert.assertFalse(didAllRun[0]);
    }

    @Test
    public void AnonymousMiddleware() throws ExecutionException, InterruptedException
    {
        final boolean didRun[] = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc = new MiddlewareCall() {
            public CompletableFuture<Boolean> requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                didRun[0] = true;
                await(nd.next());
                return completedFuture(null);
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc));

        Assert.assertFalse(didRun[0]);
        await( m.ReceiveActivity(null));
        Assert.assertTrue(didRun[0]);
    }

    @Test
    public void TwoAnonymousMiddleware() throws ExecutionException, InterruptedException
    {
        final boolean didRun1[] = {false};
        final boolean didRun2[] = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc1 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                didRun1[0] = true;
                await(nd.next());
                return completedFuture(null);
            }
        };

        m.Use(new AnonymousReceiveMiddleware(mwc1));
        MiddlewareCall mwc2 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                didRun2[0] = true;
                await(nd.next());
                return completedFuture(null);
            }
        };

        m.Use(new AnonymousReceiveMiddleware(mwc2));

        await(m.ReceiveActivity(null));
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void TwoAnonymousMiddlewareInOrder() throws ExecutionException, InterruptedException
    {
        final boolean didRun1[] = {false};
        final boolean didRun2[] = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc1 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                Assert.assertFalse("Looks like the 2nd one has already run", didRun2[0]);
                didRun1[0] = true;
                await(nd.next());
                return completedFuture(null);
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                Assert.assertTrue("Looks like the 1nd one has not yet run", didRun1[0]);
                didRun2[0] = true;
                await(nd.next());
                return completedFuture(null);
            }
        };

        m.Use(new AnonymousReceiveMiddleware(mwc2));

        await(m.ReceiveActivity(null));
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void MixedMiddlewareInOrderAnonymousFirst() throws ExecutionException, InterruptedException
    {
        final boolean didRun1[] = {false};
        final boolean didRun2[] = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc1 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                Assert.assertFalse("First middleware already ran", didRun1[0]);
                Assert.assertFalse("Looks like the second middleware was already run", didRun2[0]);
                didRun1[0] = true;
                await(nd.next());
                Assert.assertTrue("Second middleware should have completed running", didRun2[0]);
                return completedFuture(null);
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc1));

        ActionDel act = new ActionDel() {
            @Override
            public void CallMe() {
                Assert.assertTrue("First middleware should have already been called", didRun1[0]);
                Assert.assertFalse("Second middleware should not have been invoked yet", didRun2[0]);
                didRun2[0] = true;
            }
        };
        m.Use(new CallMeMiddlware(act));

        await(m.ReceiveActivity(null));
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void MixedMiddlewareInOrderAnonymousLast() throws ExecutionException, InterruptedException
    {
        final boolean didRun1[] = {false};
        final boolean didRun2[] = {false};

        MiddlewareSet m = new MiddlewareSet();

        ActionDel act = new ActionDel() {
            @Override
            public void CallMe() {
                Assert.assertFalse("First middleware should not have already been called", didRun1[0]);
                Assert.assertFalse("Second middleware should not have been invoked yet", didRun2[0]);
                didRun1[0] = true;
            }
        };
        m.Use(new CallMeMiddlware(act));

        MiddlewareCall mwc1 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                Assert.assertTrue("First middleware has not been run yet", didRun1[0]);
                didRun2[0] = true;
                await(nd.next());
                return completedFuture(null);
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc1));

        await(m.ReceiveActivity(null));
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void RunCodeBeforeAndAfter() throws ExecutionException, InterruptedException
    {
        final boolean didRun1[] = {false};
        final boolean codeafter2run[] = {false};
        final boolean didRun2[] = {false};

        MiddlewareSet m = new MiddlewareSet();

        MiddlewareCall mwc1 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                Assert.assertFalse("Looks like the 1st middleware has already run", didRun1[0]);
                didRun1[0] = true;
                await(nd.next());
                Assert.assertTrue("The 2nd middleware should have run now.", didRun1[0]);
                codeafter2run[0] = true;
                return completedFuture(null);
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                Assert.assertTrue("Looks like the 1st middleware has not been run", didRun1[0]);
                Assert.assertFalse("The code that runs after middleware 2 is complete has already run.", codeafter2run[0]);
                didRun2[0] = true;
                await(nd.next());
                return completedFuture(null);
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc2));

        await(m.ReceiveActivity(null));
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
        Assert.assertTrue(codeafter2run[0]);
    }

    @Test
    public void CatchAnExceptionViaMiddlware() throws ExecutionException, InterruptedException
    {
        MiddlewareSet m = new MiddlewareSet();
        final boolean caughtException[] = {false};

        MiddlewareCall mwc1 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                try {
                    await(nd.next());
                    Assert.assertTrue("Should not get here", false);

                }
                catch (InterruptedException ex) {
                    System.out.println("Here isi the exception message" + ex.getMessage());
                    System.out.flush();
                    Assert.assertTrue(ex.getMessage() == "test");

                    caughtException[0] = true;
                }
                return completedFuture(null);
        }};

        m.Use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = new MiddlewareCall() {
            public CompletableFuture requestHandler(TurnContext tc, NextDelegate nd) throws InterruptedException {
                throw new InterruptedException("test");
            }
            };

        m.Use(new AnonymousReceiveMiddleware(mwc2));

        await(m.ReceiveActivity(null));
        Assert.assertTrue(caughtException[0]);
    }



}
