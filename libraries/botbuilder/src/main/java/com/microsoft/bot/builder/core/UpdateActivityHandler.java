package com.microsoft.bot.builder.core;
import com.microsoft.bot.builder.core.Func;
import com.microsoft.bot.builder.core.TurnContext;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ResourceResponse;

import java.util.concurrent.CompletableFuture;

/// <summary>
/// A method that can participate in update activity events for the current turn.
/// </summary>
/// <param name="context">The context object for the turn.</param>
/// <param name="activity">The replacement activity.</param>
/// <param name="next">The delegate to call to continue event processing.</param>
/// <returns>A task that represents the work queued to execute.</returns>
/// <remarks>A handler calls the <paramref name="next"/> delegate to pass control to
/// the next registered handler. If a handler doesn’t call the next delegate,
/// the adapter does not call any of the subsequent handlers and does not update the
/// activity.
/// <para>The activity's <see cref="IActivity.Id"/> indicates the activity in the
/// conversation to replace.</para>
/// </remarks>
/// <seealso cref="BotAdapter"/>
/// <seealso cref="SendActivitiesHandler"/>
/// <seealso cref="DeleteActivityHandler"/>



// public delegate Task<ResourceResponse> UpdateActivityHandler(ITurnContext context, Activity activity, Func<Task<ResourceResponse>> next);
@FunctionalInterface
public interface UpdateActivityHandler {
    CompletableFuture<ResourceResponse> handle(TurnContext context, Activity activity, Func<CompletableFuture<ResourceResponse[]>> next);
}
