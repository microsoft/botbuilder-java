// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppBasedLinkQuery {
    @JsonProperty(value = "url")
    private String url;

    public AppBasedLinkQuery(String withUrl) {
        url = withUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String withUrl) {
        url = withUrl;
    }
}
