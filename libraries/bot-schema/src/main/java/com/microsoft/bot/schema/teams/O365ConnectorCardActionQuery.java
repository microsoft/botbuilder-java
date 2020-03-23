// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardActionQuery {
    @JsonProperty(value = "body")
    private String body;

    @JsonProperty(value = "actionId")
    private String actionId;

    public String getBody() {
        return body;
    }

    public void setBody(String withBody) {
        this.body = withBody;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String withActionId) {
        this.actionId = withActionId;
    }
}
