// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 365 connector card HttpPOST invoke query.
 */
public class O365ConnectorCardActionQuery {
    @JsonProperty(value = "body")
    private String body;

    @JsonProperty(value = "actionId")
    private String actionId;

    /**
     * Gets the results of body string defined in O365ConnectorCardHttpPOST with
     * substituted input values.
     * 
     * @return The query body.
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the results of body string defined in O365ConnectorCardHttpPOST with
     * substituted input values.
     * 
     * @param withBody The query body.
     */
    public void setBody(String withBody) {
        this.body = withBody;
    }

    /**
     * Gets the action Id associated with the HttpPOST action button triggered,
     * defined in O365ConnectorCardActionBase.
     * 
     * @return The action id.
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Sets the action Id associated with the HttpPOST action button triggered,
     * defined in O365ConnectorCardActionBase.
     * 
     * @param withActionId The action id.
     */
    public void setActionId(String withActionId) {
        this.actionId = withActionId;
    }
}
