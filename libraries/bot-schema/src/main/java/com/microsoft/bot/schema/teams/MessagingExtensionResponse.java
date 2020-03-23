// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessagingExtensionResponse {
    @JsonProperty(value = "composeExtension")
    public MessagingExtensionResult composeExtension;

    public MessagingExtensionResult getComposeExtension() {
        return composeExtension;
    }

    public void setComposeExtension(MessagingExtensionResult withComposeExtension) {
        composeExtension = withComposeExtension;
    }
}
