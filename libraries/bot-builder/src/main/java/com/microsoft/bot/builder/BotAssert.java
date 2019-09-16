// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods for debugging Bot Builder code.
 */
public final class BotAssert {
    /**
     * This class can't be created.
     */
    private BotAssert() {

    }

    /**
     * Checks that an activity object is not {@code null}.
     *
     * @param activity The activity object.
     * @throws NullPointerException {@code activity} is {@code null}.
     */
    public static void activityNotNull(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity");
        }
    }

    /**
     * Checks that a context object is not {@code null}.
     *
     * @param context The context object.
     * @throws NullPointerException {@code context} is {@code null}.
     */
    public static void contextNotNull(TurnContext context) {
        if (context == null) {
            throw new IllegalArgumentException("TurnContext");
        }
    }

    /**
     * Checks that a conversation reference object is not {@code null}.
     *
     * @param reference The conversation reference object.
     * @throws NullPointerException {@code reference} is {@code null}.
     */
    public static void conversationReferenceNotNull(ConversationReference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("ConversationReference");
        }
    }

    /**
     * Checks that an activity collection is not {@code null}.
     *
     * @param activities The activities.
     * @throws NullPointerException {@code activities} is {@code null}.
     */
    public static void activityListNotNull(List<Activity> activities) {
        if (activities == null) {
            throw new NullPointerException("List<Activity>");
        }
    }

    /**
     * Checks that a middleware object is not {@code null}.
     *
     * @param middleware The middleware object.
     * @throws NullPointerException {@code middleware} is {@code null}.
     */
    public static void middlewareNotNull(Middleware middleware) {
        if (middleware == null) {
            throw new NullPointerException("Middleware");
        }
    }

    /**
     * Checks that a middleware collection is not {@code null}.
     *
     * @param middleware The middleware.
     * @throws NullPointerException {@code middleware} is {@code null}.
     */
    public static void middlewareNotNull(ArrayList<Middleware> middleware) {
        if (middleware == null) {
            throw new NullPointerException("List<Middleware>");
        }
    }
}
