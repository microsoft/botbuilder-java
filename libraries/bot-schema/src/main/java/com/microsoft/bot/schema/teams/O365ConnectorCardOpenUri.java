// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class O365ConnectorCardOpenUri extends O365ConnectorCardActionBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "OpenUri";

    @JsonProperty(value = "targets")
    public List<O365ConnectorCardOpenUriTarget> targets;

    public List<O365ConnectorCardOpenUriTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<O365ConnectorCardOpenUriTarget> withTargets) {
        targets = withTargets;
    }
}
