package com.microsoft.bot.builder;


import com.microsoft.bot.builder.base.TestBase;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;


// [TestCategory("Russian Doll Middleware, Nested Middleware sets")]
public class MiddlewareSetTest extends TestBase
{
    protected RestConnectorClient connector;
    protected ChannelAccount bot;
    protected ChannelAccount user;
    private boolean innerOnreceiveCalled;

    public MiddlewareSetTest() {
        super(RunCondition.BOTH);
    }

    @Override
    protected void initializeClients(RestClient restClient, String botId, String userId) {

        connector = new RestConnectorClient(restClient);
        bot = new ChannelAccount(botId);
        user = new ChannelAccount(userId);

        // Test-specific stuff
        innerOnreceiveCalled = false;
    }

    @Override
    protected void cleanUpResources() {
    }


    @Test
    public void NoMiddleware() throws Exception {
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
    public void NestedSet_OnReceive() throws Exception {
        final boolean[] wasCalled = {false};
        MiddlewareSet inner = new MiddlewareSet();
        inner.Use(new AnonymousReceiveMiddleware((MiddlewareCall) (tc, nd) -> {
            wasCalled[0] = true;
            return nd.next();
        }));
        MiddlewareSet outer = new MiddlewareSet();
        outer.Use(inner);
        try {
            outer.ReceiveActivity(null);
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
    public void NoMiddlewareWithDelegate() throws Exception {
        MiddlewareSet m = new MiddlewareSet();
        final boolean wasCalled[] = {false};
        Consumer<TurnContext> cb = context -> {
                wasCalled[0] = true;
        };
        // No middleware. Should not explode.
        m.ReceiveActivityWithStatus(null, cb);
        Assert.assertTrue("Delegate was not called", wasCalled[0]);
    }

    @Test
    public void OneMiddlewareItem() throws Exception {
        WasCalledMiddlware simple = new WasCalledMiddlware();

        final boolean wasCalled[] = {false};
        Consumer<TurnContext> cb = context -> {
                wasCalled[0] = true;
        };

        MiddlewareSet m = new MiddlewareSet();
        m.Use(simple);

        Assert.assertFalse(simple.getCalled());
        m.ReceiveActivityWithStatus(null, cb);
        Assert.assertTrue(simple.getCalled());
        Assert.assertTrue( "Delegate was not called", wasCalled[0]);
    }

    @Test
    public void OneMiddlewareItemWithDelegate() throws Exception {
        WasCalledMiddlware simple = new WasCalledMiddlware();

        MiddlewareSet m = new MiddlewareSet();
        m.Use(simple);

        Assert.assertFalse(simple.getCalled());
        m.ReceiveActivity(null);
        Assert.assertTrue(simple.getCalled());
    }

    @Test(expected = IllegalStateException.class)
    //[ExpectedException(typeof(InvalidOperationException))]
    public void BubbleUncaughtException() throws Exception {
        MiddlewareSet m = new MiddlewareSet();
        m.Use(new AnonymousReceiveMiddleware(new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws IllegalStateException {
                throw new IllegalStateException("test");
            }}
            ));

        m.ReceiveActivity(null);
        Assert.assertFalse("Should never have gotten here", true);
    }

    @Test
    public void TwoMiddlewareItems() throws Exception {
        WasCalledMiddlware one = new WasCalledMiddlware();
        WasCalledMiddlware two = new WasCalledMiddlware();

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        m.ReceiveActivity(null);
        Assert.assertTrue(one.getCalled());
        Assert.assertTrue(two.getCalled());
    }

    @Test
    public void TwoMiddlewareItemsWithDelegate() throws Exception {
        WasCalledMiddlware one = new WasCalledMiddlware();
        WasCalledMiddlware two = new WasCalledMiddlware();

        final int called[] = {0};
        Consumer<TurnContext> cb = (context) -> {
                called[0]++;
        };

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);
        m.Use(two);

        m.ReceiveActivityWithStatus(null, cb);
        Assert.assertTrue(one.getCalled());
        Assert.assertTrue(two.getCalled());
        Assert.assertTrue("Incorrect number of calls to Delegate", called[0] == 1 );
    }

    @Test
    public void TwoMiddlewareItemsInOrder() throws Exception {
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

        m.ReceiveActivity(null);
        Assert.assertTrue(called1[0]);
        Assert.assertTrue(called2[0]);
    }

    @Test
    public void Status_OneMiddlewareRan() throws Exception {
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
        Consumer<TurnContext> cb  = (context) -> {
                didAllRun[0] = true;
        };
        m.ReceiveActivityWithStatus(null, cb);

        Assert.assertTrue(called1[0]);
        Assert.assertTrue(didAllRun[0]);
    }

    @Test
    public void Status_RunAtEndEmptyPipeline() throws Exception {
        MiddlewareSet m = new MiddlewareSet();
        final boolean didAllRun[] = {false};
        Consumer<TurnContext> cb = (context)-> {
                didAllRun[0] = true;
        };

        // This middlware pipeline has no entries. This should result in
        // the status being TRUE.
        m.ReceiveActivityWithStatus(null, cb);
        Assert.assertTrue(didAllRun[0]);

    }

    @Test
    public void Status_TwoItemsOneDoesNotCallNext() throws Exception {
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
        Consumer<TurnContext> cb= (context) -> {
                didAllRun[0] = true;
        };
        m.ReceiveActivityWithStatus(null, cb);
        Assert.assertTrue(called1[0]);
        Assert.assertTrue(called2[0]);

        // The 2nd middleware did not call next, so the "final" action should not have run.
        Assert.assertFalse(didAllRun[0]);
    }

    @Test
    public void Status_OneEntryThatDoesNotCallNext() throws Exception {
        final boolean called1[] = {false};

        DoNotCallNextMiddleware one = new DoNotCallNextMiddleware(new ActionDel() {
            @Override
            public void CallMe() {
                called1[0] = true;
            }
        });

        MiddlewareSet m = new MiddlewareSet();
        m.Use(one);

        // The middleware in this pipeline DOES NOT call next(), so this must not be called
        boolean didAllRun[] = {false};
        Consumer<TurnContext> cb = (context) -> {
                didAllRun[0] = true;
        };
        m.ReceiveActivityWithStatus(null, cb);

        Assert.assertTrue(called1[0]);

        // Our "Final" action MUST NOT have been called, as the Middlware Pipeline
        // didn't complete.
        Assert.assertFalse(didAllRun[0]);
    }

    @Test
    public void AnonymousMiddleware() throws Exception {
        final boolean didRun[] = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws Exception {
                didRun[0] = true;
                nd.next();
                return;
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc));

        Assert.assertFalse(didRun[0]);
         m.ReceiveActivity(null);
        Assert.assertTrue(didRun[0]);
    }

