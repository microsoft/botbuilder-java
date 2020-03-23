// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileConsentCardResponse {
    @JsonProperty(value = "action")
    private String action;

    @JsonProperty(value = "context")
    private Object context;

    @JsonProperty(value = "uploadInfo")
    private FileUploadInfo uploadInfo;

    public String getAction() {
        return action;
    }

    public void setAction(String withAction) {
        action = withAction;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object withContext) {
        context = withContext;
    }

    public FileUploadInfo getUploadInfo() {
        return uploadInfo;
    }

    public void setUploadInfo(FileUploadInfo withUploadInfo) {
        uploadInfo = withUploadInfo;
    }
}
