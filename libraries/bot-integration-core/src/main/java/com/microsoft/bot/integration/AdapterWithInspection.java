// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.inspection.InspectionMiddleware;
import com.microsoft.bot.builder.inspection.InspectionState;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;

/**
 * A BotFrameworkHttpAdapter that use InspectionMiddleware to forward message
 * and state information.
 *
 * <p>
 * See the Inspection sample for details on how this is used.
 * </p>
 */
public class AdapterWithInspection extends BotFrameworkHttpAdapter {
    /**
     * Uses InspectionMiddleware to track ConversationState and UserState.
     *
     * @param configuration     The Configuration
     * @param inspectionState   The InspectionState
     * @param userState         The UserState
     * @param conversationState The ConversationState
     */
    public AdapterWithInspection(
        Configuration configuration,
        InspectionState inspectionState,
        UserState userState,
        ConversationState conversationState
    ) {
        super(configuration);

        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(
            configuration.getProperty("MicrosoftAppId"),
            configuration.getProperty("MicrosoftAppPassword")
        );

        use(new InspectionMiddleware(inspectionState, userState, conversationState, credentials));
    }
}
