// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.builder.ConversationState;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

/**
 * An Adapter that provides exception handling.
 */
public class AdapterWithErrorHandler extends BotFrameworkHttpAdapter {
    private static final String ERROR_MSG_ONE = "The bot encountered an error or bug.";
    private static final String ERROR_MSG_TWO =
        "To continue to run this bot, please fix the bot source code.";

    /**
     * Constructs an error handling BotFrameworkHttpAdapter by providing an
     * {@link com.microsoft.bot.builder.OnTurnErrorHandler}.
     *
     * <p>
     * For this sample, a simple message is displayed. For a production Bot, a more
     * informative message or action is likely preferred.
     * </p>
     *
     * @param withConfiguration The Configuration object to use.
     */
    public AdapterWithErrorHandler(Configuration withConfiguration) {
        super(withConfiguration);

        setOnTurnError((turnContext, exception) -> {
            LoggerFactory.getLogger(AdapterWithErrorHandler.class).error("onTurnError", exception);

            return turnContext.sendActivities(
                MessageFactory.text(ERROR_MSG_ONE), MessageFactory.text(ERROR_MSG_TWO)
            ).thenCompose(resourceResponse -> sendTraceActivity(turnContext, exception));
        });
    }

    /**
     * Constructs an error handling BotFrameworkHttpAdapter by providing an
     * {@link com.microsoft.bot.builder.OnTurnErrorHandler}.
     *
     * <p>
     * For this sample, a simple message is displayed. For a production Bot, a more
     * informative message or action is likely preferred.
     * </p>
     *
     * @param withConfiguration     The Configuration object to use.
     * @param withConversationState For ConversationState.
     */
    public AdapterWithErrorHandler(
        Configuration withConfiguration,
        ConversationState withConversationState
    ) {
        super(withConfiguration);

        setOnTurnError((turnContext, exception) -> {
            LoggerFactory.getLogger(AdapterWithErrorHandler.class).error("onTurnError", exception);

            return turnContext.sendActivities(
                MessageFactory.text(ERROR_MSG_ONE), MessageFactory.text(ERROR_MSG_TWO)
            ).thenCompose(resourceResponse -> sendTraceActivity(turnContext, exception))
                .thenCompose(stageResult -> {
                    if (withConversationState != null) {
                        // Delete the conversationState for the current conversation to prevent the
                        // bot from getting stuck in a error-loop caused by being in a bad state.
                        // ConversationState should be thought of as similar to "cookie-state" in a
                        // Web pages.
                        return withConversationState.delete(turnContext)
                            .exceptionally(deleteException -> {
                                LoggerFactory.getLogger(AdapterWithErrorHandler.class)
                                    .error("ConversationState.delete", deleteException);
                                return null;
                            });
                    }
                    return CompletableFuture.completedFuture(null);
                });
        });
    }

    private CompletableFuture<Void> sendTraceActivity(
        TurnContext turnContext,
        Throwable exception
    ) {
        if (StringUtils.equals(turnContext.getActivity().getChannelId(), Channels.EMULATOR)) {
            Activity traceActivity = new Activity(ActivityTypes.TRACE);
            traceActivity.setLabel("TurnError");
            traceActivity.setName("OnTurnError Trace");
            traceActivity.setValue(ExceptionUtils.getStackTrace(exception));
            traceActivity.setValueType("https://www.botframework.com/schemas/error");

            return turnContext.sendActivity(traceActivity).thenApply(resourceResponse -> null);
        }

        return CompletableFuture.completedFuture(null);
    }
}
