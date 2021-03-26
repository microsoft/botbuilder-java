// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests to ensure that MessageActionsPayload works as expected.
 */
public class MessageActionsPayloadTest {
    /**
     * Ensures the constructor of the MessageActionsPayload class works as expected.
     */
    @Test
    public void TestMessageActionPayloadConstructor(){
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        Assert.assertNotNull(messageActionsPayload);
    }

    /**
     * Ensures that the Id property can be set and retrieved.
     */
    @Test
    public void TestGetId(){
        String id = "testId";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setId(id);
        String result = messageActionsPayload.getId();

        Assert.assertEquals(result, id);
    }

    /**
     * Ensures that the ReplyToId property can be set and retrieved.
     */
    @Test
    public void TestGetReplyToId(){
        String replyToId = "testReplyToId";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setReplyToId(replyToId);
        String result = messageActionsPayload.getReplyToId();

        Assert.assertEquals(result, replyToId);
    }

    /**
     * Ensures that the MessageType property can be set and retrieved.
     */
    @Test
    public void TestGetMessageType(){
        String messageType = "testMessageType";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setMessageType(messageType);
        String result = messageActionsPayload.getMessageType();

        Assert.assertEquals(result, messageType);
    }

    /**
     * Ensures that the CreatedDateTime property can be set and retrieved.
     */
    @Test
    public void TestGetCreatedDateTime(){
        String createdDateTime = "2000-01-01";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setCreatedDateTime(createdDateTime);
        String result = messageActionsPayload.getCreatedDateTime();

        Assert.assertEquals(result, createdDateTime);
    }

    /**
     * Ensures that the LastModifiedDateTime property can be set and retrieved.
     */
    @Test
    public void TestGetLastModifiedDateTime(){
        String lastModifiedDateTime = "2000-01-01";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setLastModifiedDateTime(lastModifiedDateTime);
        String result = messageActionsPayload.getLastModifiedDateTime();

        Assert.assertEquals(result, lastModifiedDateTime);
    }

    /**
     * Ensures that the Deleted property can be set and retrieved.
     */
    @Test
    public void TestGetDeleted(){
        Boolean deleted = false;
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setDeleted(deleted);
        Boolean result = messageActionsPayload.getDeleted();

        Assert.assertEquals(result, deleted);
    }

    /**
     * Ensures that the Subject property can be set and retrieved.
     */
    @Test
    public void TestGetSubject(){
        String subject = "testSubject";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setSubject(subject);
        String result = messageActionsPayload.getSubject();

        Assert.assertEquals(result, subject);
    }

    /**
     * Ensures that the Summary property can be set and retrieved.
     */
    @Test
    public void TestGetSummary(){
        String summary = "testSummary";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setSummary(summary);
        String result = messageActionsPayload.getSummary();

        Assert.assertEquals(result, summary);
    }

    /**
     * Ensures that the Importance property can be set and retrieved.
     */
    @Test
    public void TestGetImportance(){
        String importance = "normal";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setImportance(importance);
        String result = messageActionsPayload.getImportance();

        Assert.assertEquals(result, importance);
    }

    /**
     * Ensures that the LinkToMessage property can be set and retrieved.
     */
    @Test
    public void TestGetLinkToMessage(){
        String linkToMessage = "https://teams.microsoft.com/l/message/testing-id";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setLinkToMessage(linkToMessage);
        String result = messageActionsPayload.getLinkToMessage();

        Assert.assertEquals(result, linkToMessage);
    }

    /**
     * Ensures that the Locale property can be set and retrieved.
     */
    @Test
    public void TestGetLocale(){
        String locale = "US";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setLocale(locale);
        String result = messageActionsPayload.getLocale();

        Assert.assertEquals(result, locale);
    }

    /**
     * Ensures that the From property can be set and retrieved.
     */
    @Test
    public void TestGetFrom(){
        MessageActionsPayloadFrom from = new MessageActionsPayloadFrom();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setFrom(from);
        MessageActionsPayloadFrom result = messageActionsPayload.getFrom();

        Assert.assertEquals(result, from);
    }

    /**
     * Ensures that the Body property can be set and retrieved.
     */
    @Test
    public void TestGetBody(){
        MessageActionsPayloadBody body = new MessageActionsPayloadBody();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setBody(body);
        MessageActionsPayloadBody result = messageActionsPayload.getBody();

        Assert.assertEquals(result, body);
    }

    /**
     * Ensures that the AttachmentLayout property can be set and retrieved.
     */
    @Test
    public void TestGetAttachmentLayout(){
        String attachmentLayout = "testAttachmentLayout";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setAttachmentLayout(attachmentLayout);
        String result = messageActionsPayload.getAttachmentLayout();

        Assert.assertEquals(result, attachmentLayout);
    }

    /**
     * Ensures that the Attachments property can be set and retrieved.
     */
    @Test
    public void TestGetAttachments(){
        List<MessageActionsPayloadAttachment> attachments = new ArrayList<MessageActionsPayloadAttachment>();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setAttachments(attachments);
        List<MessageActionsPayloadAttachment> result = messageActionsPayload.getAttachments();

        Assert.assertEquals(result, attachments);
    }

    /**
     * Ensures that the Mentions property can be set and retrieved.
     */
    @Test
    public void TestGetMentions(){
        List<MessageActionsPayloadMention> mentions = new ArrayList<MessageActionsPayloadMention>();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setMentions(mentions);
        List<MessageActionsPayloadMention> result = messageActionsPayload.getMentions();

        Assert.assertEquals(result, mentions);
    }

    /**
     * Ensures that the Reactions property can be set and retrieved.
     */
    @Test
    public void TestGetReactions(){
        List<MessageActionsPayloadReaction> reactions = new ArrayList<MessageActionsPayloadReaction>();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        messageActionsPayload.setReactions(reactions);
        List<MessageActionsPayloadReaction> result = messageActionsPayload.getReactions();

        Assert.assertEquals(result, reactions);
    }
}
