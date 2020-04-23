// CHECKSTYLE:OFF
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.inspection;

import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class InspectionSession {
    private ConversationReference conversationReference;
    private Logger logger;
    private ConnectorClient connectorClient;

    public InspectionSession(
        ConversationReference withConversationReference,
        MicrosoftAppCredentials withCredentials
    ) {
        this(withConversationReference, withCredentials, null);
    }

    public InspectionSession(
        ConversationReference withConversationReference,
        MicrosoftAppCredentials withCredentials,
        Logger withLogger
    ) {
        conversationReference = withConversationReference;
        logger = withLogger != null ? withLogger : LoggerFactory.getLogger(InspectionSession.class);
        connectorClient = new RestConnectorClient(
            conversationReference.getServiceUrl(),
            withCredentials
        );
    }

    public CompletableFuture<Boolean> send(Activity activity) {
        return connectorClient.getConversations().sendToConversation(
            activity.applyConversationReference(conversationReference)
        )

            .handle((result, exception) -> {
                if (exception == null) {
                    return true;
                }

                logger.warn(
                    "Exception '{}' while attempting to call Emulator for inspection, check it is running, "
                        + "and you have correct credentials in the Emulator and the InspectionMiddleware.",
                    exception.getMessage()
                );

                return false;
            });
    }
}
