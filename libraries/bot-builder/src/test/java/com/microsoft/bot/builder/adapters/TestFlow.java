// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.adapters;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.schema.Activity;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestFlow {
    final TestAdapter adapter;
    CompletableFuture<Void> testTask;
    BotCallbackHandler callback;
    // ArrayList<Supplier<String>> tasks = new ArrayList<Supplier<String>>();

    public TestFlow(TestAdapter adapter) {
        this(adapter, null);
    }

    public TestFlow(
        TestAdapter adapter,
        BotCallbackHandler callback
    ) {
        this.adapter = adapter;
        this.callback = callback;
        this.testTask = CompletableFuture.completedFuture(null);
    }

    public TestFlow(
        CompletableFuture<Void> testTask,
        TestFlow flow
    ) {
        this.testTask = testTask == null ? CompletableFuture.completedFuture(null) : testTask;
        this.callback = flow.callback;
        this.adapter = flow.adapter;
    }

    /**
     * Start the execution of the test flow
     *
     * @return
     */
    public CompletableFuture<Void> startTest() {
        return testTask;
    }

    /**
     * Send a message from the user to the bot
     *
     * @param userSays
     * @return
     */
    public TestFlow send(String userSays) throws IllegalArgumentException {
        if (userSays == null)
            throw new IllegalArgumentException("You have to pass a userSays parameter");

        return new TestFlow(testTask.thenCompose(result -> {
            System.out.print(
                String.format("USER SAYS: %s (tid: %s)\n", userSays, Thread.currentThread().getId())
            );
            return this.adapter.sendTextToBot(userSays, this.callback);
        }), this);
    }

    /**
     *  Creates a conversation update activity and process it the activity.
     * @return A new TestFlow Object
     */
    public TestFlow sendConversationUpdate() {
        return new TestFlow(testTask.thenCompose(result -> {
            Activity cu = Activity.createConversationUpdateActivity();
            cu.getMembersAdded().add(this.adapter.conversationReference().getUser());
            return this.adapter.processActivity(cu, callback);
        }), this);
    }

    /**
     * Send an activity from the user to the bot.
     *
     * @param userActivity
     * @return
     */
    public TestFlow send(Activity userActivity) {
        if (userActivity == null)
            throw new IllegalArgumentException("You have to pass an Activity");

        return new TestFlow(testTask.thenCompose(result -> {
            System.out.printf(
                "TestFlow: Send with User Activity! %s (tid:%s)",
                userActivity.getText(),
                Thread.currentThread().getId()
            );
            return this.adapter.processActivity(userActivity, this.callback);
        }), this);
    }

    /**
     * Delay for time period
     *
     * @param ms
     * @return
     */
    public TestFlow delay(int ms) {
        return new TestFlow(testTask.thenCompose(result -> {
            System.out.printf(
                "TestFlow: Delay(%s ms) called. (tid:%s)\n",
                ms,
                Thread.currentThread().getId()
            );
            System.out.flush();
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {

            }
            return CompletableFuture.completedFuture(null);
        }), this);
    }

    /**
     * Assert that reply is expected text
     *
     * @param expected
     * @return
     */
    public TestFlow assertReply(String expected) {
        return this.assertReply(expected, null, 3000);
    }

    public TestFlow assertReply(String expected, String description) {
        return this.assertReply(expected, description, 3000);
    }

    public TestFlow assertReply(String expected, String description, int timeout) {
        return this.assertReply(this.adapter.makeActivity(expected), description, timeout);
    }

    /**
     * Assert that the reply is expected activity
     *
     * @param expected
     * @return
     */
    public TestFlow assertReply(Activity expected) {
        String description = Thread.currentThread().getStackTrace()[1].getMethodName();
        return assertReply(expected, description);
    }

    public TestFlow assertReply(Activity expected, String description) {
        return assertReply(expected, description, 3000);
    }

    public TestFlow assertReply(Activity expected, String description, int timeout) {
        if (description == null)
            description = Thread.currentThread().getStackTrace()[1].getMethodName();
        return this.assertReply((reply) -> {
            if (!StringUtils.equals(expected.getType(), reply.getType()))
                throw new RuntimeException(
                    String.format(
                        "Type: '%s' should match expected '%s'",
                        reply.getType(),
                        expected.getType()
                    )
                );
            if (!expected.getText().equals(reply.getText())) {
                throw new RuntimeException(
                    String.format(
                        "Text '%s' should match expected '%s'",
                        reply.getText(),
                        expected.getText()
                    )
                );
            }
        }, description, timeout);
    }

    /**
     * Assert that the reply matches a custom validation routine
     *
     * @param validateActivity
     * @return
     */
    public TestFlow assertReply(Consumer<Activity> validateActivity) {
        String description = Thread.currentThread().getStackTrace()[1].getMethodName();
        return assertReply(validateActivity, description, 3000);
    }

    public TestFlow assertReply(Consumer<Activity> validateActivity, String description) {
        return assertReply(validateActivity, description, 3000);
    }

    public TestFlow assertReply(
        Consumer<Activity> validateActivity,
        String description,
        int timeout
    ) {
        return new TestFlow(testTask.thenApply(result -> {
            System.out.println(
                String.format(
                    "AssertReply: Starting loop : %s (tid:%s)",
                    description,
                    Thread.currentThread().getId()
                )
            );
            System.out.flush();

            int finalTimeout = Integer.MAX_VALUE;
            if (isDebug())
                finalTimeout = Integer.MAX_VALUE;

            long start = System.currentTimeMillis();
            while (true) {
                long current = System.currentTimeMillis();

                if ((current - start) > (long) finalTimeout) {
                    System.out.println("AssertReply: Timeout!\n");
                    System.out.flush();
                    throw new RuntimeException(
                        String.format("%d ms Timed out waiting for:'%s'", finalTimeout, description)
                    );
                }

                // System.out.println("Before GetNextReply\n");
                // System.out.flush();

                Activity replyActivity = this.adapter.getNextReply();
                // System.out.println("After GetNextReply\n");
                // System.out.flush();

                if (replyActivity != null) {
                    System.out.printf(
                        "AssertReply: Received Reply (tid:%s)",
                        Thread.currentThread().getId()
                    );
                    System.out.flush();
                    System.out.printf(
                        "\n =============\n From: %s\n To:%s\n Text:%s\n ==========\n",
                        (replyActivity.getFrom() == null)
                            ? "No from set"
                            : replyActivity.getFrom().getName(),
                        (replyActivity.getRecipient() == null)
                            ? "No recipient set"
                            : replyActivity.getRecipient().getName(),
                        (replyActivity.getText() == null) ? "No Text set" : replyActivity.getText()
                    );
                    System.out.flush();

                    // if we have a reply
                    validateActivity.accept(replyActivity);
                    return null;
                } else {
                    System.out.printf(
                        "AssertReply(tid:%s): Waiting..\n",
                        Thread.currentThread().getId()
                    );
                    System.out.flush();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }), this);
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
    public TestFlow turn(String userSays, String expected, String description, int timeout) {
        String result = null;
        try {

            result = CompletableFuture.supplyAsync(() -> { // Send the message

                if (userSays == null)
                    throw new IllegalArgumentException("You have to pass a userSays parameter");

                System.out.print(
                    String.format(
                        "TestTurn(%s): USER SAYS: %s \n",
                        Thread.currentThread().getId(),
                        userSays
                    )
                );
                System.out.flush();

                try {
                    this.adapter.sendTextToBot(userSays, this.callback);
                    return null;
                } catch (Exception e) {
                    return e.getMessage();
                }

            }, ExecutorFactory.getExecutor()).thenApply(arg -> { // Assert Reply
                int finalTimeout = Integer.MAX_VALUE;
                if (isDebug())
                    finalTimeout = Integer.MAX_VALUE;
                Function<Activity, String> validateActivity = activity -> {
                    if (activity.getText().equals(expected)) {
                        System.out.println(
                            String.format(
                                "TestTurn(tid:%s): Validated text is: %s",
                                Thread.currentThread().getId(),
                                expected
                            )
                        );
                        System.out.flush();

                        return "SUCCESS";
                    }
                    System.out.println(
                        String.format(
                            "TestTurn(tid:%s): Failed validate text is: %s",
                            Thread.currentThread().getId(),
                            expected
                        )
                    );
                    System.out.flush();

                    return String.format(
                        "FAIL: %s received in Activity.text (%s expected)",
                        activity.getText(),
                        expected
                    );
                };

                System.out.println(
                    String.format(
                        "TestTurn(tid:%s): Started receive loop: %s",
                        Thread.currentThread().getId(),
                        description
                    )
                );
                System.out.flush();
                long start = System.currentTimeMillis();
                while (true) {
                    long current = System.currentTimeMillis();

                    if ((current - start) > (long) finalTimeout)
                        return String.format(
                            "TestTurn: %d ms Timed out waiting for:'%s'",
                            finalTimeout,
                            description
                        );

                    Activity replyActivity = this.adapter.getNextReply();

                    if (replyActivity != null) {
                        // if we have a reply
                        System.out.println(
                            String.format(
                                "TestTurn(tid:%s): Received Reply: %s",
                                Thread.currentThread().getId(),
                                String.format(
                                    "\n========\n To:%s\n From:%s\n Msg:%s\n=======",
                                    replyActivity.getRecipient().getName(),
                                    replyActivity.getFrom().getName(),
                                    replyActivity.getText()
                                )
                            )
                        );
                        System.out.flush();
                        return validateActivity.apply(replyActivity);
                    } else {
                        System.out.println(
                            String.format(
                                "TestTurn(tid:%s): No reply..",
                                Thread.currentThread().getId()
                            )
                        );
                        System.out.flush();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).get(timeout, TimeUnit.MILLISECONDS);
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
    public TestFlow test(String userSays, String expected) {
        return test(userSays, expected, null, 3000);
    }

    public TestFlow test(String userSays, String expected, String description) {
        return test(userSays, expected, description, 3000);
    }

    public TestFlow test(String userSays, String expected, String description, int timeout) {
        if (expected == null)
            throw new IllegalArgumentException("expected");

        return this.send(userSays).assertReply(expected, description, timeout);
    }

    /**
     * Test() -> shortcut for .Send(user).AssertReply(Expected)
     *
     * @param userSays
     * @param expected
     * @return
     */
    public TestFlow test(String userSays, Activity expected) {
        return test(userSays, expected, null, 3000);
    }

    public TestFlow test(String userSays, Activity expected, String description) {
        return test(userSays, expected, description, 3000);
    }

    public TestFlow test(String userSays, Activity expected, String description, int timeout) {
        if (expected == null)
            throw new IllegalArgumentException("expected");

        return this.send(userSays).assertReply(expected, description, timeout);
    }

    /**
     * Say() -> shortcut for .Send(user).AssertReply(Expected)
     *
     * @param userSays
     * @param expected
     * @return
     */
    public TestFlow test(String userSays, Consumer<Activity> expected) {
        return test(userSays, expected, null, 3000);
    }

    public TestFlow test(String userSays, Consumer<Activity> expected, String description) {
        return test(userSays, expected, description, 3000);
    }

    public TestFlow test(
        String userSays,
        Consumer<Activity> expected,
        String description,
        int timeout
    ) {
        if (expected == null)
            throw new IllegalArgumentException("expected");

        return this.send(userSays).assertReply(expected, description, timeout);
    }

    /**
     * Assert that reply is one of the candidate responses
     *
     * @param candidates
     * @return
     */
    public TestFlow assertReplyOneOf(String[] candidates) {
        return assertReplyOneOf(candidates, null, 3000);
    }

    public TestFlow assertReplyOneOf(String[] candidates, String description) {
        return assertReplyOneOf(candidates, description, 3000);
    }

    public TestFlow assertReplyOneOf(String[] candidates, String description, int timeout) {
        if (candidates == null)
            throw new IllegalArgumentException("candidates");

        return this.assertReply((reply) -> {
            for (String candidate : candidates) {
                if (StringUtils.equals(reply.getText(), candidate))
                    return;
            }
            throw new RuntimeException(
                String.format(
                    "%s: Not one of candidates: %s",
                    description,
                    String.join("\n ", candidates)
                )
            );
        }, description, timeout);
    }
}
