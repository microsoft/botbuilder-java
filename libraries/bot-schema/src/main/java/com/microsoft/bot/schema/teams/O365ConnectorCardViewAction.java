// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * O365 connector card ViewAction action.
 */
public class O365ConnectorCardViewAction extends O365ConnectorCardActionBase {
    /**
     * Content type to be used in the type property.
     */
    public static final String TYPE = "ViewAction";

    @JsonProperty(value = "target")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> target;

    /**
     * Gets target urls, only the first url effective for card button.
     * 
     * @return List of button targets.
     */
    public List<String> getTarget() {
        return target;
    }

    /**
     * Sets target urls, only the first url effective for card button.
     * 
     * @param withTarget List of button targets.
     */
    public void setTarget(List<String> withTarget) {
        target = withTarget;
    }
}
