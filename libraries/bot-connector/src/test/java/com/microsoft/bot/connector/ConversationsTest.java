// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.rest.ErrorResponseException;
import com.microsoft.bot.schema.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;

public class ConversationsTest extends BotConnectorTestBase {

    @Test
    public void CreateConversation() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Create Conversation");

        ConversationParameters params = new ConversationParameters();
        params.setMembers(Collections.singletonList(user));
        params.setBot(bot);
        params.setActivity(activity);

        ConversationResourceResponse result = connector.getConversations().createConversation(params).join();

        Assert.assertNotNull(result.getActivityId());
    }

    @Test
    public void CreateConversationWithInvalidBot() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Create Conversation");

        bot.setId("invalid-id");
        ConversationParameters params = new ConversationParameters();
        params.setMembers(Collections.singletonList(user));
        params.setBot(bot);
        params.setActivity(activity);

        try {
            ConversationResourceResponse result = connector.getConversations().createConversation(params).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals("ServiceError", ((ErrorResponseException)e.getCause()).body().getError().getCode());
                Assert.assertTrue(((ErrorResponseException)e.getCause()).body().getError().getMessage().startsWith("Invalid userId"));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void CreateConversationWithoutMembers() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Create Conversation");

        ConversationParameters params = new ConversationParameters();
        params.setMembers(Collections.<ChannelAccount>emptyList());
        params.setBot(bot);
        params.setActivity(activity);

        try {
            ConversationResourceResponse result = connector.getConversations().createConversation(params).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals("BadArgument", ((ErrorResponseException)e.getCause()).body().getError().getCode());
                Assert.assertTrue(((ErrorResponseException)e.getCause()).body().getError().getMessage().startsWith("Conversations"));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void CreateConversationWithBotMember() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Create Conversation");

        ConversationParameters params = new ConversationParameters();
        params.setMembers(Collections.singletonList(bot));
        params.setBot(bot);
        params.setActivity(activity);

        try {
            ConversationResourceResponse result = connector.getConversations().createConversation(params).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertEquals("BadArgument", ((ErrorResponseException)e.getCause()).body().getError().getCode());
        }
    }

    @Test
    public void CreateConversationWithNullParameter() {
        try {
            ConversationResourceResponse result = connector.getConversations().createConversation(null).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void GetConversationMembers() {

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        List<ChannelAccount> members = connector.getConversations().getConversationMembers(conversation.getId()).join();

        boolean hasUser = false;

        for (ChannelAccount member : members) {
            hasUser = member.getId().equals(user.getId());
            if (hasUser) break;
        }

        Assert.assertTrue(hasUser);
    }

    @Test
    public void GetConversationMembersWithInvalidConversationId() {

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        try {
            List<ChannelAccount> members = connector.getConversations().getConversationMembers(conversation.getId().concat("M")).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals("ServiceError", ((ErrorResponseException)e.getCause()).body().getError().getCode());
                Assert.assertTrue(((ErrorResponseException)e.getCause()).body().getError().getMessage().contains("The specified channel was not found"));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void GetConversationMembersWithNullConversationId() {
        try {
            List<ChannelAccount> members = connector.getConversations().getConversationMembers(null).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void GetConversationPagedMembers() {
        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        try {
            PagedMembersResult pagedMembers = connector.getConversations().getConversationPagedMembers(conversation.getId()).join();

            boolean hasUser = false;
            for (ChannelAccount member : pagedMembers.getMembers()) {
                hasUser = member.getId().equalsIgnoreCase(user.getId());
                if (hasUser)
                    break;
            }

            Assert.assertTrue(hasUser);
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().getError().getCode().toString());
        }
    }

    @Test
    public void GetConversationPagedMembersWithInvalidConversationId() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Get Activity Members");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);
        createMessage.setActivity(activity);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        try {
            connector.getConversations().getConversationPagedMembers(conversation.getId().concat("M")).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals(400, ((ErrorResponseException)e.getCause()).response().code());
            } else {
                throw e;
            }
        }
    }

    @Test
    public void SendToConversation() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setName("activity");
        activity.setText("TEST Send to Conversation");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity).join();

        Assert.assertNotNull(response.getId());
    }

    @Test
    public void SendToConversationWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setName("activity");
        activity.setText("TEST Send to Conversation");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        try {
            ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId().concat("M"), activity).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals("ServiceError", ((ErrorResponseException)e.getCause()).body().getError().getCode());
                Assert.assertTrue(((ErrorResponseException)e.getCause()).body().getError().getMessage().contains("The specified channel was not found"));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void SendToConversationWithInvalidBotId() {

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        bot.setId("B21S8SG7K:T03CWQ0QB");
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setName("activity");
        activity.setText("TEST Send to Conversation");

        try {
            ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals("MissingProperty", ((ErrorResponseException)e.getCause()).body().getError().getCode());
                Assert.assertEquals("The bot referenced by the 'from' field is unrecognized", ((ErrorResponseException)e.getCause()).body().getError().getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test
    public void SendToConversationWithNullConversationId() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Send to Conversation with null conversation id");

        try {
            connector.getConversations().sendToConversation(null, activity).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void SendToConversationWithNullActivity() {
        try {
            connector.getConversations().sendToConversation("id",null).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }


    @Test
    public void SendCardToConversation() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setName("activity");
        activity.setText("TEST Send Card to Conversation");
        CardImage imageJPEG = new CardImage();
        imageJPEG.setUrl("https://docs.microsoft.com/en-us/bot-framework/media/designing-bots/core/dialogs-screens.png");
        HeroCard heroCardJPEG = new HeroCard();
        heroCardJPEG.setTitle("A static image");
        heroCardJPEG.setSubtitle("JPEG image");
        heroCardJPEG.setImages(Collections.singletonList(imageJPEG));
        Attachment attachmentJPEG = new Attachment();
        attachmentJPEG.setContentType("application/vnd.microsoft.card.hero");
        attachmentJPEG.setContent(heroCardJPEG);

        CardImage imageGIF = new CardImage();
        imageGIF.setUrl("http://i.giphy.com/Ki55RUbOV5njy.gif");
        HeroCard heroCardGIF = new HeroCard();
        heroCardGIF.setTitle("An animation");
        heroCardGIF.setSubtitle("GIF image");
        heroCardGIF.setImages(Collections.singletonList(imageGIF));
        Attachment attachmentGIF = new Attachment();
        attachmentGIF.setContentType("application/vnd.microsoft.card.hero");
        attachmentGIF.setContent(heroCardGIF);
        activity.setAttachments(Arrays.asList(attachmentJPEG, attachmentGIF));
        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity).join();

        Assert.assertNotNull(response.getId());
    }

    @Test
    public void GetActivityMembers() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Get Activity Members");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);
        createMessage.setActivity(activity);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        List<ChannelAccount> members = connector.getConversations().getActivityMembers(conversation.getId(), conversation.getActivityId()).join();

        boolean hasUser = false;

        for (ChannelAccount member : members) {
            hasUser = member.getId().equals(user.getId());
            if (hasUser) break;
        }

        Assert.assertTrue(hasUser);
    }

    @Test
    public void GetActivityMembersWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Get Activity Members");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);
        createMessage.setActivity(activity);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        try {
            List<ChannelAccount> members = connector.getConversations().getActivityMembers(conversation.getId().concat("M"), conversation.getActivityId()).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals("ServiceError", ((ErrorResponseException)e.getCause()).body().getError().getCode());
                Assert.assertTrue(((ErrorResponseException)e.getCause()).body().getError().getMessage().contains("The specified channel was not found"));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void GetActivityMembersWithNullConversationId() {
        try {
            connector.getConversations().getActivityMembers(null, "id").join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void GetActivityMembersWithNullActivityId() {
        try {
            connector.getConversations().getActivityMembers("id", null).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void ReplyToActivity() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Send to Conversation");

        Activity reply = new Activity(ActivityTypes.MESSAGE);
        reply.setRecipient(user);
        reply.setFrom(bot);
        reply.setText("TEST Reply to Activity");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity).join();

        ResourceResponse replyResponse = connector.getConversations().replyToActivity(conversation.getId(), response.getId(), reply).join();

        Assert.assertNotNull(replyResponse.getId());
    }

    @Test
    public void ReplyToActivityWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Send to Conversation");

        Activity reply = new Activity(ActivityTypes.MESSAGE);
        reply.setRecipient(user);
        reply.setFrom(bot);
        reply.setText("TEST Reply to Activity");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity).join();

        try {
            ResourceResponse replyResponse = connector.getConversations().replyToActivity(conversation.getId().concat("M"), response.getId(), reply).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals("ServiceError", ((ErrorResponseException)e.getCause()).body().getError().getCode());
                Assert.assertTrue(((ErrorResponseException)e.getCause()).body().getError().getMessage().contains("The specified channel was not found"));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void ReplyToActivityWithNullConversationId() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Reply activity with null conversation id");

        try {
            connector.getConversations().replyToActivity(null, "id", activity).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void ReplyToActivityWithNullActivityId() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Reply activity with null activity id");

        try {
            connector.getConversations().replyToActivity("id", null, activity).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void ReplyToActivityWithNullActivity() {
        try {
            connector.getConversations().replyToActivity("id", "id", null).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void ReplyToActivityWithNullReply() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Reply activity with null reply");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity).join();

        try {
            ResourceResponse replyResponse = connector.getConversations().replyToActivity(conversation.getId(), response.getId(), null).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void DeleteActivity() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Delete Activity");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);
        createMessage.setActivity(activity);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        connector.getConversations().deleteActivity(conversation.getId(), conversation.getActivityId());

        Assert.assertNotNull(conversation.getActivityId());
    }

    @Test
    public void DeleteActivityWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Delete Activity");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);
        createMessage.setActivity(activity);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        try {
            connector.getConversations().deleteActivity("B21S8SG7K:T03CWQ0QB", conversation.getActivityId()).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals("ServiceError", ((ErrorResponseException)e.getCause()).body().getError().getCode());
                Assert.assertTrue(((ErrorResponseException)e.getCause()).body().getError().getMessage().contains("Invalid ConversationId"));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void DeleteActivityWithNullConversationId() {
        try {
            connector.getConversations().deleteActivity(null, "id").join();
            Assert.fail("expected exception did not occur.");
        } catch(CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void DeleteActivityWithNullActivityId() {
        try {
            connector.getConversations().deleteActivity("id", null).join();
            Assert.fail("expected exception did not occur.");
        } catch(CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void UpdateActivity() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Send to Conversation");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity).join();

        activity.setId(response.getId());
        activity.setText("TEST Update Activity");

        ResourceResponse updateResponse = connector.getConversations().updateActivity(conversation.getId(), response.getId(), activity).join();

        Assert.assertNotNull(updateResponse.getId());
    }

    @Test
    public void UpdateActivityWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Send to Conversation");

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity).join();

        activity.setId(response.getId());
        activity.setText("TEST Update Activity");

        try {
            ResourceResponse updateResponse = connector.getConversations().updateActivity("B21S8SG7K:T03CWQ0QB", response.getId(), activity).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            if (e.getCause() instanceof ErrorResponseException) {
                Assert.assertEquals("ServiceError", ((ErrorResponseException)e.getCause()).body().getError().getCode());
                Assert.assertTrue(((ErrorResponseException)e.getCause()).body().getError().getMessage().contains("Invalid ConversationId"));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void UpdateActivityWithNullConversationId() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Activity to be updated with null conversation Id");

        try {
            connector.getConversations().updateActivity(null, "id", activity).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void UpdateActivityWithNullActivityId() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setRecipient(user);
        activity.setFrom(bot);
        activity.setText("TEST Activity to be updated with null activity Id");

        try {
            connector.getConversations().updateActivity("id", null, activity).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void UpdateActivityWithNullActivity() {
        try {
            connector.getConversations().updateActivity("id", "id", null).join();
            Assert.fail("expected exception did not occur.");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("cannot be null"));
        }
    }

    @Test
    public void UploadAttachment() {

        AttachmentData attachment = new AttachmentData();
        attachment.setName("bot-framework.png");
        attachment.setType("image/png");
        attachment.setOriginalBase64(encodeToBase64(new File(getClass().getClassLoader().getResource("bot-framework.png").getFile())));

        ConversationParameters createMessage = new ConversationParameters();
        createMessage.setMembers(Collections.singletonList(user));
        createMessage.setBot(bot);

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage).join();

        ResourceResponse response = connector.getConversations().uploadAttachment(conversation.getId(), attachment).join();

        Assert.assertNotNull(response.getId());
    }

    private byte[] encodeToBase64(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] result = new byte[(int) file.length()];
            int size = fis.read(result);
            return result;
        } catch (Exception ex) {
            return null;
        }
    }
}
