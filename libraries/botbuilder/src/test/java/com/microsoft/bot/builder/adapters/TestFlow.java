package com.microsoft.bot.builder.adapters;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.Activity;
import org.joda.time.DateTime;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class TestFlow {
    final TestAdapter adapter;
    CompletableFuture testTask;
    Function<TurnContext, CompletableFuture> callback;

    public TestFlow(TestAdapter adapter) {
        this(adapter, null);
    }

    public TestFlow(TestAdapter adapter, Function<TurnContext, CompletableFuture> callback) {
        this.adapter = adapter;
        this.callback = callback;
        this.testTask = completedFuture(null);
    }


    public TestFlow(CompletableFuture testTask, TestFlow flow) {
        if (testTask == null)
            this.testTask = completedFuture(null);
        else
            this.testTask = testTask;
        this.callback = flow.callback;
        this.adapter = flow.adapter;
    }

    /**
     * Start the execution of the test flow
     * @return 
     */
    public CompletableFuture<String> StartTest() throws ExecutionException, InterruptedException {
        return (CompletableFuture<String>) this.testTask;
    }

    /**
     * Send a message from the user to the bot
     * @param userSays 
     * @return 
     */
//    public TestFlow Send(String userSays) throws IllegalArgumentException {
//        if (userSays == null)
//            throw new IllegalArgumentException("You have to pass a userSays parameter");
//
//        System.out.print(String.format("USER SAYS: %s", userSays));
//        System.out.flush();
//
//      /**
//       */  Function<TurnContextImpl, CompletableFuture>
//       */
//        return new TestFlow(this.testTask.thenCompose(task -> supplyAsync(() ->{
//          /**
//           */ task.Wait();
//           */
//
//            try {
//                this.adapter.SendTextToBot(userSays, this.callback);
//                return null;
//            } catch (Exception e) {
//                return e.getMessage();
//            }
//        })), this);
//    }
    public TestFlow Send(String userSays) throws IllegalArgumentException {
        if (userSays == null)
            throw new IllegalArgumentException("You have to pass a userSays parameter");

        System.out.print(String.format("USER SAYS: %s (Thread Id: %s)\n", userSays, Thread.currentThread().getId()));
        System.out.flush();

        //  Function<TurnContextImpl, CompletableFuture>
        return new TestFlow(this.testTask.thenApply(task -> supplyAsync(() ->{
            // task.Wait();

            try {
                this.adapter.SendTextToBot(userSays, this.callback);
                return null;
            } catch (Exception e) {
                return e.getMessage();
            }
        })), this);
    }

    /**
     * Send an activity from the user to the bot
     * @param userActivity 
     * @return 
     */
    public TestFlow Send(Activity userActivity) {
        if (userActivity == null)
            throw new IllegalArgumentException("You have to pass an Activity");

        return new TestFlow(this.testTask.thenCompose(task -> supplyAsync(() ->{
            // NOTE: See details code in above method.
            //task.Wait();

            try {
                this.adapter.ProcessActivity((ActivityImpl) userActivity, this.callback);
            } catch (Exception e) {
                return e.getMessage();
            }
            return null;
        })), this);
    }

    /**
     * Delay for time period
     * @param ms 
     * @return 
     */
    public TestFlow Delay(int ms) {
        return new TestFlow(this.testTask.thenCompose(task -> supplyAsync(() ->{

            // NOTE: See details code in above method.
            //task.Wait();


            try {
                Thread.sleep((int) ms);
            } catch (InterruptedException e) {
                return e.getMessage();
            }
            return null;
        })), this);
    }

    /**
     * Assert that reply is expected text
     * @param expected 
     * @param description 
     * @param timeout 
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
     * @param expected 
     * @param description 
     * @param timeout 
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
            if (expected.type() != reply.type())
                return String.format("%s: Type should match", finalDescription);
            if (expected.text().equals(reply.text())) {
                if (finalDescription == null)
                    return String.format("Expected:%s\nReceived:{reply.AsMessageActivity().Text}", expected.text());
                else
                    return String.format("%s: Text should match", finalDescription);
            }
            // TODO, expand this to do all properties set on expected
            return null;
        }, description, timeout);
    }

    /**
     * Assert that the reply matches a custom validation routine
     * @param validateActivity 
     * @param description 
     * @param timeout 
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
        return new TestFlow(this.testTask.thenApply(task -> supplyAsync(() ->{
            // NOTE: See details code in above method.
            //task.Wait();
            int finalTimeout = Integer.MAX_VALUE;
            if (isDebug())
                finalTimeout = Integer.MAX_VALUE;

            System.out.println(String.format("AssertReply: Starting loop : %s (Thread:%s)", description, Thread.currentThread().getId()));
            System.out.flush();
            DateTime start = DateTime.now();
            while (true) {
                DateTime current = DateTime.now();

                if ((current.getMillis() - start.getMillis()) > (long) finalTimeout)
                    return String.format("%d ms Timed out waiting for:'%s'", finalTimeout, description);


                Activity replyActivity = this.adapter.GetNextReply();
                System.out.println(String.format("AssertReply: Received Reply: %s (Thread:%s) ",
                        String.format("=============\n From: %s\n To:%s\n Text:%s\n==========\n", replyActivity.from().name(), replyActivity.recipient().name(), replyActivity.text()),
                        Thread.currentThread().getId()));
                System.out.flush();

                if (replyActivity != null) {
                    // if we have a reply
                    return validateActivity.apply(replyActivity);
                }
            }
        })), this);
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
     *
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

                System.out.print(String.format("TestTurn(%s): USER SAYS: %s \n",Thread.currentThread().getId(), userSays ));
                System.out.flush();

                try {
                    this.adapter.SendTextToBot(userSays, this.callback);
                    return null;
                } catch (Exception e) {
                    return e.getMessage();
                }

            })
            .thenApply(arg -> { // Assert Reply
                int finalTimeout = Integer.MAX_VALUE;
                if (isDebug())
                    finalTimeout = Integer.MAX_VALUE;
                Function<Activity, String> validateActivity = activity -> {
                        if (activity.text().equals(expected)) {
                            System.out.println(String.format("TestTurn(tid:%s): Validated text is: %s", Thread.currentThread().getId(), expected ));
                            System.out.flush();

                            return "SUCCESS";
                        }
                        System.out.println(String.format("TestTurn(tid:%s): Failed validate text is: %s", Thread.currentThread().getId(), expected ));
                        System.out.flush();

                        return String.format("FAIL: %s received in Activity.text (%s expected)", activity.text(), expected);
                        };


                System.out.println(String.format("TestTurn(tid:%s): Started receive loop: %s", Thread.currentThread().getId(), description ));
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
                                String.format("\n========\n To:%s\n From:%s\n Msg:%s\n=======", replyActivity.recipient().name(), replyActivity.from().name(), replyActivity.text())
                                ));
                        System.out.flush();
                        return validateActivity.apply(replyActivity);
                    }
                    else {
                        System.out.println(String.format("TestTurn(tid:%s): No reply..", Thread.currentThread().getId()));
                        System.out.flush();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }})
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
     * @param userSays 
     * @param expected 
     * @param description 
     * @param timeout 
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
     * @param userSays 
     * @param expected 
     * @param description 
     * @param timeout 
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
     * @param userSays 
     * @param expected 
     * @param description 
     * @param timeout 
     * @return 
     */
    public TestFlow Test(String userSays,  Function<Activity, String> expected) {
        return Test(userSays, expected, null, 3000);
    }
    public TestFlow Test(String userSays,  Function<Activity, String> expected, String description) {
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
     * @param candidates 
     * @param description 
     * @param timeout 
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
                        for(String candidate : candidates) {
                            if (reply.text() == candidate)
                                return null;
                        }
                        return String.format("%s: Not one of candidates: %s", description, String.join("\n ", candidates));
                        },description, timeout);
    }

}
