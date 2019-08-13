package com.microsoft.bot.builder;


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.bot.schema.models.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * Represents a transcript logger that writes activites to a <see cref="Trace"/> object.
 */
public class TraceTranscriptLogger implements TranscriptLogger {
    // https://github.com/FasterXML/jackson-databind/wiki/Serialization-Features
    private static ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final Logger logger = LogManager.getLogger("HelloWorld");

    ForkJoinPool.ForkJoinWorkerThreadFactory factory = new ForkJoinPool.ForkJoinWorkerThreadFactory()
    {
        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool)
        {
            final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setName("BotTrace-" + worker.getPoolIndex());
            return worker;
        }
    };

    ExecutorService executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), factory, null, true);

    /**
     * Log an activity to the transcript.
     *
     * @param activity The activity to transcribe.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public void LogActivityAsync(Activity activity) {
            BotAssert.ActivityNotNull(activity);
            String event = null;
            try {
                event = mapper.writeValueAsString(activity);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            this.logger.info(event);
    }
}
