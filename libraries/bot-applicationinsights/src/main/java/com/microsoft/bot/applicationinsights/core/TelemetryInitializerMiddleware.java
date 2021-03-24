// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.applicationinsights.core;

import com.microsoft.applicationinsights.core.dependencies.http.client.protocol.HttpClientContext;
import com.microsoft.applicationinsights.core.dependencies.http.protocol.HttpContext;
import com.microsoft.bot.builder.BotAssert;
import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.NextDelegate;
import com.microsoft.bot.builder.TelemetryLoggerMiddleware;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.Activity;

import java.util.concurrent.CompletableFuture;

/**
 * Middleware for storing incoming activity on the HttpContext.
 */
public class TelemetryInitializerMiddleware implements Middleware {

    private HttpContext httpContext;
    private final String botActivityKey = "BotBuilderActivity";
    private final TelemetryLoggerMiddleware telemetryLoggerMiddleware;
    private final Boolean logActivityTelemetry;

    /**
     * Initializes a new instance of the {@link TelemetryInitializerMiddleware}.
     * 
     * @param withTelemetryLoggerMiddleware The TelemetryLoggerMiddleware to use.
     * @param withLogActivityTelemetry      Boolean determining if you want to log
     *                                      telemetry activity
     */
    public TelemetryInitializerMiddleware(
        TelemetryLoggerMiddleware withTelemetryLoggerMiddleware,
        Boolean withLogActivityTelemetry
    ) {
        telemetryLoggerMiddleware = withTelemetryLoggerMiddleware;
        if (withLogActivityTelemetry == null) {
            withLogActivityTelemetry = true;
        }
        logActivityTelemetry = withLogActivityTelemetry;
    }

    /**
     * Stores the incoming activity as JSON in the items collection on the
     * HttpContext.
     * 
     * @param context The incoming TurnContext
     * @param next    Delegate to run next on
     * @return Returns a CompletableFuture with Void value
     */
    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
        BotAssert.contextNotNull(context);

        if (context.getActivity() != null) {
            Activity activity = context.getActivity();

            if (this.httpContext == null) {
                this.httpContext = HttpClientContext.create();
            }

            Object item = httpContext.getAttribute(botActivityKey);

            if (item != null) {
                httpContext.removeAttribute(botActivityKey);
            }

            httpContext.setAttribute(botActivityKey, activity);
        }

        if (logActivityTelemetry) {
            return telemetryLoggerMiddleware.onTurn(context, next);
        } else {
            return next.next();
        }
    }
}
