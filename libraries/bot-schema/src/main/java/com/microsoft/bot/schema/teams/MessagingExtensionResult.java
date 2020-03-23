// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.schema.Activity;

import java.util.List;

public class MessagingExtensionResult {
    @JsonProperty(value = "attachmentLayout")
    private String attachmentLayout;

    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "attachments")
    public List<MessagingExtensionAttachment> attachments;

    @JsonProperty(value = "suggestedActions")
    public MessagingExtensionSuggestedAction suggestedActions;

    @JsonProperty(value = "text")
    private String text;

    @JsonProperty(value = "activityPreview")
    public Activity activityPreview;

    public String getAttachmentLayout() {
        return attachmentLayout;
    }

    public void setAttachmentLayout(String withAttachmentLayout) {
        attachmentLayout = withAttachmentLayout;
    }

    public String getType() {
        return type;
    }

    public void setType(String withType) {
        type = withType;
    }

    public List<MessagingExtensionAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MessagingExtensionAttachment> withAttachments) {
        attachments = withAttachments;
    }

    public MessagingExtensionSuggestedAction getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(MessagingExtensionSuggestedAction withSuggestedActions) {
        suggestedActions = withSuggestedActions;
    }

    public String getText() {
        return text;
    }

    public void setText(String withText) {
        text = withText;
    }

    public Activity getActivityPreview() {
        return activityPreview;
    }

    public void setActivityPreview(Activity withActivityPreview) {
        activityPreview = withActivityPreview;
    }
}
