// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * When added, this middleware will log incoming and outgoing activities to a TranscriptStore.
 */
public class TranscriptLoggerMiddleware implements Middleware {
    private static final Logger logger = LoggerFactory.getLogger(TranscriptLoggerMiddleware.class);
    // https://github.com/FasterXML/jackson-databind/wiki/Serialization-Features
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
        mapper.findAndRegisterModules();
    }

    private TranscriptLogger transcriptLogger;
    private Queue<Activity> transcript = new ConcurrentLinkedQueue<Activity>();

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
     * initialization for middleware turn.
     *
     * @param context
     * @param next
     * @return
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
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
                context.getActivity().getFrom().getProperties().put("role", mapper.createObjectNode().with("user"));
            }

            logActivity(Activity.clone(context.getActivity()));
        }

        // hook up onSend pipeline
        context.onSendActivities((ctx, activities, nextSend) -> {
            // run full pipeline
            return nextSend.get()
                .thenApply(responses -> {
                    for (Activity activity : activities) {
                        logActivity(Activity.clone(activity));
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
                    logActivity(updateActivity);

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

                    logActivity(deleteActivity);

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

    private void logActivity(Activity activity) {
        if (activity.getTimestamp() == null) {
            activity.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));
        }
        transcript.offer(activity);
    }
}



