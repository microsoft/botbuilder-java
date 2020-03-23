// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class O365ConnectorCardImage {
    @JsonProperty(value = "image")
    private String image;

    @JsonProperty(value = "title")
    private String title;

    public String getImage() {
        return image;
    }

    public void setImage(String withImage) {
        image = withImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String withTitle) {
        title = withTitle;
    }
}
