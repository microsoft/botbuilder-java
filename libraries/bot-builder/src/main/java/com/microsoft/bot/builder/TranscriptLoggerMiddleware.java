// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityEventNames;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.RoleTypes;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.lang3.StringUtils;

/**
 * When added, this middleware will log incoming and outgoing activities to a
 * TranscriptStore.
 */
public class TranscriptLoggerMiddleware implements Middleware {

    /**
     * The TranscriptLogger to log to.
     */
    private TranscriptLogger transcriptLogger;

    /**
     * Activity queue.
     */
    private Queue<Activity> transcript = new ConcurrentLinkedQueue<Activity>();

    /**
     * Initializes a new instance of the <see cref="TranscriptLoggerMiddleware"/>
     * class.
     *
     * @param withTranscriptLogger The transcript logger to use.
     */
    public TranscriptLoggerMiddleware(TranscriptLogger withTranscriptLogger) {
        if (withTranscriptLogger == null) {
            throw new IllegalArgumentException(
                "TranscriptLoggerMiddleware requires a ITranscriptLogger implementation."
            );
        }

        transcriptLogger = withTranscriptLogger;
    }

    /**
     * Records incoming and outgoing activities to the conversation store.
     *
     * @param context The context object for this turn.
     * @param next    The delegate to call to continue the bot middleware pipeline.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
        // log incoming activity at beginning of turn
        if (context.getActivity() != null) {
            if (context.getActivity().getFrom() == null) {
                context.getActivity().setFrom(new ChannelAccount());
            }

            if (context.getActivity().getFrom().getProperties().get("role") == null
                && context.getActivity().getFrom().getRole() == null
            ) {
                context.getActivity().getFrom().setRole(RoleTypes.USER);
            }

            // We should not log ContinueConversation events used by skills to initialize the middleware.
            if (!(context.getActivity().isType(ActivityTypes.EVENT)
                && StringUtils.equals(context.getActivity().getName(), ActivityEventNames.CONTINUE_CONVERSATION))
            ) {
                logActivity(Activity.clone(context.getActivity()), true);
            }
        }

        // hook up onSend pipeline
        context.onSendActivities(
            (ctx, activities, nextSend) -> {
                // run full pipeline
                return nextSend.get().thenApply(responses -> {
                    for (Activity activity : activities) {
                        logActivity(Activity.clone(activity), false);
                    }

                    return responses;
                });
            }
        );

        // hook up update activity pipeline
        context.onUpdateActivity(
            (ctx, activity, nextUpdate) -> {
                // run full pipeline
                return nextUpdate.get().thenApply(resourceResponse -> {
                    // add Message Update activity
                    Activity updateActivity = Activity.clone(activity);
                    updateActivity.setType(ActivityTypes.MESSAGE_UPDATE);
                    logActivity(updateActivity, false);

                    return resourceResponse;
                });
            }
        );

        // hook up delete activity pipeline
        context.onDeleteActivity(
            (ctx, reference, nextDel) -> {
                // run full pipeline
                return nextDel.get().thenApply(nextDelResult -> {
                    // add MessageDelete activity
                    // log as MessageDelete activity
                    Activity deleteActivity = new Activity(ActivityTypes.MESSAGE_DELETE);
                    deleteActivity.setId(reference.getActivityId());
                    deleteActivity.applyConversationReference(reference, false);

                    logActivity(deleteActivity, false);

                    return null;
                });
            }
        );

        // process bot logic
        return next.next()
            .thenAccept(
                nextResult -> {
                    // flush transcript at end of turn
                    while (!transcript.isEmpty()) {
                        Activity activity = transcript.poll();
                        transcriptLogger.logActivity(activity);
                    }
                }
            );
    }

    private void logActivity(Activity activity, boolean incoming) {
        if (activity.getTimestamp() == null) {
            activity.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));
        }

        if (activity.getFrom() == null) {
            activity.setFrom(new ChannelAccount());
        }

        if (activity.getFrom().getRole() == null) {
            activity.getFrom().setRole(incoming ? RoleTypes.USER : RoleTypes.BOT);
        }

        transcript.offer(activity);
    }
}
