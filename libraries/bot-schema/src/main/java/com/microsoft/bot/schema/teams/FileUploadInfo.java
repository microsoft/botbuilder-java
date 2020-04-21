// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about the file to be uploaded.
 */
public class FileUploadInfo {
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "uploadUrl")
    private String uploadUrl;

    @JsonProperty(value = "contentUrl")
    private String contentUrl;

    @JsonProperty(value = "uniqueId")
    private String uniqueId;

    @JsonProperty(value = "fileType")
    private String fileType;

    /**
     * Gets name of the file.
     * 
     * @return The file name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of the file.
     * 
     * @param withName The file name.
     */
    public void setName(String withName) {
        name = withName;
    }

    /**
     * Gets URL to an upload session that the bot can use to set the file contents.
     * 
     * @return The url to the upload session.
     */
    public String getUploadUrl() {
        return uploadUrl;
    }

    /**
     * Sets URL to an upload session that the bot can use to set the file contents.
     * 
     * @param withUploadUrl The url to the upload session.
     */
    public void setUploadUrl(String withUploadUrl) {
        uploadUrl = withUploadUrl;
    }

    /**
     * Gets URL to file.
     * 
     * @return The url to the file content.
     */
    public String getContentUrl() {
        return contentUrl;
    }

    /**
     * Sets URL to file.
     * 
     * @param withContentUrl The url to the file content.
     */
    public void setContentUrl(String withContentUrl) {
        contentUrl = withContentUrl;
    }

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
}
