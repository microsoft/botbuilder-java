// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

/**
 * Represents middleware that can operate on incoming activities. A
 * {@link BotAdapter} passes incoming activities from the user's channel to the
 * middleware's {@link #onTurn(TurnContext, NextDelegate)} method.
 * <p>
 * You can add middleware objects to your adapter’s middleware collection. The
 * adapter processes and directs incoming activities in through the bot
 * middleware pipeline to your bot’s logic and then back out again. As each
 * activity flows in and out of the bot, each piece of middleware can inspect or
 * act upon the activity, both before and after the bot logic runs.
 * </p>
 * <p>
 * For each activity, the adapter calls middleware in the order in which you
 * added it.
 * </p>
 *
 * This defines middleware that sends "before" and "after" messages before and
 * after the adapter calls the bot's {@link Bot#onTurn(TurnContext)} method.
 *
 * {@code
 * public class SampleMiddleware : Middleware
 * {
 *    public async Task OnTurn(TurnContext context, MiddlewareSet.NextDelegate next)
 *    {
 *       context.SendActivity("before"); await next().ConfigureAwait(false);
 * context.SendActivity("after"); } } }
 *
 * {@link Bot}
 */
public interface Middleware {
    /**
     * Processes an incoming activity.
     *
     * @param turnContext The context object for this turn.
     * @param next        The delegate to call to continue the bot middleware
     *                    pipeline.
     * @return A task that represents the work queued to execute. Middleware calls
     *         the {@code next} delegate to pass control to the next middleware in
     *         the pipeline. If middleware doesn’t call the next delegate, the
     *         adapter does not call any of the subsequent middleware’s request
     *         handlers or the bot’s receive handler, and the pipeline short
     *         circuits.
     *         <p>
     *         The {@code context} provides information about the incoming activity,
     *         and other data needed to process the activity.
     *         </p>
     *         <p>
     *         {@link TurnContext} {@link com.microsoft.bot.schema.Activity}
     */
    CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next);
}
