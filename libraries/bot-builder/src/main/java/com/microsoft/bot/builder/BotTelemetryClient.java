// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Logging client for Bot Telemetry.
 */
public interface BotTelemetryClient {

    /**
     * Send information about availability of an application.
     *
     * @param name        Availability test name.
     * @param timeStamp   The time when the availability was captured.
     * @param duration    The time taken for the availability test to run.
     * @param runLocation Name of the location the availability test was run from.
     * @param success     True if the availability test ran successfully.
     */
    default void trackAvailability(
        String name,
        OffsetDateTime timeStamp,
        Duration duration,
        String runLocation,
        boolean success
    ) {
        trackAvailability(name, timeStamp, duration, runLocation, success, null, null, null);
    }

    /**
     * Send information about availability of an application.
     *
     * @param name        Availability test name.
     * @param timeStamp   The time when the availability was captured.
     * @param duration    The time taken for the availability test to run.
     * @param runLocation Name of the location the availability test was run from.
     * @param success     True if the availability test ran successfully.
     * @param message     Error message on availability test run failure.
     * @param properties  Named string values you can use to classify and search for
     *                    this availability telemetry.
     * @param metrics     Additional values associated with this availability
     *                    telemetry.
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    void trackAvailability(
        String name,
        OffsetDateTime timeStamp,
        Duration duration,
        String runLocation,
        boolean success,
        String message,
        Map<String, String> properties,
        Map<String, Double> metrics
    );

    /**
     * Send information about an external dependency (outgoing call) in the
     * application.
     *
     * @param dependencyTypeName Name of the command initiated with this dependency
     *                           call. Low cardinality value. Examples are SQL,
     *                           Azure table, and HTTP.
     * @param target             External dependency target.
     * @param dependencyName     Name of the command initiated with this dependency
     *                           call. Low cardinality value. Examples are stored
     *                           procedure name and URL path template.
     * @param data               Command initiated by this dependency call. Examples
     *                           are SQL statement and HTTP URL's with all query
     *                           parameters.
     * @param startTime          The time when the dependency was called.
     * @param duration           The time taken by the external dependency to handle
     *                           the call.
     * @param resultCode         Result code of dependency call execution.
     * @param success            True if the dependency call was handled
     *                           successfully.
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    void trackDependency(
        String dependencyTypeName,
        String target,
        String dependencyName,
        String data,
        OffsetDateTime startTime,
        Duration duration,
        String resultCode,
        boolean success
    );

    /**
     * Logs custom events with extensible named fields.
     *
     * @param eventName A name for the event.
     */
    default void trackEvent(String eventName) {
        trackEvent(eventName, null, null);
    }

    /**
     * Logs custom events with extensible named fields.
     *
     * @param eventName  A name for the event.
     * @param properties Named string values you can use to search and classify
     *                   events.
     */
    default void trackEvent(String eventName, Map<String, String> properties) {
        trackEvent(eventName, properties, null);
    }

    /**
     * Logs custom events with extensible named fields.
     *
     * @param eventName  A name for the event.
     * @param properties Named string values you can use to search and classify
     *                   events.
     * @param metrics    Measurements associated with this event.
     */
    void trackEvent(String eventName, Map<String, String> properties, Map<String, Double> metrics);

    /**
     * Logs a system exception.
     *
     * @param exception The exception to log.
     */
    default void trackException(Exception exception) {
        trackException(exception, null, null);
    }

    /**
     * Logs a system exception.
     *
     * @param exception  The exception to log.
     * @param properties Named string values you can use to classify and search for
     *                   this exception.
     * @param metrics    Additional values associated with this exception.
     */
    void trackException(
        Exception exception,
        Map<String, String> properties,
        Map<String, Double> metrics
    );

    /**
     * Send a trace message.
     *
     * @param message       Message to display.
     * @param severityLevel Trace severity level.
     * @param properties    Named string values you can use to search and classify
     *                      events.
     */
    void trackTrace(String message, Severity severityLevel, Map<String, String> properties);

    /**
     * Log a DialogView using the TrackPageView method on the IBotTelemetryClient if
     * IBotPageViewTelemetryClient has been implemented. Alternatively log the information out via
     * TrackTrace.
     *
     * @param dialogName       The name of the dialog to log the entry / start for.
     * @param properties       Named string values you can use to search and classify
     *                         events.
     * @param metrics          Measurements associated with this event.
     */
    void trackDialogView(String dialogName, Map<String, String> properties, Map<String, Double> metrics);

    /**
     * Flushes the in-memory buffer and any metrics being pre-aggregated.
     */
    void flush();
}
