// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.applicationinsights;

import com.microsoft.bot.builder.Severity;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

public class MyBotTelemetryClient extends ApplicationInsightsBotTelemetryClient {
    public MyBotTelemetryClient(String instrumentationKey) {
        super(instrumentationKey);
    }

    @Override
    public void trackDependency(
        String dependencyTypeName,
        String target,
        String dependencyName,
        String data,
        OffsetDateTime startTime,
        Duration duration,
        String resultCode,
        boolean success)
    {
        super.trackDependency(dependencyName, target, dependencyName, data, startTime, duration, resultCode, success);
    }

    @Override
    public void trackAvailability(
        String name,
        OffsetDateTime timeStamp,
        Duration duration,
        String runLocation,
        boolean success,
        String message,
        Map<String, String> properties,
        Map<String, Double> metrics)
    {
        super.trackAvailability(name, timeStamp, duration, runLocation, success, message, properties, metrics);
    }

    @Override
    public void trackEvent(
        String eventName,
        Map<String, String> properties,
        Map<String, Double> metrics)
    {
        super.trackEvent(eventName, properties, metrics);
    }

    @Override
    public void trackException(
        Exception exception,
        Map<String, String> properties,
        Map<String, Double> metrics)
    {
        super.trackException(exception, properties, metrics);
    }

    @Override
    public void trackTrace(
        String message,
        Severity severityLevel,
        Map<String, String> properties)
    {
        super.trackTrace(message, severityLevel, properties);
    }

    @Override
    public void trackPageView(
        String name,
        Map<String, String> properties,
        Map<String, Double> metrics)
    {
        super.trackPageView(name, properties, metrics);
    }

    @Override
    public void flush()
    {
        super.flush();
    }
}
