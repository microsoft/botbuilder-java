// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * O365 connector card OpenUri action.
 */
public class O365ConnectorCardOpenUri extends O365ConnectorCardActionBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "OpenUri";

    @JsonProperty(value = "targets")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<O365ConnectorCardOpenUriTarget> targets;

    /**
     * Gets target os / urls.
     * 
     * @return List of target urls.
     */
    public List<O365ConnectorCardOpenUriTarget> getTargets() {
        return targets;
    }

    /**
     * Sets target os / urls.
     * 
     * @param withTargets List of target urls.
     */
    public void setTargets(List<O365ConnectorCardOpenUriTarget> withTargets) {
        targets = withTargets;
    }
}
