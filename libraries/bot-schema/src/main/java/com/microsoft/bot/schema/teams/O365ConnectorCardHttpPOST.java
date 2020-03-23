// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardHttpPOST extends O365ConnectorCardActionBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "HttpPOST";

    @JsonProperty(value = "body")
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String withBody) {
        body = body;
    }
}
