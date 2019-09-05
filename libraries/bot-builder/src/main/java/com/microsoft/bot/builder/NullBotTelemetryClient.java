// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Map;

public class NullBotTelemetryClient implements BotTelemetryClient {
    @Override
    public void trackAvailability(String name, DateTime timeStamp, Duration duration, String runLocation, boolean success, String message, Map<String, String> properties, Map<String, Double> metrics) {

    }

    @Override
    public void trackDependency(String dependencyTypeName, String target, String dependencyName, String data, DateTime startTime, Duration duration, String resultCode, boolean success) {

    }

    @Override
    public void trackEvent(String eventName, Map<String, String> properties, Map<String, Double> metrics) {

    }

    @Override
    public void trackException(Exception exception, Map<String, String> properties, Map<String, Double> metrics) {

    }

    @Override
    public void trackTrace(String message, Severity severityLevel, Map<String, String> properties) {

    }

    @Override
    public void flush() {

    }
}
