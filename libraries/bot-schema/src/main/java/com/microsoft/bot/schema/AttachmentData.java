// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Attachment data.
 */
public class AttachmentData {
    /**
     * Content-Type of the attachment.
     */
    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    /**
     * Name of the attachment.
     */
    @JsonProperty(value = "name")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    /**
     * Attachment content.
     */
    @JsonProperty(value = "originalBase64")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private byte[] originalBase64;

    /**
     * Attachment thumbnail.
     */
    @JsonProperty(value = "thumbnailBase64")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private byte[] thumbnailBase64;

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param withType the type value to set
     */
    public void setType(String withType) {
        this.type = withType;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param withName the name value to set
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * Get the originalBase64 value.
     *
     * @return the originalBase64 value
     */
    public byte[] getOriginalBase64() {
        return this.originalBase64;
    }

    /**
     * Set the originalBase64 value.
     *
     * @param withOriginalBase64 the originalBase64 value to set
     */
    public void setOriginalBase64(byte[] withOriginalBase64) {
        this.originalBase64 = withOriginalBase64;
    }

    /**
     * Get the thumbnailBase64 value.
     *
     * @return the thumbnailBase64 value
     */
    public byte[] getThumbnailBase64() {
        return this.thumbnailBase64;
    }

    /**
     * Set the thumbnailBase64 value.
     *
     * @param withThumbnailBase64 the thumbnailBase64 value to set
     */
    public void setThumbnailBase64(byte[] withThumbnailBase64) {
        this.thumbnailBase64 = withThumbnailBase64;
    }

}
