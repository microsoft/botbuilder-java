// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * File info card.
 */
public class FileInfoCard {
    /**
     * Content type to be used in the type property.
     */
    public static final String CONTENT_TYPE = "application/vnd.microsoft.teams.card.file.info";

    @JsonProperty(value = "uniqueId")
    private String uniqueId;

    @JsonProperty(value = "fileType")
    private String fileType;

    @JsonProperty(value = "etag")
    private Object etag;

    /**
     * Gets unique Id for the file.
     * 
     * @return The unique id of the download.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Sets unique Id for the file.
     * 
     * @param withUniqueId The unique id of the download.
     */
    public void setUniqueId(String withUniqueId) {
        uniqueId = withUniqueId;
    }

    /**
     * Gets type of file.
     * 
     * @return The type of the file.
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Sets type of file.
     * 
     * @param withFileType The type of the file.
     */
    public void setFileType(String withFileType) {
        fileType = withFileType;
    }

    /**
     * Gets eTag for the file.
     * 
     * @return The eTag.
     */
    public Object getEtag() {
        return etag;
    }

    /**
     * Sets eTag for the file.
     * 
     * @param withEtag The eTag value.
     */
    public void setEtag(Object withEtag) {
        etag = withEtag;
    }
}
