// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.builder;

import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ConversationReference;

import java.util.ArrayList;

/// <summary>
/// Provides methods for debugging Bot Builder code.
/// </summary>
public class BotAssert
{
    /// <summary>
    /// Checks that an activity object is not <c>null</c>.
    /// </summary>
    /// <param name="activity">The activity object.</param>
    /// <exception cref="ArgumentNullException">
    /// <paramref name="activity"/> is <c>null</c>.</exception>
    public static void ActivityNotNull(ActivityImpl activity)
    {
        if (activity == null)
            throw new IllegalArgumentException ("Activity");
    }

    /// <summary>
    /// Checks that an activity object is not <c>null</c>.
    /// </summary>
    /// <param name="activity">The activity object.</param>
    /// <exception cref="ArgumentNullException">
    /// <paramref name="activity"/> is <c>null</c>.</exception>
    public static void ActivityNotNull(Activity activity)
    {
        if (activity == null)
            throw new IllegalArgumentException ("Activity");
    }

    /// <summary>
    /// Checks that a context object is not <c>null</c>.
    /// </summary>
    /// <param name="context">The context object.</param>
    /// <exception cref="ArgumentNullException">
    /// <paramref name="context"/> is <c>null</c>.</exception>
    public static void ContextNotNull(TurnContext context)
    {
        if (context == null)
            throw new IllegalArgumentException ("TurnContext");
    }

    /// <summary>
    /// Checks that a conversation reference object is not <c>null</c>.
    /// </summary>
    /// <param name="reference">The conversation reference object.</param>
    /// <exception cref="ArgumentNullException">
    /// <paramref name="reference"/> is <c>null</c>.</exception>
    public static void ConversationReferenceNotNull(ConversationReference reference)
    {
        if (reference == null)
            throw new IllegalArgumentException ("ConversationReference");
    }

    /// <summary>
    /// Checks that an activity collection is not <c>null</c>.
    /// </summary>
    /// <param name="activities">The activities.</param>
    /// <exception cref="ArgumentNullException">
    /// <paramref name="activities"/> is <c>null</c>.</exception>
    public static void ActivityListNotNull(ArrayList<Activity> activities)
    {
        if (activities == null)
            throw new NullPointerException("List<Activity>");
    }

    /// <summary>
    /// Checks that a middleware object is not <c>null</c>.
    /// </summary>
    /// <param name="middleware">The middleware object.</param>
    /// <exception cref="ArgumentNullException">
    /// <paramref name="middleware"/> is <c>null</c>.</exception>
    public static void MiddlewareNotNull(Middleware middleware)
    {
        if (middleware == null)
            throw new NullPointerException("Middleware");
    }

    /// <summary>
    /// Checks that a middleware collection is not <c>null</c>.
    /// </summary>
    /// <param name="middleware">The middleware.</param>
    /// <exception cref="ArgumentNullException">
    /// <paramref name="middleware"/> is <c>null</c>.</exception>
    public static void MiddlewareNotNull(ArrayList<Middleware> middleware)
    {
        if (middleware == null)
            throw new NullPointerException("List<Middleware>");
    }
}
