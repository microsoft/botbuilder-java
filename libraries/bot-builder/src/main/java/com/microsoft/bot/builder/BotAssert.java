// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.builder;

import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ConversationReference;

import java.util.ArrayList;

/**
 * Provides methods for debugging Bot Builder code.
 */
public class BotAssert
{
    /**
     * Checks that an activity object is not {@code null}.
     * @param activity The activity object.
     * @throws NullPointerException 
     * {@code activity} is {@code null}.
     */
    public static void ActivityNotNull(ActivityImpl activity)
    {
        if (activity == null)
            throw new IllegalArgumentException ("Activity");
    }

    /**
     * Checks that an activity object is not {@code null}.
     * @param activity The activity object.
     * @throws NullPointerException 
     * {@code activity} is {@code null}.
     */
    public static void ActivityNotNull(Activity activity)
    {
        if (activity == null)
            throw new IllegalArgumentException ("Activity");
    }

    /**
     * Checks that a context object is not {@code null}.
     * @param context The context object.
     * @throws NullPointerException 
     * {@code context} is {@code null}.
     */
    public static void ContextNotNull(TurnContext context)
    {
        if (context == null)
            throw new IllegalArgumentException ("TurnContext");
    }

    /**
     * Checks that a conversation reference object is not {@code null}.
     * @param reference The conversation reference object.
     * @throws NullPointerException 
     * {@code reference} is {@code null}.
     */
    public static void ConversationReferenceNotNull(ConversationReference reference)
    {
        if (reference == null)
            throw new IllegalArgumentException ("ConversationReference");
    }

    /**
     * Checks that an activity collection is not {@code null}.
     * @param activities The activities.
     * @throws NullPointerException 
     * {@code activities} is {@code null}.
     */
    public static void ActivityListNotNull(ArrayList<Activity> activities)
    {
        if (activities == null)
            throw new NullPointerException("List<Activity>");
    }

    /**
     * Checks that a middleware object is not {@code null}.
     * @param middleware The middleware object.
     * @throws NullPointerException 
     * {@code middleware} is {@code null}.
     */
    public static void MiddlewareNotNull(Middleware middleware)
    {
        if (middleware == null)
            throw new NullPointerException("Middleware");
    }

    /**
     * Checks that a middleware collection is not {@code null}.
     * @param middleware The middleware.
     * @throws NullPointerException 
     * {@code middleware} is {@code null}.
     */
    public static void MiddlewareNotNull(ArrayList<Middleware> middleware)
    {
        if (middleware == null)
            throw new NullPointerException("List<Middleware>");
    }
}
