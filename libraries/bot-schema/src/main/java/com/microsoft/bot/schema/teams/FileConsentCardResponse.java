// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the value of the invoke activity sent when the user acts on a file
 * consent card.
 */
public class FileConsentCardResponse {
    @JsonProperty(value = "action")
    private String action;

    @JsonProperty(value = "context")
    private Object context;

    @JsonProperty(value = "uploadInfo")
    private FileUploadInfo uploadInfo;

    /**
     * Gets the action the user took.
     * 
     * @return Possible values include 'accept', 'decline'
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action the user took.
     * 
     * @param withAction Possible values include 'accept', 'decline'
     */
    public void setAction(String withAction) {
        action = withAction;
    }

    /**
     * Gets the context associated with the action.
     * 
     * @return The context value.
     */
    public Object getContext() {
        return context;
    }

    /**
     * Sets the context associated with the action.
     * 
     * @param withContext The new context.
     */
    public void setContext(Object withContext) {
        context = withContext;
    }

    /**
     * Gets if the user accepted the file, contains information about the file to be
     * uploaded.
     * 
     * @return The file upload info.
     */
    public FileUploadInfo getUploadInfo() {
        return uploadInfo;
    }

    /**
     * Sets if the user accepted the file, contains information about the file to be
     * uploaded.
     * 
     * @param withUploadInfo The file upload info.
     */
    public void setUploadInfo(FileUploadInfo withUploadInfo) {
        uploadInfo = withUploadInfo;
    }
}
