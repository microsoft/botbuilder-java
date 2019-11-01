// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * When added, this middleware will log incoming and outgoing activities to a TranscriptStore.
 */
public class TranscriptLoggerMiddleware implements Middleware {
    /**
     * The TranscriptLogger to log to.
     */
    private TranscriptLogger transcriptLogger;

    /**
     * Initializes a new instance of the <see cref="TranscriptLoggerMiddleware"/> class.
     *
     * @param withTranscriptLogger The transcript logger to use.
     */
    public TranscriptLoggerMiddleware(TranscriptLogger withTranscriptLogger) {
        if (withTranscriptLogger == null) {
            throw new IllegalArgumentException(
                "TranscriptLoggerMiddleware requires a ITranscriptLogger implementation.");
        }

        transcriptLogger = withTranscriptLogger;
    }

    /**
     * Records incoming and outgoing activities to the conversation store.
     *
     * @param context The context object for this turn.
     * @param next The delegate to call to continue the bot middleware pipeline.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
        Queue<Activity> transcript = new ConcurrentLinkedQueue<>();

        // log incoming activity at beginning of turn
        if (context.getActivity() != null) {
            if (context.getActivity().getFrom() == null) {
                context.getActivity().setFrom(new ChannelAccount());
            }

            JsonNode role = null;
            if (context.getActivity().getFrom().getProperties().containsKey("role")) {
                role = context.getActivity().getFrom().getProperties().get("role");
            }

            if (role == null || StringUtils.isBlank(role.asText())) {
                context.getActivity().getFrom().getProperties().put("role", new TextNode("user"));
            }

            logActivity(transcript, Activity.clone(context.getActivity()));
        }

        // hook up onSend pipeline
        context.onSendActivities((ctx, activities, nextSend) -> {
            // run full pipeline
            return nextSend.get()
                .thenApply(responses -> {
                    for (Activity activity : activities) {
                        logActivity(transcript, Activity.clone(activity));
                    }

                    return responses;
                });
        });

        // hook up update activity pipeline
        context.onUpdateActivity((ctx, activity, nextUpdate) -> {
            // run full pipeline
            return nextUpdate.get()
                .thenApply(resourceResponse -> {
                    // add Message Update activity
                    Activity updateActivity = Activity.clone(activity);
                    updateActivity.setType(ActivityTypes.MESSAGE_UPDATE);
                    logActivity(transcript, updateActivity);

                    return resourceResponse;
                });
        });

        // hook up delete activity pipeline
        context.onDeleteActivity((ctx, reference, nextDel) -> {
            // run full pipeline
            return nextDel.get()
                .thenApply(nextDelResult -> {
                    // add MessageDelete activity
                    // log as MessageDelete activity
                    Activity deleteActivity = new Activity(ActivityTypes.MESSAGE_DELETE) {{
                        setId(reference.getActivityId());
                        applyConversationReference(reference, false);
                    }};

                    logActivity(transcript, deleteActivity);

                    return null;
                });
        });


        // process bot logic
        return next.next()
            .thenAccept(nextResult -> {
                // flush transcript at end of turn
                while (!transcript.isEmpty()) {
                    Activity activity = transcript.poll();
                    transcriptLogger.logActivity(activity);
                }
            });
    }

    private void logActivity(Queue transcript, Activity activity) {
        if (activity.getTimestamp() == null) {
            activity.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));
        }
        transcript.offer(activity);
    }
}



