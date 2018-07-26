package com.microsoft.bot.builder;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.bot.schema.ActivityImpl;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ActivityTypes;
import com.microsoft.bot.schema.models.ResourceResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.LinkedList;


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

    private TranscriptLogger logger;
    private static final Logger log4j = LogManager.getLogger("BotFx");

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
    public void OnTurn(TurnContext context, NextDelegate next) throws Exception {
        // log incoming activity at beginning of turn
        if (context.getActivity() != null) {
            JsonNode role = null;
            if (context.getActivity().from() == null) {
                throw new RuntimeException("Activity does not contain From field");
            }
            if (context.getActivity().from().properties().containsKey("role")) {
                role = context.getActivity().from().properties().get("role");
            }

            if (role == null || StringUtils.isBlank(role.asText())) {
                context.getActivity().from().properties().put("role", mapper.createObjectNode().with("user"));
            }
            Activity activityTemp = ActivityImpl.CloneActity(context.getActivity());

            LogActivity(ActivityImpl.CloneActity(context.getActivity()));
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
                LogActivity(ActivityImpl.CloneActity(activity));
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
            Activity updateActivity = ActivityImpl.CloneActity(activity);
            updateActivity.withType(ActivityTypes.MESSAGE_UPDATE);
            LogActivity(updateActivity);
            return response;


        });

        // hook up delete activity pipeline
        context.OnDeleteActivity((ctxt, reference, nextDel) -> {
            // run full pipeline

            try {
                if (nextDel != null) {
                    log4j.error(String.format("Transcript logActivity next delegate: %s)", nextDel));
                    nextDel.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
                log4j.error(String.format("Transcript logActivity failed with %s (next delegate: %s)", e.toString(), nextDel));
                throw new RuntimeException(String.format("Transcript logActivity failed with %s", e.getMessage()));

            }

            // add MessageDelete activity
            // log as MessageDelete activity
            Activity deleteActivity = new Activity()
                    .withType(ActivityTypes.MESSAGE_DELETE)
                    .withId(reference.activityId())
                    .applyConversationReference(reference, false);

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
                this.logger.LogActivityAsync(activity);
            } catch (RuntimeException err) {
                log4j.error(String.format("Transcript poll failed : %1$s", err));
            }
        }

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



