// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.inspection;

import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.NextDelegate;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.Activity;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public abstract class InterceptionMiddleware implements Middleware {
    private Logger logger;

    public static class Intercept {
        public Intercept(boolean forward, boolean intercept) {
            shouldForwardToApplication = forward;
            shouldIntercept = intercept;
        }

        public boolean shouldForwardToApplication;
        public boolean shouldIntercept;
    }

    public InterceptionMiddleware(Logger withLogger) {
        logger = withLogger;
    }

    @Override
    public CompletableFuture<Void> onTurnAsync(TurnContext turnContext, NextDelegate next) {
        return invokeInboundAsync(turnContext, InspectionActivityExtensions
            .traceActivity(turnContext.getActivity(),"ReceivedActivity","Received Activity"))

            .thenCompose(intercept -> {
                if (intercept.shouldIntercept) {
                    turnContext.onSendActivities((sendContext, sendActivities, sendNext) -> {
                        List<Activity> traceActivities = sendActivities.stream()
                            .map(a ->
                                InspectionActivityExtensions.traceActivity(a, "SentActivity", "Sent Activity"))
                            .collect(Collectors.toList());
                        return invokeOutboundAsync(sendContext, traceActivities)
                            .thenCompose(response -> {
                                return sendNext.get();
                            });
                    });
                }

                if (intercept.shouldForwardToApplication) {
                    next.next()
                        .exceptionally(exception -> {
                            Activity traceActivity = InspectionActivityExtensions.traceActivity(exception);
                            invokeTraceExceptionAsync(turnContext, traceActivity).join();
                            throw new CompletionException(exception);
                        }).join();
                }

                if (intercept.shouldIntercept) {
                    return invokeTraceStateAsync(turnContext);
                }

                return null;
            });
    }

    protected abstract CompletableFuture<Intercept> inboundAsync(TurnContext turnContext, Activity activity);

    protected abstract CompletableFuture<Void> outboundAsync(TurnContext turnContext, List<Activity> clonedActivities);

    protected abstract CompletableFuture<Void> traceStateAsync(TurnContext turnContext);

    private CompletableFuture<Intercept> invokeInboundAsync(TurnContext turnContext, Activity traceActivity) {
        return inboundAsync(turnContext, traceActivity)
            .exceptionally(exception -> {
                logger.warn("Exception in inbound interception {}", exception.getMessage());
                return new Intercept(true, false);
            });
    }

    private CompletableFuture<Void> invokeOutboundAsync(TurnContext turnContext, List<Activity> traceActivities) {
        return outboundAsync(turnContext, traceActivities)
            .exceptionally(exception -> {
            logger.warn("Exception in outbound interception {}", exception.getMessage());
            return null;
        });
    }

    private CompletableFuture<Void> invokeOutboundAsync(TurnContext turnContext, Activity activity) {
        return invokeOutboundAsync(turnContext, Collections.singletonList(activity));
    }

    private CompletableFuture<Void> invokeTraceStateAsync(TurnContext turnContext) {
        return traceStateAsync(turnContext)
            .exceptionally(exception -> {
                logger.warn("Exception in state interception {}", exception.getMessage());
                return null;
            });
    }

    private CompletableFuture<Void> invokeTraceExceptionAsync(TurnContext turnContext, Activity traceActivity) {
        return outboundAsync(turnContext, Collections.singletonList(Activity.createContactRelationUpdateActivity()))
            .exceptionally(exception -> {
            logger.warn("Exception in exception interception {}", exception.getMessage());
            return null;
        });
    }
}
