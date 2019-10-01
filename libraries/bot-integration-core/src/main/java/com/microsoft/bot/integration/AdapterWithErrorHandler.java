// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

/**
 * An Adapter that provides exception handling.
 */
public class AdapterWithErrorHandler extends BotFrameworkHttpAdapter {
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

        setOnTurnError((turnContext, exception) ->
            turnContext.sendActivity(
                "Sorry, it looks like something went wrong.")
            .thenApply(resourceResponse -> null));
    }
}
