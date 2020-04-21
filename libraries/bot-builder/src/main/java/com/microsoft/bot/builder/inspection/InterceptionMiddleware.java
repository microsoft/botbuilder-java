// CHECKSTYLE:OFF
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

    static class Intercept {
        Intercept(
            boolean forward,
            boolean intercept
        ) {
            shouldForwardToApplication = forward;
            shouldIntercept = intercept;
        }

        @SuppressWarnings({ "checkstyle:JavadocVariable", "checkstyle:VisibilityModifier" })
        boolean shouldForwardToApplication;
        @SuppressWarnings({ "checkstyle:JavadocVariable", "checkstyle:VisibilityModifier" })
        boolean shouldIntercept;
    }

    public InterceptionMiddleware(Logger withLogger) {
        logger = withLogger;
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
        return invokeInbound(
            turnContext,
            InspectionActivityExtensions.traceActivity(
                turnContext.getActivity(),
                "ReceivedActivity",
                "Received Activity"
            )
        )

            .thenCompose(intercept -> {
                if (intercept.shouldIntercept) {
                    turnContext.onSendActivities((sendContext, sendActivities, sendNext) -> {
                        List<Activity> traceActivities = sendActivities.stream().map(
                            a -> InspectionActivityExtensions.traceActivity(
                                a,
                                "SentActivity",
                                "Sent Activity"
                            )
                        ).collect(Collectors.toList());
                        return invokeOutbound(sendContext, traceActivities).thenCompose(
                            response -> {
                                return sendNext.get();
                            }
                        );
                    });

                    turnContext.onUpdateActivity((updateContext, updateActivity, updateNext) -> {
                        Activity traceActivity = InspectionActivityExtensions.traceActivity(
                            updateActivity,
                            "MessageUpdate",
                            "Message Update"
                        );
                        return invokeOutbound(turnContext, traceActivity).thenCompose(
                            response -> updateNext.get()
                        );
                    });

                    turnContext.onDeleteActivity((deleteContext, deleteReference, deleteNext) -> {
                        Activity traceActivity = InspectionActivityExtensions.traceActivity(
                            deleteReference
                        );
                        return invokeOutbound(turnContext, traceActivity).thenCompose(
                            response -> deleteNext.get()
                        );
                    });
                }

                if (intercept.shouldForwardToApplication) {
                    next.next().exceptionally(exception -> {
                        Activity traceActivity = InspectionActivityExtensions.traceActivity(
                            exception
                        );
                        invokeTraceException(turnContext, traceActivity).join();
                        throw new CompletionException(exception);
                    }).join();
                }

                if (intercept.shouldIntercept) {
                    return invokeTraceState(turnContext);
                }

                return CompletableFuture.completedFuture(null);
            });
    }

    protected abstract CompletableFuture<Intercept> inbound(
        TurnContext turnContext,
        Activity activity
    );

    protected abstract CompletableFuture<Void> outbound(
        TurnContext turnContext,
        List<Activity> clonedActivities
    );

    protected abstract CompletableFuture<Void> traceState(TurnContext turnContext);

    private CompletableFuture<Intercept> invokeInbound(
        TurnContext turnContext,
        Activity traceActivity
    ) {
        return inbound(turnContext, traceActivity).exceptionally(exception -> {
            logger.warn("Exception in inbound interception {}", exception.getMessage());
            return new Intercept(true, false);
        });
    }

    private CompletableFuture<Void> invokeOutbound(
        TurnContext turnContext,
        List<Activity> traceActivities
    ) {
        return outbound(turnContext, traceActivities).exceptionally(exception -> {
            logger.warn("Exception in outbound interception {}", exception.getMessage());
            return null;
        });
    }

    private CompletableFuture<Void> invokeOutbound(TurnContext turnContext, Activity activity) {
        return invokeOutbound(turnContext, Collections.singletonList(activity));
    }

    private CompletableFuture<Void> invokeTraceState(TurnContext turnContext) {
        return traceState(turnContext).exceptionally(exception -> {
            logger.warn("Exception in state interception {}", exception.getMessage());
            return null;
        });
    }

    private CompletableFuture<Void> invokeTraceException(
        TurnContext turnContext,
        Activity traceActivity
    ) {
        return outbound(turnContext, Collections.singletonList(traceActivity)).exceptionally(
            exception -> {
                logger.warn("Exception in exception interception {}", exception.getMessage());
                return null;
            }
        );
    }
}
