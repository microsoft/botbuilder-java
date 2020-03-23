// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileDownloadInfo {
    /**
     * Content type to be used in the type property.
     */
    public static final String CONTENT_TYPE = "application/vnd.microsoft.teams.file.download.info";

    @JsonProperty(value = "downloadUrl")
    private String downloadUrl;

    @JsonProperty(value = "uniqueId")
    private String uniqueId;

    @JsonProperty(value = "fileType")
    private String fileType;

    @JsonProperty(value = "etag")
    private Object etag;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String withDownloadUrl) {
        downloadUrl = withDownloadUrl;
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

    public Object getEtag() {
        return etag;
    }

    public void setEtag(Object withEtag) {
        etag = withEtag;
    }
}
