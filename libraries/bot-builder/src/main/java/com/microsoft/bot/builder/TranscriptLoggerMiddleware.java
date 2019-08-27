package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ResourceResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * When added, this middleware will log incoming and outgoing activitites to a ITranscriptStore.
 */
public class TranscriptLoggerMiddleware implements Middleware {
    // https://github.com/FasterXML/jackson-databind/wiki/Serialization-Features
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        mapper.findAndRegisterModules();
    }

    private TranscriptLogger transcriptLogger;
    private static final Logger logger = LoggerFactory.getLogger(TranscriptLoggerMiddleware.class);

    private Queue<Activity> transcript = new ConcurrentLinkedQueue<Activity>();

    /**
     * Initializes a new instance of the <see cref="TranscriptLoggerMiddleware"/> class.
     *
     * @param transcriptLogger The transcript logger to use.
     */
    public TranscriptLoggerMiddleware(TranscriptLogger transcriptLogger) {
        if (transcriptLogger == null)
            throw new NullPointerException("TranscriptLoggerMiddleware requires a ITranscriptLogger implementation.  ");

        this.transcriptLogger = transcriptLogger;

    }

    /**
     * initialization for middleware turn.
     *
     * @param context
     * @param next
     * @return
     */
    @Override
    public void OnTurn(TurnContext context, NextDelegate next) throws Exception {
        // log incoming activity at beginning of turn
        if (context.getActivity() != null) {
            JsonNode role = null;
            if (context.getActivity().getFrom() == null) {
                throw new RuntimeException("Activity does not contain From field");
            }
            if (context.getActivity().getFrom().getProperties().containsKey("role")) {
                role = context.getActivity().getFrom().getProperties().get("role");
            }

            if (role == null || StringUtils.isBlank(role.asText())) {
                context.getActivity().getFrom().getProperties().put("role", mapper.createObjectNode().with("user"));
            }
            Activity activityTemp = Activity.clone(context.getActivity());

            LogActivity(Activity.clone(context.getActivity()));
        }

        // hook up onSend pipeline
        context.OnSendActivities((ctx, activities, nextSend) ->
        {

            // run full pipeline
            ResourceResponse[] responses = new ResourceResponse[0];
            try {
                if (nextSend != null) {
                    responses = nextSend.call();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (Activity activity : activities) {
                LogActivity(Activity.clone(activity));
            }

            return responses;


        });

        // hook up update activity pipeline
        context.OnUpdateActivity((ctx, activity, nextUpdate) ->
        {

            // run full pipeline
            ResourceResponse response = null;
            try {
                if (nextUpdate != null) {
                    response = nextUpdate.call();
                }
            } catch (Exception e) {
                e.printStackTrace();


                throw new RuntimeException(String.format("Error on Logging.OnUpdateActivity : %s", e.toString()));
            }

            // add Message Update activity
            Activity updateActivity = Activity.clone(activity);
            updateActivity.setType(ActivityTypes.MESSAGE_UPDATE);
            LogActivity(updateActivity);
            return response;


        });

        // hook up delete activity pipeline
        context.OnDeleteActivity((ctxt, reference, nextDel) -> {
            // run full pipeline

            try {
                if (nextDel != null) {
                    logger.error(String.format("Transcript logActivity next delegate: %s)", nextDel));
                    nextDel.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(String.format("Transcript logActivity failed with %s (next delegate: %s)", e.toString(), nextDel));
                throw new RuntimeException(String.format("Transcript logActivity failed with %s", e.getMessage()));

            }

            // add MessageDelete activity
            // log as MessageDelete activity
            Activity deleteActivity = new Activity(ActivityTypes.MESSAGE_DELETE) {{
                setId(reference.getActivityId());
                applyConversationReference(reference, false);
            }};

            LogActivity(deleteActivity);
            return;

        });


        // process bot logic
        try {
            next.next();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Error on Logging.next : %s", e.toString()));
        }

        // flush transcript at end of turn
        while (!transcript.isEmpty()) {
            Activity activity = transcript.poll();
            try {
                this.transcriptLogger.LogActivityAsync(activity);
            } catch (RuntimeException err) {
                logger.error(String.format("Transcript poll failed : %1$s", err));
            }
        }

    }


    private void LogActivity(Activity activity) {
        if (activity.getTimestamp() == null) {
            activity.setTimestamp(DateTime.now(DateTimeZone.UTC));
        }
        transcript.offer(activity);
    }

}



