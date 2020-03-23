// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SigninStateVerificationQuery {
    @JsonProperty(value = "state")
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String withState) {
        state = withState;
    }
}