    @Test
    public void TwoAnonymousMiddleware() throws Exception {
        final boolean didRun1[] = {false};
        final boolean didRun2[] = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc1 = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws Exception {
                didRun1[0] = true;
                nd.next();
                return;
            }
        };

        m.Use(new AnonymousReceiveMiddleware(mwc1));
        MiddlewareCall mwc2 = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws Exception {
                didRun2[0] = true;
                nd.next();
                return;
            }
        };

        m.Use(new AnonymousReceiveMiddleware(mwc2));

        m.ReceiveActivity(null);
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void TwoAnonymousMiddlewareInOrder() throws Exception {
        final boolean didRun1[] = {false};
        final boolean didRun2[] = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc1 = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws Exception {
                Assert.assertFalse("Looks like the 2nd one has already run", didRun2[0]);
                didRun1[0] = true;
                nd.next();
                return;
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws Exception {
                Assert.assertTrue("Looks like the 1nd one has not yet run", didRun1[0]);
                didRun2[0] = true;
                nd.next();
                return ;
            }
        };

        m.Use(new AnonymousReceiveMiddleware(mwc2));

        m.ReceiveActivity(null);
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void MixedMiddlewareInOrderAnonymousFirst() throws Exception {
        final boolean didRun1[] = {false};
        final boolean didRun2[] = {false};

        MiddlewareSet m = new MiddlewareSet();
        MiddlewareCall mwc1 = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws Exception {
                Assert.assertFalse("First middleware already ran", didRun1[0]);
                Assert.assertFalse("Looks like the second middleware was already run", didRun2[0]);
                didRun1[0] = true;
                nd.next();
                Assert.assertTrue("Second middleware should have completed running", didRun2[0]);
                return ;
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

        m.ReceiveActivity(null);
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void MixedMiddlewareInOrderAnonymousLast() throws Exception {
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
            public void requestHandler(TurnContext tc, NextDelegate nd) throws Exception {
                Assert.assertTrue("First middleware has not been run yet", didRun1[0]);
                didRun2[0] = true;
                nd.next();
                return;
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc1));

        m.ReceiveActivity(null);
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
    }

    @Test
    public void RunCodeBeforeAndAfter() throws Exception {
        final boolean didRun1[] = {false};
        final boolean codeafter2run[] = {false};
        final boolean didRun2[] = {false};

        MiddlewareSet m = new MiddlewareSet();

        MiddlewareCall mwc1 = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws Exception {
                Assert.assertFalse("Looks like the 1st middleware has already run", didRun1[0]);
                didRun1[0] = true;
                nd.next();
                Assert.assertTrue("The 2nd middleware should have run now.", didRun1[0]);
                codeafter2run[0] = true;
                return ;
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws Exception {
                Assert.assertTrue("Looks like the 1st middleware has not been run", didRun1[0]);
                Assert.assertFalse("The code that runs after middleware 2 is complete has already run.", codeafter2run[0]);
                didRun2[0] = true;
                nd.next();
                return ;
            }
        };
        m.Use(new AnonymousReceiveMiddleware(mwc2));

        m.ReceiveActivity(null);
        Assert.assertTrue(didRun1[0]);
        Assert.assertTrue(didRun2[0]);
        Assert.assertTrue(codeafter2run[0]);
    }

    @Test
    public void CatchAnExceptionViaMiddlware() throws Exception {
        MiddlewareSet m = new MiddlewareSet();
        final boolean caughtException[] = {false};

        MiddlewareCall mwc1 = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws ExecutionException, InterruptedException {
                try {
                    nd.next();
                    Assert.assertTrue("Should not get here", false);

                }
                catch (InterruptedException ex) {
                    System.out.println("Here isi the exception message" + ex.getMessage());
                    System.out.flush();
                    Assert.assertTrue(ex.getMessage() == "test");

                    caughtException[0] = true;
                } catch (Exception e) {
                    Assert.assertTrue("Should not get here" + e.getMessage(), false);
                }
                return ;
        }};

        m.Use(new AnonymousReceiveMiddleware(mwc1));

        MiddlewareCall mwc2 = new MiddlewareCall() {
            public void requestHandler(TurnContext tc, NextDelegate nd) throws InterruptedException {
                throw new InterruptedException("test");
            }
            };

        m.Use(new AnonymousReceiveMiddleware(mwc2));

        m.ReceiveActivity(null);
        Assert.assertTrue(caughtException[0]);
    }



}
