// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Messaging extension response.
 */
public class MessagingExtensionResponse {
    @JsonProperty(value = "composeExtension")
    private MessagingExtensionResult composeExtension;

    /**
     * Gets the response result.
     * @return The result.
     */
    public MessagingExtensionResult getComposeExtension() {
        return composeExtension;
    }

    /**
     * Sets the response result.
     * @param withComposeExtension The result.
     */
    public void setComposeExtension(MessagingExtensionResult withComposeExtension) {
        composeExtension = withComposeExtension;
    }
}
