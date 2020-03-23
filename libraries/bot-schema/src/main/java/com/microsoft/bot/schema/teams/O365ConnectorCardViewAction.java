// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class O365ConnectorCardViewAction extends O365ConnectorCardActionBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "ViewAction";

    @JsonProperty(value = "target")
    public List<String> target;

    public List<String> getTarget() {
        return target;
    }

    public void setTarget(List<String> withTarget) {
        target = withTarget;
    }
}
