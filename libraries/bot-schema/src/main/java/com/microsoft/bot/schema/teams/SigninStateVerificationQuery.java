// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Signin state (part of signin action auth flow) verification invoke query.
 */
public class SigninStateVerificationQuery {
    @JsonProperty(value = "state")
    private String state;

    /**
     * The state string originally received when the signin web flow is finished
     * with a state posted back to client via tab SDK
     * microsoftTeams.authentication.notifySuccess(state).
     * 
     * @return The sign-in state.
     */
    public String getState() {
        return state;
    }

    /**
     * The state string originally received when the signin web flow is finished
     * with a state posted back to client via tab SDK
     * microsoftTeams.authentication.notifySuccess(state).
     * 
     * @param withState The sign-in state.
     */
    public void setState(String withState) {
        state = withState;
    }
}
