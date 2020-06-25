// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the individual message within a chat or channel where a message
 * actions is taken.
 */
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

    @JsonProperty(value = "linkToMessage")
    private String linkToMessage;

    @JsonProperty(value = "locale")
    private String locale;

    @JsonProperty(value = "from")
    private MessageActionsPayloadFrom from;

    @JsonProperty(value = "body")
    private MessageActionsPayloadBody body;

    @JsonProperty(value = "attachmentLayout")
    private String attachmentLayout;

    @JsonProperty(value = "attachments")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MessageActionsPayloadAttachment> attachments;

    @JsonProperty(value = "mentions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MessageActionsPayloadMention> mentions;

    @JsonProperty(value = "reactions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MessageActionsPayloadReaction> reactions;

    /**
     * Gets unique id of the message.
     *
     * @return The unique id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id of the message.
     *
     * @param withId The new id of the message.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets id of the parent/root message of the thread.
     *
     * @return The id of the parent/root message.
     */
    public String getReplyToId() {
        return replyToId;
    }

    /**
     * Sets id of the parent/root message of the thread.
     *
     * @param withReplyToId The id of the parent/root message.
     */
    public void setReplyToId(String withReplyToId) {
        replyToId = withReplyToId;
    }

    /**
     * Gets type of message - automatically set to message.
     *
     * @return Possible values include: 'message'
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Sets type of message.
     *
     * @param withMessageType Possible values include: 'message'
     */
    public void setMessageType(String withMessageType) {
        messageType = withMessageType;
    }

    /**
     * Gets timestamp of when the message was created.
     *
     * @return The timestamp of the message.
     */
    public String getCreatedDateTime() {
        return createdDateTime;
    }

    /**
     * Sets timestamp of when the message was created.
     *
     * @param withCreatedDateTime The message timestamp.
     */
    public void setCreatedDateTime(String withCreatedDateTime) {
        createdDateTime = withCreatedDateTime;
    }

    /**
     * Gets timestamp of when the message was edited or updated.
     *
     * @return The timestamp of the message.
     */
    public String getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    /**
     * Sets timestamp of when the message was edited or updated.
     *
     * @param withLastModifiedDateTime The message timestamp.
     */
    public void setLastModifiedDateTime(String withLastModifiedDateTime) {
        lastModifiedDateTime = withLastModifiedDateTime;
    }

    /**
     * Indicates whether a message has been soft deleted.
     *
     * @return True if deleted.
     */
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * Indicates whether a message has been soft deleted.
     *
     * @param withDeleted True if deleted.
     */
    public void setDeleted(Boolean withDeleted) {
        deleted = withDeleted;
    }

    /**
     * Gets subject line of the message.
     *
     * @return The message subject line.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets subject line of the message.
     *
     * @param withSubject The message subject line.
     */
    public void setSubject(String withSubject) {
        subject = withSubject;
    }

    /**
     * Gets summary text of the message that could be used for notifications.
     *
     * @return The summary text.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets summary text of the message that could be used for notifications.
     *
     * @param withSummary The summary text.
     */
    public void setSummary(String withSummary) {
        summary = withSummary;
    }

    /**
     * Gets the importance of the message.
     *
     * @return Possible values include: 'normal', 'high', 'urgent'
     */
    public String getImportance() {
        return importance;
    }

    /**
     * Sets the importance of the message.
     *
     * @param withImportance Possible values include: 'normal', 'high', 'urgent'
     */
    public void setImportance(String withImportance) {
        importance = withImportance;
    }

    /**
     * Gets the link back to the message.
     *
     * @return The link back to the message.
     */
    public String getLinkToMessage() {
        return linkToMessage;
    }

    /**
     * Sets link back to the message.
     *
     * @param withLinkToMessage The link back to the message.
     */
    public void setLinkToMessage(String withLinkToMessage) {
        linkToMessage = withLinkToMessage;
    }

    /**
     * Gets locale of the message set by the client.
     *
     * @return The message locale.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets locale of the message set by the client.
     *
     * @param withLocale The message locale.
     */
    public void setLocale(String withLocale) {
        locale = withLocale;
    }

    /**
     * Gets sender of the message.
     *
     * @return The message sender.
     */
    public MessageActionsPayloadFrom getFrom() {
        return from;
    }

    /**
     * Sets sender of the message.
     *
     * @param withFrom The message sender.
     */
    public void setFrom(MessageActionsPayloadFrom withFrom) {
        from = withFrom;
    }

    /**
     * Gets plaintext/HTML representation of the content of the message.
     *
     * @return The message body.
     */
    public MessageActionsPayloadBody getBody() {
        return body;
    }

    /**
     * Sets plaintext/HTML representation of the content of the message.
     *
     * @param withBody The message body.
     */
    public void setBody(MessageActionsPayloadBody withBody) {
        body = withBody;
    }

    /**
     * Gets how the attachment(s) are displayed in the message.
     *
     * @return The attachment layout.
     */
    public String getAttachmentLayout() {
        return attachmentLayout;
    }

    /**
     * Sets how the attachment(s) are displayed in the message.
     *
     * @param withAttachmentLayout The attachment layout.
     */
    public void setAttachmentLayout(String withAttachmentLayout) {
        attachmentLayout = withAttachmentLayout;
    }

    /**
     * Gets attachments in the message - card, image, file, etc.
     *
     * @return The message attachments.
     */
    public List<MessageActionsPayloadAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets attachments in the message - card, image, file, etc.
     *
     * @param withAttachments The message attachments.
     */
    public void setAttachments(List<MessageActionsPayloadAttachment> withAttachments) {
        attachments = withAttachments;
    }

    /**
     * Gets list of entities mentioned in the message.
     *
     * @return The list of mentions.
     */
    public List<MessageActionsPayloadMention> getMentions() {
        return mentions;
    }

    /**
     * Sets list of entities mentioned in the message.
     *
     * @param withMentions The list of mentions.
     */
    public void setMentions(List<MessageActionsPayloadMention> withMentions) {
        mentions = withMentions;
    }

    /**
     * Gets reactions for the message.
     *
     * @return Message reactions.
     */
    public List<MessageActionsPayloadReaction> getReactions() {
        return reactions;
    }

    /**
     * Sets reactions for the message.
     *
     * @param withReactions Message reactions.
     */
    public void setReactions(List<MessageActionsPayloadReaction> withReactions) {
        reactions = withReactions;
    }
}
