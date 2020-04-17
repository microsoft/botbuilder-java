// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * File consent card attachment.
 */
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

    /**
     * Gets file description.
     * 
     * @return The file description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets file description.
     * 
     * @param withDescription The new file description.
     */
    public void setDescription(String withDescription) {
        description = withDescription;
    }

    /**
     * Gets size of the file to be uploaded in Bytes.
     * 
     * @return The size in bytes.
     */
    public long getSizeInBytes() {
        return sizeInBytes;
    }

    /**
     * Sets size of the file to be uploaded in Bytes.
     * 
     * @param withSizeInBytes The new size in bytes.
     */
    public void setSizeInBytes(long withSizeInBytes) {
        sizeInBytes = withSizeInBytes;
    }

    /**
     * Gets context sent back to the Bot if user consented to upload. This is free
     * flow schema and is sent back in Value field of Activity.
     * 
     * @return The accept context.
     */
    public Object getAcceptContext() {
        return acceptContext;
    }

    /**
     * Sets context sent back to the Bot if user consented to upload. This is free
     * flow schema and is sent back in Value field of Activity.
     * 
     * @param withAcceptContext The new context.
     */
    public void setAcceptContext(Object withAcceptContext) {
        acceptContext = withAcceptContext;
    }

    /**
     * Gets context sent back to the Bot if user declined. This is free flow schema
     * and is sent back in Value field of Activity.
     * 
     * @return The decline context.
     */
    public Object getDeclineContext() {
        return declineContext;
    }

    /**
     * Sets context sent back to the Bot if user declined. This is free flow schema
     * and is sent back in Value field of Activity.
     * 
     * @param withDeclineContext The decline context.
     */
    public void setDeclineContext(Object withDeclineContext) {
        declineContext = withDeclineContext;
    }
}
