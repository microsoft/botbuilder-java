package com.microsoft.bot.builder.core;

import com.microsoft.bot.builder.core.TurnContext;
import com.microsoft.bot.schema.models.ConversationReference;

import java.util.concurrent.CompletableFuture;

/// <summary>
/// A method that can participate in delete activity events for the current turn.
/// </summary>
/// <param name="context">The context object for the turn.</param>
/// <param name="reference">The conversation containing the activity.</param>
/// <param name="next">The delegate to call to continue event processing.</param>
/// <returns>A task that represents the work queued to execute.</returns>
/// <remarks>A handler calls the <paramref name="next"/> delegate to pass control to
/// the next registered handler. If a handler doesn’t call the next delegate,
/// the adapter does not call any of the subsequent handlers and does not delete the
///activity.
/// <para>The conversation reference's <see cref="ConversationReference.ActivityId"/>
/// indicates the activity in the conversation to replace.</para>
/// </remarks>
/// <seealso cref="BotAdapter"/>
/// <seealso cref="SendActivitiesHandler"/>
/// <seealso cref="UpdateActivityHandler"/>
@FunctionalInterface
public interface DeleteActivityHandler {
    CompletableFuture handle(TurnContext context, ConversationReference reference, Func<CompletableFuture> next);
}
