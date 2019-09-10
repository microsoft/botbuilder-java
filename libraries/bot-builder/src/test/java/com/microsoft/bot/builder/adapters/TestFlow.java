package com.microsoft.bot.builder.adapters;

import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.schema.Activity;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Assert;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class TestFlow {
    final TestAdapter adapter;
    CompletableFuture<String> testTask;
    BotCallbackHandler callback;

    ArrayList<Supplier<String>> tasks = new ArrayList<Supplier<String>>();
    ForkJoinPool.ForkJoinWorkerThreadFactory factory = new ForkJoinPool.ForkJoinWorkerThreadFactory()
    {
        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool)
        {
            final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setName("TestFlow-" + worker.getPoolIndex());
            return worker;
        }
    };

    ExecutorService executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), factory, null, true);


    public TestFlow(TestAdapter adapter) {
        this(adapter, null);
    }

    public TestFlow(TestAdapter adapter, BotCallbackHandler callback) {
        this.adapter = adapter;
        this.callback = callback;
        this.testTask = completedFuture(null);
    }


    public TestFlow(Supplier<String> testTask, TestFlow flow) {
        this.tasks = flow.tasks;
        if (testTask != null)
            this.tasks.add(testTask);
        this.callback = flow.callback;
        this.adapter = flow.adapter;
    }


    /**
     * Start the execution of the test flow
     *
     * @return
     */
    public String StartTest() throws ExecutionException, InterruptedException {

        System.out.printf("+------------------------------------------+\n");
        int count = 0;
        for (Supplier<String> task : this.tasks) {
            System.out.printf("| Running task %s of %s\n", count++, this.tasks.size());
            String result = null;
            result = task.get();
            System.out.printf("|  --> Result: %s", result);
            System.out.flush();
        }
        System.out.printf("+------------------------------------------+\n");
        return "Completed";

    }

    /**
     * Send a message from the user to the bot
     *
     * @param userSays
     * @return
     */
    public TestFlow Send(String userSays) throws IllegalArgumentException {
        if (userSays == null)
            throw new IllegalArgumentException("You have to pass a userSays parameter");

        //  Function<TurnContextImpl, CompletableFuture>
        return new TestFlow((() -> {
            System.out.print(String.format("USER SAYS: %s (Thread Id: %s)\n", userSays, Thread.currentThread().getId()));
            System.out.flush();
            try {
                this.adapter.SendTextToBot(userSays, this.callback);
                return "Successfully sent " + userSays;
            } catch (Exception e) {
                Assert.fail(e.getMessage());
                return e.getMessage();
            }
        }), this);
    }

    /**
     * Send an activity from the user to the bot
     *
     * @param userActivity
     * @return
     */
    public TestFlow Send(Activity userActivity) {
        if (userActivity == null)
            throw new IllegalArgumentException("You have to pass an Activity");

        return new TestFlow((() -> {
            System.out.printf("TestFlow(%s): Send with User Activity! %s", Thread.currentThread().getId(), userActivity.getText());
            System.out.flush();


            try {
                this.adapter.ProcessActivity((Activity) userActivity, this.callback);
                return "TestFlow: Send() -> ProcessActivity: " + userActivity.getText();
            } catch (Exception e) {
                return e.getMessage();

            }

        }), this);
    }

    /**
     * Delay for time period
     *
     * @param ms
     * @return
     */
    public TestFlow Delay(int ms) {
        return new TestFlow(() ->
        {
            System.out.printf("TestFlow(%s): Delay(%s ms) called. ", Thread.currentThread().getId(), ms);
            System.out.flush();
            try {
                Thread.sleep((int) ms);
            } catch (InterruptedException e) {
                return e.getMessage();
            }
            return null;
        }, this);
    }

    /**
     * Assert that reply is expected text
     *
     * @param expected
     * @return
     */
    public TestFlow AssertReply(String expected) {
        return this.AssertReply(expected, null, 3000);
    }

    public TestFlow AssertReply(String expected, String description) {
        return this.AssertReply(expected, description, 3000);
    }

    public TestFlow AssertReply(String expected, String description, int timeout) {
        return this.AssertReply(this.adapter.MakeActivity(expected), description, timeout);
    }

    /**
     * Assert that the reply is expected activity
     *
     * @param expected
     * @return
     */
    public TestFlow AssertReply(Activity expected) {
        String description = Thread.currentThread().getStackTrace()[1].getMethodName();
        return AssertReply(expected, description, 3000);
    }

    public TestFlow AssertReply(Activity expected, String description, int timeout) {
        if (description == null)
            description = Thread.currentThread().getStackTrace()[1].getMethodName();
        String finalDescription = description;
        return this.AssertReply((reply) -> {
            if (expected.getType() != reply.getType())
                return String.format("%s: Type should match", finalDescription);
            if (expected.getText().equals(reply.getText())) {
                if (finalDescription == null)
                    return String.format("Expected:%s\nReceived:{reply.AsMessageActivity().Text}", expected.getText());
                else
                    return String.format("%s: Text should match", finalDescription);
            }
            // TODO, expand this to do all properties set on expected
            return null;
        }, description, timeout);
    }

    /**
     * Assert that the reply matches a custom validation routine
     *
     * @param validateActivity
     * @return
     */
    public TestFlow AssertReply(Function<Activity, String> validateActivity) {
        String description = Thread.currentThread().getStackTrace()[1].getMethodName();
        return AssertReply(validateActivity, description, 3000);
    }

    public TestFlow AssertReply(Function<Activity, String> validateActivity, String description) {
        return AssertReply(validateActivity, description, 3000);
    }

    public TestFlow AssertReply(Function<Activity, String> validateActivity, String description, int timeout) {
        return new TestFlow(() -> {
            System.out.println(String.format("AssertReply: Starting loop : %s (Thread:%s)", description, Thread.currentThread().getId()));
            System.out.flush();

            int finalTimeout = Integer.MAX_VALUE;
            if (isDebug())
                finalTimeout = Integer.MAX_VALUE;

            DateTime start = DateTime.now();
            while (true) {
                DateTime current = DateTime.now();

                if ((current.getMillis() - start.getMillis()) > (long) finalTimeout) {
                    System.out.println("AssertReply: Timeout!\n");
                    System.out.flush();
                    return String.format("%d ms Timed out waiting for:'%s'", finalTimeout, description);
                }

//                System.out.println("Before GetNextReply\n");
//                System.out.flush();

                Activity replyActivity = this.adapter.GetNextReply();
//                System.out.println("After GetNextReply\n");
//                System.out.flush();

                if (replyActivity != null) {
                    System.out.printf("AssertReply(tid:%s): Received Reply: %s ", Thread.currentThread().getId(), (replyActivity.getText() == null) ? "No Text set" : replyActivity.getText());
                    System.out.flush();
                    System.out.printf("=============\n From: %s\n To:%s\n ==========\n", (replyActivity.getFrom() == null) ? "No from set" : replyActivity.getFrom().getName(),
                            (replyActivity.getRecipient() == null) ? "No recipient set" : replyActivity.getRecipient().getName());
                    System.out.flush();

                    // if we have a reply
                    return validateActivity.apply(replyActivity);
                } else {
                    System.out.printf("AssertReply(tid:%s): Waiting..\n", Thread.currentThread().getId());
                    System.out.flush();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, this);
    }

    // Hack to determine if debugger attached..
    public boolean isDebug() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.contains("jdwp=")) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param userSays
     * @param expected
     * @return
     */
    public TestFlow Turn(String userSays, String expected, String description, int timeout) {
        String result = null;
        try {

            result = CompletableFuture.supplyAsync(() -> {  // Send the message

                if (userSays == null)
                    throw new IllegalArgumentException("You have to pass a userSays parameter");

                System.out.print(String.format("TestTurn(%s): USER SAYS: %s \n", Thread.currentThread().getId(), userSays));
                System.out.flush();

                try {
                    this.adapter.SendTextToBot(userSays, this.callback);
                    return null;
                } catch (Exception e) {
                    return e.getMessage();
                }

            }, ExecutorFactory.getExecutor())
                    .thenApply(arg -> { // Assert Reply
                        int finalTimeout = Integer.MAX_VALUE;
                        if (isDebug())
                            finalTimeout = Integer.MAX_VALUE;
                        Function<Activity, String> validateActivity = activity -> {
                            if (activity.getText().equals(expected)) {
                                System.out.println(String.format("TestTurn(tid:%s): Validated text is: %s", Thread.currentThread().getId(), expected));
                                System.out.flush();

                                return "SUCCESS";
                            }
                            System.out.println(String.format("TestTurn(tid:%s): Failed validate text is: %s", Thread.currentThread().getId(), expected));
                            System.out.flush();

                            return String.format("FAIL: %s received in Activity.text (%s expected)", activity.getText(), expected);
                        };


                        System.out.println(String.format("TestTurn(tid:%s): Started receive loop: %s", Thread.currentThread().getId(), description));
                        System.out.flush();
                        DateTime start = DateTime.now();
                        while (true) {
                            DateTime current = DateTime.now();

                            if ((current.getMillis() - start.getMillis()) > (long) finalTimeout)
                                return String.format("TestTurn: %d ms Timed out waiting for:'%s'", finalTimeout, description);


                            Activity replyActivity = this.adapter.GetNextReply();


                            if (replyActivity != null) {
                                // if we have a reply
                                System.out.println(String.format("TestTurn(tid:%s): Received Reply: %s",
                                        Thread.currentThread().getId(),
                                        String.format("\n========\n To:%s\n From:%s\n Msg:%s\n=======", replyActivity.getRecipient().getName(), replyActivity.getFrom().getName(), replyActivity.getText())
                                ));
                                System.out.flush();
                                return validateActivity.apply(replyActivity);
                            } else {
                                System.out.println(String.format("TestTurn(tid:%s): No reply..", Thread.currentThread().getId()));
                                System.out.flush();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    })
                    .get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return this;

    }

    /**
     * Say() -> shortcut for .Send(user).AssertReply(Expected)
     *
     * @param userSays
     * @param expected
     * @return
     */
    public TestFlow Test(String userSays, String expected) {
        return Test(userSays, expected, null, 3000);
    }

    public TestFlow Test(String userSays, String expected, String description) {
        return Test(userSays, expected, description, 3000);
    }

    public TestFlow Test(String userSays, String expected, String description, int timeout) {
        if (expected == null)
            throw new IllegalArgumentException("expected");

        return this.Send(userSays)
                .AssertReply(expected, description, timeout);
    }

    /**
     * Test() -> shortcut for .Send(user).AssertReply(Expected)
     *
     * @param userSays
     * @param expected
     * @return
     */
    public TestFlow Test(String userSays, Activity expected) {
        return Test(userSays, expected, null, 3000);
    }

    public TestFlow Test(String userSays, Activity expected, String description) {
        return Test(userSays, expected, description, 3000);
    }

    public TestFlow Test(String userSays, Activity expected, String description, int timeout) {
        if (expected == null)
            throw new IllegalArgumentException("expected");

        return this.Send(userSays)
                .AssertReply(expected, description, timeout);
    }

    /**
     * Say() -> shortcut for .Send(user).AssertReply(Expected)
     *
     * @param userSays
     * @param expected
     * @return
     */
    public TestFlow Test(String userSays, Function<Activity, String> expected) {
        return Test(userSays, expected, null, 3000);
    }

    public TestFlow Test(String userSays, Function<Activity, String> expected, String description) {
        return Test(userSays, expected, description, 3000);
    }

    public TestFlow Test(String userSays, Function<Activity, String> expected, String description, int timeout) {
        if (expected == null)
            throw new IllegalArgumentException("expected");

        return this.Send(userSays)
                .AssertReply(expected, description, timeout);
    }

    /**
     * Assert that reply is one of the candidate responses
     *
     * @param candidates
     * @return
     */
    public TestFlow AssertReplyOneOf(String[] candidates) {
        return AssertReplyOneOf(candidates, null, 3000);
    }

    public TestFlow AssertReplyOneOf(String[] candidates, String description) {
        return AssertReplyOneOf(candidates, description, 3000);
    }

    public TestFlow AssertReplyOneOf(String[] candidates, String description, int timeout) {
        if (candidates == null)
            throw new IllegalArgumentException("candidates");

        return this.AssertReply((reply) -> {
            for (String candidate : candidates) {
                if (StringUtils.equals(reply.getText(), candidate))
                    return null;
            }
            return String.format("%s: Not one of candidates: %s", description, String.join("\n ", candidates));
        }, description, timeout);
    }

}
