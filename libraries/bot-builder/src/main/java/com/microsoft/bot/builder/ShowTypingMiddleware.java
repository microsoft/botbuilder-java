// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.SkillValidation;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * When added, this middleware will send typing activities back to the user when
 * a Message activity is received to let them know that the bot has received the
 * message and is working on the response. You can specify a delay in
 * milliseconds before the first typing activity is sent and then a frequency,
 * also in milliseconds which determines how often another typing activity is
 * sent. Typing activities will continue to be sent until your bot sends another
 * message back to the user.
 */
public class ShowTypingMiddleware implements Middleware {
    private static final int DEFAULT_DELAY = 500;
    private static final int DEFAULT_PERIOD = 2000;

    /**
     * Initial delay before sending first typing indicator. Defaults to 500ms.
     */
    private long delay;

    /**
     * Rate at which additional typing indicators will be sent. Defaults to every
     * 2000ms.
     */
    private long period;

    /**
     * Constructs with default delay and period.
     */
    public ShowTypingMiddleware() {
        this(DEFAULT_DELAY, DEFAULT_PERIOD);
    }

    /**
     * Initializes a new instance of the ShowTypingMiddleware class.
     *
     * @param withDelay  Initial delay before sending first typing indicator.
     * @param withPeriod Rate at which additional typing indicators will be sent.
     * @throws IllegalArgumentException delay and period must be greater than zero
     */
    public ShowTypingMiddleware(long withDelay, long withPeriod) throws IllegalArgumentException {
        if (withDelay < 0) {
            throw new IllegalArgumentException("Delay must be greater than or equal to zero");
        }

        if (withPeriod < 0) {
            throw new IllegalArgumentException("Repeat period must be greater than zero");
        }

        delay = withDelay;
        period = withPeriod;
    }

    /**
     * Processes an incoming activity.
     *
     * @param turnContext The context object for this turn.
     * @param next        The delegate to call to continue the bot middleware
     *                    pipeline.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
        if (!turnContext.getActivity().isType(ActivityTypes.MESSAGE) || isSkillBot(turnContext)) {
            return next.next();
        }

        // do not await task - we want this to run in the background and we will cancel
        // it when its done
        CompletableFuture sendFuture = sendTyping(turnContext, delay, period);
        return next.next().thenAccept(result -> sendFuture.cancel(true));
    }

    private static Boolean isSkillBot(TurnContext turnContext) {
        Object identity = turnContext.getTurnState().get(BotAdapter.BOT_IDENTITY_KEY);
        if (identity instanceof ClaimsIdentity) {
            ClaimsIdentity claimsIdentity = (ClaimsIdentity) identity;
            return SkillValidation.isSkillClaim(claimsIdentity.claims());
        } else {
            return false;
        }
    }

    private static CompletableFuture<Void> sendTyping(
        TurnContext turnContext,
        long delay,
        long period
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delay);

                while (!Thread.currentThread().isInterrupted()) {
                    sendTypingActivity(turnContext).join();
                    Thread.sleep(period);
                }
            } catch (InterruptedException e) {
                // do nothing
            }
        }, ExecutorFactory.getExecutor());
    }

    private static CompletableFuture<ResourceResponse[]> sendTypingActivity(
        TurnContext turnContext
    ) {
        // create a TypingActivity, associate it with the conversation and send
        // immediately
        Activity typingActivity = new Activity(ActivityTypes.TYPING);
        typingActivity.setRelatesTo(turnContext.getActivity().getRelatesTo());

        // sending the Activity directly on the Adapter avoids other Middleware and
        // avoids setting the Responded
        // flag, however, this also requires that the conversation reference details are
        // explicitly added.
        ConversationReference conversationReference = turnContext.getActivity()
            .getConversationReference();
        typingActivity.applyConversationReference(conversationReference);

        // make sure to send the Activity directly on the Adapter rather than via the
        // TurnContext
        return turnContext.getAdapter()
            .sendActivities(turnContext, Collections.singletonList(typingActivity));
    }
}
