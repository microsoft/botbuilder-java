// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MessageActionsPayloadTest {
    @Test
    public void TestMessageActionPayloadConstructor(){
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload();
        Assert.assertNotNull(messageActionsPayload);
    }

    @Test
    public void TestGetId(){
        String id = "testId";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setId(id);
            }
        };
        String result = messageActionsPayload.getId();

        Assert.assertEquals(result, id);
    }

    @Test
    public void TestGetReplyToId(){
        String replyToId = "testReplyToId";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setReplyToId(replyToId);
            }
        };
        String result = messageActionsPayload.getReplyToId();

        Assert.assertEquals(result, replyToId);
    }

    @Test
    public void TestGetMessageType(){
        String messageType = "testMessageType";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setMessageType(messageType);
            }
        };
        String result = messageActionsPayload.getMessageType();

        Assert.assertEquals(result, messageType);
    }

    @Test
    public void TestGetCreatedDateTime(){
        String createdDateTime = "2000-01-01";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setCreatedDateTime(createdDateTime);
            }
        };
        String result = messageActionsPayload.getCreatedDateTime();

        Assert.assertEquals(result, createdDateTime);
    }

    @Test
    public void TestGetLastModifiedDateTime(){
        String lastModifiedDateTime = "2000-01-01";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setLastModifiedDateTime(lastModifiedDateTime);
            }
        };
        String result = messageActionsPayload.getLastModifiedDateTime();

        Assert.assertEquals(result, lastModifiedDateTime);
    }

    @Test
    public void TestGetDeleted(){
        Boolean deleted = false;
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setDeleted(deleted);
            }
        };
        Boolean result = messageActionsPayload.getDeleted();

        Assert.assertEquals(result, deleted);
    }

    @Test
    public void TestGetSubject(){
        String subject = "testSubject";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setSubject(subject);
            }
        };
        String result = messageActionsPayload.getSubject();

        Assert.assertEquals(result, subject);
    }

    @Test
    public void TestGetSummary(){
        String summary = "testSummary";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setSummary(summary);
            }
        };
        String result = messageActionsPayload.getSummary();

        Assert.assertEquals(result, summary);
    }

    @Test
    public void TestGetImportance(){
        String importance = "normal";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setImportance(importance);
            }
        };
        String result = messageActionsPayload.getImportance();

        Assert.assertEquals(result, importance);
    }

    @Test
    public void TestGetLinkToMessage(){
        String linkToMessage = "https://teams.microsoft.com/l/message/testing-id";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setLinkToMessage(linkToMessage);
            }
        };
        String result = messageActionsPayload.getLinkToMessage();

        Assert.assertEquals(result, linkToMessage);
    }

    @Test
    public void TestGetLocale(){
        String locale = "US";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setLocale(locale);
            }
        };
        String result = messageActionsPayload.getLocale();

        Assert.assertEquals(result, locale);
    }

    @Test
    public void TestGetFrom(){
        MessageActionsPayloadFrom from = new MessageActionsPayloadFrom();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setFrom(from);
            }
        };
        MessageActionsPayloadFrom result = messageActionsPayload.getFrom();

        Assert.assertEquals(result, from);
    }

    @Test
    public void TestGetBody(){
        MessageActionsPayloadBody body = new MessageActionsPayloadBody();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setBody(body);
            }
        };
        MessageActionsPayloadBody result = messageActionsPayload.getBody();

        Assert.assertEquals(result, body);
    }

    @Test
    public void TestGetAttachmentLayout(){
        String attachmentLayout = "testAttachmentLayout";
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setAttachmentLayout(attachmentLayout);
            }
        };
        String result = messageActionsPayload.getAttachmentLayout();

        Assert.assertEquals(result, attachmentLayout);
    }

    @Test
    public void TestGetAttachments(){
        List<MessageActionsPayloadAttachment> attachments = new ArrayList<MessageActionsPayloadAttachment>();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setAttachments(attachments);
            }
        };
        List<MessageActionsPayloadAttachment> result = messageActionsPayload.getAttachments();

        Assert.assertEquals(result, attachments);
    }

    @Test
    public void TestGetMentions(){
        List<MessageActionsPayloadMention> mentions = new ArrayList<MessageActionsPayloadMention>();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setMentions(mentions);
            }
        };
        List<MessageActionsPayloadMention> result = messageActionsPayload.getMentions();

        Assert.assertEquals(result, mentions);
    }

    @Test
    public void TestGetReactions(){
        List<MessageActionsPayloadReaction> reactions = new ArrayList<MessageActionsPayloadReaction>();
        MessageActionsPayload messageActionsPayload = new MessageActionsPayload(){
            {
                setReactions(reactions);
            }
        };
        List<MessageActionsPayloadReaction> result = messageActionsPayload.getReactions();

        Assert.assertEquals(result, reactions);
    }
}
