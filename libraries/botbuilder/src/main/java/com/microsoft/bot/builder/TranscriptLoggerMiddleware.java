package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ActivityTypes;
import com.microsoft.bot.schema.models.ResourceResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;


/**
 * When added, this middleware will log incoming and outgoing activitites to a ITranscriptStore.
 */
public class TranscriptLoggerMiddleware implements Middleware {
    // https://github.com/FasterXML/jackson-databind/wiki/Serialization-Features
    private static ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private TranscriptLogger logger;
    private static final Logger log4j = LogManager.getLogger("HelloWorld");

    private LinkedList<Activity> transcript = new LinkedList<Activity>();

    /**
     * Initializes a new instance of the <see cref="TranscriptLoggerMiddleware"/> class.
     *
     * @param transcriptLogger The transcript logger to use.
     */
    public TranscriptLoggerMiddleware(TranscriptLogger transcriptLogger) {
        if (transcriptLogger == null)
            throw new NullPointerException("TranscriptLoggerMiddleware requires a ITranscriptLogger implementation.  ");

        this.logger = transcriptLogger;

    }

    /**
     * initialization for middleware turn.
     *
     * @param context
     * @param next
     * @return
     */
    @Override
    public CompletableFuture OnTurn(TurnContext context, NextDelegate next) throws Exception {
        return CompletableFuture.runAsync(() -> {
            // log incoming activity at beginning of turn
            if (context.getActivity() != null) {
                JsonNode role = null;
                if (context.getActivity().from().properties().containsKey("role")) {
                    role = context.getActivity().from().properties().get("role");
                }

                if (role == null || StringUtils.isBlank(role.asText())){
                    context.getActivity().from().properties().put("role", mapper.createObjectNode().with("user"));
                }
                LogActivity(CloneActivity(context.getActivity()));
            }

            // hook up onSend pipeline
            context.OnSendActivities((ctx, activities, nextSend) ->
            {
                return CompletableFuture.supplyAsync(() -> {
                    // run full pipeline
                    ResourceResponse[] responses = new ResourceResponse[0];
                    try {
                        responses = await(nextSend.call());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    for (Activity activity : activities) {
                        LogActivity(CloneActivity(activity));
                    }

                    return responses;

                });
            });

            // hook up update activity pipeline
            context.OnUpdateActivity((ctx, activity, nextUpdate) ->
            {
                return CompletableFuture.supplyAsync(() -> {
                    // run full pipeline
                    ResourceResponse response = null;
                    try {
                        response = await(nextUpdate.call());
                    } catch (Exception e) {
                        e.printStackTrace();


                        throw new RuntimeException(String.format("Error on Logging.OnUpdateActivity : %s", e.toString()));
                    }

                    // add Message Update activity
                    Activity updateActivity = CloneActivity(activity);
                    updateActivity.withType(ActivityTypes.MESSAGE_UPDATE);
                    LogActivity(updateActivity);
                    return response;
                });

            });

            // hook up delete activity pipeline
            context.OnDeleteActivity((ctxt, reference, nextDel) -> {
                return CompletableFuture.runAsync(() -> {
                    // run full pipeline
                    try {
                        await(nextDel.call());
                    } catch (Exception e) {
                        e.printStackTrace();
                        log4j.error(String.format("Transcript logActivity failed with %s", e.toString()));
                    }

                    // add MessageDelete activity
                    // log as MessageDelete activity
                    Activity deleteActivity = new Activity()
                            .withType(ActivityTypes.MESSAGE_DELETE)
                            .withId(reference.activityId());

                    LogActivity(deleteActivity);
                    return;
                });

            });


            // process bot logic
            try {
                await(next.next());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(String.format("Error on Logging.next : %s", e.toString()));
            }

            // flush transcript at end of turn
            while (!transcript.isEmpty()) {
                try {
                    Activity activity = transcript.poll();
                    await(logger.LogActivityAsync(activity));
                } catch (RuntimeException err) {
                    log4j.error(String.format("Transcript logActivity failed with %1$s", err));
                }
            }

        });
    }

    private static Activity CloneActivity(Activity activity) {
        try {
            activity = mapper.treeToValue(mapper.valueToTree(activity), Activity.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return activity;
    }

    private void LogActivity(Activity activity) {
        synchronized (transcript) {
            if (activity.timestamp() == null) {
                activity.withTimestamp(DateTime.now(DateTimeZone.UTC));
            }
            transcript.offer(activity);
        }
    }

}



