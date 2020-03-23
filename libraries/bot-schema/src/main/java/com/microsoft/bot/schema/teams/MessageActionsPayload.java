// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MessageActionsPayload {
    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "replyToId")
    private String replyToId;

    @JsonProperty(value = "messageType")
    private String messageType;

    @JsonProperty(value = "createdDateTime")
    private String createdDateTime;

    @JsonProperty(value = "lastModifiedDateTime")
    private String lastModifiedDateTime;

    @JsonProperty(value = "deleted")
    private Boolean deleted;

    @JsonProperty(value = "subject")
    private String subject;

    @JsonProperty(value = "summary")
    private String summary;

    @JsonProperty(value = "importance")
    private String importance;

    @JsonProperty(value = "locale")
    private String locale;

    @JsonProperty(value = "from")
    private MessageActionsPayloadFrom from;

    @JsonProperty(value = "body")
    private MessageActionsPayloadBody body;

    @JsonProperty(value = "attachmentLayout")
    private String attachmentLayout;

    @JsonProperty(value = "attachments")
    private List<MessageActionsPayloadAttachment> attachments;

    @JsonProperty(value = "mentions")
    private List<MessageActionsPayloadMention> mentions;

    @JsonProperty(value = "reactions")
    private List<MessageActionsPayloadReaction> reactions;

    public String getId() {
        return id;
    }

    public void setId(String withId) {
        id = withId;
    }

    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String withReplyToId) {
        replyToId = withReplyToId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String withMessageType) {
        messageType = withMessageType;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String withCreatedDateTime) {
        createdDateTime = withCreatedDateTime;
    }

    public String getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(String withLastModifiedDateTime) {
        lastModifiedDateTime = withLastModifiedDateTime;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean withDeleted) {
        deleted = withDeleted;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String withSubject) {
        subject = withSubject;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String withSummary) {
        summary = withSummary;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String withImportance) {
        importance = withImportance;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String withLocale) {
        locale = withLocale;
    }

    public MessageActionsPayloadFrom getFrom() {
        return from;
    }

    public void setFrom(MessageActionsPayloadFrom withFrom) {
        from = withFrom;
    }

    public MessageActionsPayloadBody getBody() {
        return body;
    }

    public void setBody(MessageActionsPayloadBody withBody) {
        body = withBody;
    }

    public String getAttachmentLayout() {
        return attachmentLayout;
    }

    public void setAttachmentLayout(String withAttachmentLayout) {
        attachmentLayout = withAttachmentLayout;
    }

    public List<MessageActionsPayloadAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MessageActionsPayloadAttachment> withAttachments) {
        attachments = withAttachments;
    }

    public List<MessageActionsPayloadMention> getMentions() {
        return mentions;
    }

    public void setMentions(List<MessageActionsPayloadMention> withMentions) {
        mentions = withMentions;
    }

    public List<MessageActionsPayloadReaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<MessageActionsPayloadReaction> withReactions) {
        reactions = withReactions;
    }
}
