// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.inspection;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.inspection.InspectionMiddleware;
import com.microsoft.bot.builder.inspection.InspectionState;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;

/**
 * A BotFrameworkHttpAdapter that use InspectionMiddleware to forward message and state information.
 */
public class AdapterWithInspection extends BotFrameworkHttpAdapter {
    /**
     * Uses InspectionMiddleware to track ConversationState and UserState.
     *
     * @param configuration The Configuration {@link BotDependencyConfiguration#getConfiguration()}
     * @param inspectionState The InspectionState {@link Application#getInspectionState(Storage)}
     * @param userState The UserState {@link BotDependencyConfiguration#getUserState(Storage)}
     * @param conversationState The ConversationState {@link BotDependencyConfiguration#getConversationState(Storage)}
     */
    public AdapterWithInspection(Configuration configuration,
                                 InspectionState inspectionState,
                                 UserState userState,
                                 ConversationState conversationState) {
        super(configuration);

        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(
            configuration.getProperty("MicrosoftAppId"), configuration.getProperty("MicrosoftAppPassword"));

        use(new InspectionMiddleware(inspectionState, userState, conversationState, credentials));
    }
}
