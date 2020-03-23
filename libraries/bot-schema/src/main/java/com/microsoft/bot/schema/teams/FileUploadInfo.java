// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public String getName() {
        return name;
    }

    public void setName(String withName) {
        name = withName;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String withUploadUrl) {
        uploadUrl = withUploadUrl;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String withContentUrl) {
        contentUrl = withContentUrl;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String withUniqueId) {
        uniqueId = withUniqueId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String withFileType) {
        fileType = withFileType;
    }
}
