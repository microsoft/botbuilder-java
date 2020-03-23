// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardOpenUriTarget {
    @JsonProperty(value = "os")
    private String os;

    @JsonProperty(value = "uri")
    private String uri;

    public String getOs() {
        return os;
    }

    public void setOs(String withOs) {
        os = withOs;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String withUri) {
        uri = withUri;
    }
}
