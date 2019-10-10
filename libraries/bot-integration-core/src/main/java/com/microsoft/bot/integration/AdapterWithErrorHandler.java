// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.builder.ConversationState;

import java.util.concurrent.CompletableFuture;
import org.slf4j.LoggerFactory;

/**
 * An Adapter that provides exception handling.
 */
public class AdapterWithErrorHandler extends BotFrameworkHttpAdapter {
    private static final String ERROR_MSG = "Bot Framework encountered an error";

    /**
     * Constructs an error handling BotFrameworkHttpAdapter by providing
     * an {@link com.microsoft.bot.builder.OnTurnErrorHandler}.
     *
     * <p>For this sample, a simple message is displayed.  For a production
     * Bot, a more informative message or action is likely preferred.</p>
     *
     * @param withConfiguration The Configuration object to use.
     */
    public AdapterWithErrorHandler(Configuration withConfiguration) {
        super(withConfiguration);

        setOnTurnError((turnContext, exception) -> {
                LoggerFactory.getLogger(AdapterWithErrorHandler.class).error("onTurnError", exception);
                return turnContext.sendActivity(ERROR_MSG + ": " + exception.getLocalizedMessage())
                    .thenApply(resourceResponse -> null);
            });
    }

    /**
     * Constructs an error handling BotFrameworkHttpAdapter by providing
     * an {@link com.microsoft.bot.builder.OnTurnErrorHandler}.
     *
     * <p>For this sample, a simple message is displayed.  For a production
     * Bot, a more informative message or action is likely preferred.</p>
     *
     * @param withConfiguration The Configuration object to use.
     * @param withConversationState For ConversationState.
     */
    public AdapterWithErrorHandler(Configuration withConfiguration, ConversationState withConversationState) {
        super(withConfiguration);

        setOnTurnError((turnContext, exception) -> {
            LoggerFactory.getLogger(AdapterWithErrorHandler.class).error("onTurnError", exception);
            return turnContext.sendActivity(ERROR_MSG + ": " + exception.getLocalizedMessage())
                .thenCompose(resourceResponse -> {
                    if (withConversationState != null) {
                        // Delete the conversationState for the current conversation to prevent the
                        // bot from getting stuck in a error-loop caused by being in a bad state.
                        // ConversationState should be thought of as similar to "cookie-state" in a Web pages.
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
}
