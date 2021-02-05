// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * A no-op telemetry client.
 */
public class NullBotTelemetryClient implements BotTelemetryClient {
    @SuppressWarnings("checkstyle:ParameterNumber")
    @Override
    public void trackAvailability(
        String name,
        OffsetDateTime timeStamp,
        Duration duration,
        String runLocation,
        boolean success,
        String message,
        Map<String, String> properties,
        Map<String, Double> metrics
    ) {

    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    @Override
    public void trackDependency(
        String dependencyTypeName,
        String target,
        String dependencyName,
        String data,
        OffsetDateTime startTime,
        Duration duration,
        String resultCode,
        boolean success
    ) {

    }

    @Override
    public void trackEvent(
        String eventName,
        Map<String, String> properties,
        Map<String, Double> metrics
    ) {

    }

    @Override
    public void trackException(
        Exception exception,
        Map<String, String> properties,
        Map<String, Double> metrics
    ) {

    }

    @Override
    public void trackTrace(String message, Severity severityLevel, Map<String, String> properties) {

    }

    @Override
    public void flush() {

    }

    @Override
    public void trackDialogView(String dialogName, Map<String, String> properties, Map<String, Double> metrics) {

    }
}
