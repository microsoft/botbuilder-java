// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient;

/**
 * Describes the level of HTTP traffic to log.
 */
public enum LogLevel {
    /**
     * Logging is turned off.
     */
    NONE,

    /**
     * Logs only URLs, HTTP methods, and time to finish the request.
     */
    BASIC,

    /**
     * Logs everything in BASIC, plus all the request and response headers.
     */
    HEADERS,

    /**
     * Logs everything in BASIC, plus all the request and response body.
     * Note that only payloads in plain text or plan text encoded in GZIP
     * will be logged.
     */
    BODY,

    /**
     * Logs everything in HEADERS and BODY.
     */
    BODY_AND_HEADERS;

    private boolean prettyJson = false;

    /**
     * @return if the JSON payloads will be prettified when log level is set
     * to BODY or BODY_AND_HEADERS. Default is false.
     */
    public boolean isPrettyJson() {
        return prettyJson;
    }

    /**
     * Specifies whether to log prettified JSON.
     * @param prettyJson true if JSON paylods are prettified.
     * @return the enum object
     */
    public LogLevel withPrettyJson(boolean prettyJson) {
        this.prettyJson = prettyJson;
        return this;
    }
}
