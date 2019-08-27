// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.microsoft.bot.connector.rest.ErrorResponseException;
import com.microsoft.bot.schema.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConversationsTest extends BotConnectorTestBase {

    @Test
    public void CreateConversation() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Create Conversation");
        }};

        ConversationParameters params = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
            setActivity(activity);
        }};

        ConversationResourceResponse result = connector.getConversations().createConversation(params);

        Assert.assertNotNull(result.getActivityId());
    }

    @Test
    public void CreateConversationWithInvalidBot() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Create Conversation");
        }};

        bot.setId("invalid-id");
        ConversationParameters params = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
            setActivity(activity);
        }};

        try {
            ConversationResourceResponse result = connector.getConversations().createConversation(params);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().getError().getCode().toString());
            Assert.assertTrue(e.body().getError().getMessage().startsWith("Invalid userId"));
        }
    }

    @Test
    public void CreateConversationWithoutMembers() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Create Conversation");
        }};

        ConversationParameters params = new ConversationParameters() {{
            setMembers(Collections.<ChannelAccount>emptyList());
            setBot(bot);
            setActivity(activity);
        }};

        try {
            ConversationResourceResponse result = connector.getConversations().createConversation(params);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("BadArgument", e.body().getError().getCode().toString());
            Assert.assertTrue(e.body().getError().getMessage().startsWith("Conversations"));
        }
    }

    @Test
    public void CreateConversationWithBotMember() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Create Conversation");
        }};

        ConversationParameters params = new ConversationParameters() {{
            setMembers(Collections.singletonList(bot));
            setBot(bot);
            setActivity(activity);
        }};

        try {
            ConversationResourceResponse result = connector.getConversations().createConversation(params);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("BadArgument", e.body().getError().getCode().toString());
        }
    }

    @Test
    public void GetConversationMembers() {

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        List<ChannelAccount> members = connector.getConversations().getConversationMembers(conversation.getId());

        boolean hasUser = false;

        for (ChannelAccount member : members) {
            hasUser = member.getId().equals(user.getId());
            if (hasUser) break;
        }

        Assert.assertTrue(hasUser);
    }

    @Test
    public void GetConversationMembersWithInvalidConversationId() {

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        try {
            List<ChannelAccount> members = connector.getConversations().getConversationMembers(conversation.getId().concat("M"));
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().getError().getCode().toString());
            Assert.assertTrue(e.body().getError().getMessage().contains("The specified channel was not found"));
        }
    }

    @Test
    public void GetConversationPagedMembers() {
        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        try {
            PagedMembersResult pagedMembers = connector.getConversations().getConversationPagedMembers(conversation.getId());

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
    public void SendToConversation() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setName("activity");
            setText("TEST Send to Conversation");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity);

        Assert.assertNotNull(response.getId());
    }

    @Test
    public void SendToConversationWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setName("activity");
            setText("TEST Send to Conversation");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        try {
            ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId().concat("M"), activity);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().getError().getCode().toString());
            Assert.assertTrue(e.body().getError().getMessage().contains("The specified channel was not found"));
        }
    }

    @Test
    public void SendToConversationWithInvalidBotId() {

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        bot.setId("B21S8SG7K:T03CWQ0QB");
        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setName("activity");
            setText("TEST Send to Conversation");
        }};

        try {
            ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("MissingProperty", e.body().getError().getCode().toString());
            Assert.assertEquals("The bot referenced by the 'from' field is unrecognized", e.body().getError().getMessage());
        }
    }

    @Test
    public void SendCardToConversation() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setName("activity");
            setText("TEST Send Card to Conversation");
            setAttachments(Arrays.asList(
                new Attachment() {{
                    setContentType("application/vnd.microsoft.card.hero");
                    setContent(new HeroCard() {{
                        setTitle("A static image");
                        setSubtitle("JPEG image");
                        setImages(Collections.singletonList(new CardImage() {{
                            setUrl("https://docs.microsoft.com/en-us/bot-framework/media/designing-bots/core/dialogs-screens.png");
                        }}));
                    }});
                }},
                new Attachment() {{
                    setContentType("application/vnd.microsoft.card.hero");
                    setContent(new HeroCard() {{
                        setTitle("An animation");
                        setSubtitle("GIF image");
                        setImages(Collections.singletonList(new CardImage() {{
                            setUrl("http://i.giphy.com/Ki55RUbOV5njy.gif");
                        }}));
                    }});
                }}
            ));
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity);

        Assert.assertNotNull(response.getId());
    }

    @Test
    public void GetActivityMembers() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Get Activity Members");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
            setActivity(activity);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        List<ChannelAccount> members = connector.getConversations().getActivityMembers(conversation.getId(), conversation.getActivityId());

        boolean hasUser = false;

        for (ChannelAccount member : members) {
            hasUser = member.getId().equals(user.getId());
            if (hasUser) break;
        }

        Assert.assertTrue(hasUser);
    }

    @Test
    public void GetActivityMembersWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Get Activity Members");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
            setActivity(activity);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        try {
            List<ChannelAccount> members = connector.getConversations().getActivityMembers(conversation.getId().concat("M"), conversation.getActivityId());
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().getError().getCode().toString());
            Assert.assertTrue(e.body().getError().getMessage().contains("The specified channel was not found"));
        }
    }

    @Test
    public void ReplyToActivity() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Send to Conversation");
        }};

        Activity reply = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Reply to Activity");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity);

        ResourceResponse replyResponse = connector.getConversations().replyToActivity(conversation.getId(), response.getId(), reply);

        Assert.assertNotNull(replyResponse.getId());
    }

    @Test
    public void ReplyToActivityWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Send to Conversation");
        }};

        Activity reply = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Reply to Activity");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity);

        try {
            ResourceResponse replyResponse = connector.getConversations().replyToActivity(conversation.getId().concat("M"), response.getId(), reply);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().getError().getCode().toString());
            Assert.assertTrue(e.body().getError().getMessage().contains("The specified channel was not found"));
        }
    }

    @Test
    public void DeleteActivity() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Delete Activity");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
            setActivity(activity);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        connector.getConversations().deleteActivity(conversation.getId(), conversation.getActivityId());

        Assert.assertNotNull(conversation.getActivityId());
    }

    @Test
    public void DeleteActivityWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Delete Activity");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
            setActivity(activity);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        try {
            connector.getConversations().deleteActivity("B21S8SG7K:T03CWQ0QB", conversation.getActivityId());
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().getError().getCode().toString());
            Assert.assertTrue(e.body().getError().getMessage().contains("Invalid ConversationId"));
        }
    }

    @Test
    public void UpdateActivity() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Send to Conversation");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity);

        activity.setId(response.getId());
        activity.setText("TEST Update Activity");

        ResourceResponse updateResponse = connector.getConversations().updateActivity(conversation.getId(), response.getId(), activity);

        Assert.assertNotNull(updateResponse.getId());
    }

    @Test
    public void UpdateActivityWithInvalidConversationId() {

        Activity activity = new Activity(ActivityTypes.MESSAGE) {{
            setRecipient(user);
            setFrom(bot);
            setText("TEST Send to Conversation");
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        ResourceResponse response = connector.getConversations().sendToConversation(conversation.getId(), activity);

        activity.setId(response.getId());
        activity.setText("TEST Update Activity");

        try {
            ResourceResponse updateResponse = connector.getConversations().updateActivity("B21S8SG7K:T03CWQ0QB", response.getId(), activity);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().getError().getCode().toString());
            Assert.assertTrue(e.body().getError().getMessage().contains("Invalid ConversationId"));
        }
    }

    @Test
    public void UploadAttachment() {

        AttachmentData attachment = new AttachmentData() {{
            setName("bot-framework.png");
            setType("image/png");
            setOriginalBase64(encodeToBase64(new File(getClass().getClassLoader().getResource("bot-framework.png").getFile())));
        }};

        ConversationParameters createMessage = new ConversationParameters() {{
            setMembers(Collections.singletonList(user));
            setBot(bot);
        }};

        ConversationResourceResponse conversation = connector.getConversations().createConversation(createMessage);

        ResourceResponse response = connector.getConversations().uploadAttachment(conversation.getId(), attachment);

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
