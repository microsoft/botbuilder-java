// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileConsentCard {
    /**
     * Content type to be used in the type property.
     */
    public static final String CONTENT_TYPE = "application/vnd.microsoft.teams.card.file.consent";

    @JsonProperty(value = "description")
    private String description;

    @JsonProperty(value = "sizeInBytes")
    private long sizeInBytes;

    @JsonProperty(value = "acceptContext")
    private Object acceptContext;

    @JsonProperty(value = "declineContext")
    private Object declineContext;

    public String getDescription() {
        return description;
    }

    public void setDescription(String withDescription) {
        description = withDescription;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long withSizeInBytes) {
        sizeInBytes = withSizeInBytes;
    }

    public Object getAcceptContext() {
        return acceptContext;
    }

    public void setAcceptContext(Object acceptContext) {
        acceptContext = acceptContext;
    }

    public Object getDeclineContext() {
        return declineContext;
    }

    public void setDeclineContext(Object withDeclineContext) {
        declineContext = withDeclineContext;
    }
}
