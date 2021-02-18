// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O365 connector card HttpPOST action.
 */
public class O365ConnectorCardHttpPOST extends O365ConnectorCardActionBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "HttpPOST";

    @JsonProperty(value = "body")
    private String body;

    /**
     * Gets the content to be posted back to bots via invoke.
     * 
     * @return The post content.
     */
    public String getBody() {
        return body;
    }

    /**
     * Set the content to be posted back to bots via invoke.
     * 
     * @param withBody The post content.
     */
    public void setBody(String withBody) {
        body = withBody;
    }
}
