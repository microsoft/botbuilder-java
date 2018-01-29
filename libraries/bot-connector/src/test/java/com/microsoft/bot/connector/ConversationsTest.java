package com.microsoft.bot.connector;

import com.microsoft.bot.connector.implementation.*;
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

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Create Conversation");

        ConversationParametersInner params = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponseInner result = connector.conversations().createConversation(params);

        Assert.assertNotNull(result.activityId());
    }

    @Test
    public void CreateConversationWithInvalidBot() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Create Conversation");

        ConversationParametersInner params = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot.withId("invalid-id"))
                .withActivity(activity);

        try {
            ConversationResourceResponseInner result = connector.conversations().createConversation(params);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().startsWith("Invalid userId"));
        }
    }

    @Test
    public void CreateConversationWithoutMembers() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Create Conversation");

        ConversationParametersInner params = new ConversationParametersInner()
                .withMembers(Collections.<ChannelAccountInner>emptyList())
                .withBot(bot)
                .withActivity(activity);

        try {
            ConversationResourceResponseInner result = connector.conversations().createConversation(params);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("BadArgument", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().startsWith("Conversations"));
        }
    }

    @Test
    public void CreateConversationWithBotMember() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Create Conversation");

        ConversationParametersInner params = new ConversationParametersInner()
                .withMembers(Collections.singletonList(bot))
                .withBot(bot)
                .withActivity(activity);

        try {
            ConversationResourceResponseInner result = connector.conversations().createConversation(params);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("BadArgument", e.body().error().code().toString());
        }
    }

    @Test
    public void GetConversationMembers() {

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        List<ChannelAccountInner> members = connector.conversations().getConversationMembers(conversation.id());

        boolean hasUser = false;

        for (ChannelAccountInner member : members) {
            hasUser = member.id().equals(user.id());
            if (hasUser) break;
        }

        Assert.assertTrue(hasUser);
    }

    @Test
    public void GetConversationMembersWithInvalidConversationId() {

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        try {
            List<ChannelAccountInner> members = connector.conversations().getConversationMembers(conversation.id().concat("M"));
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("The specified channel was not found"));
        }
    }

    @Test
    public void SendToConversation() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withName("activity")
                .withText("TEST Send to Conversation");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        ResourceResponseInner response = connector.conversations().sendToConversation(conversation.id(), activity);

        Assert.assertNotNull(response.id());
    }

    @Test
    public void SendToConversationWithInvalidConversationId() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withName("activity")
                .withText("TEST Send to Conversation");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        try {
            ResourceResponseInner response = connector.conversations().sendToConversation(conversation.id().concat("M"), activity);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("The specified channel was not found"));
        }
    }

    @Test
    public void SendToConversationWithInvalidBotId() {

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot.withId("B21S8SG7K:T03CWQ0QB"))
                .withName("activity")
                .withText("TEST Send to Conversation");

        try {
            ResourceResponseInner response = connector.conversations().sendToConversation(conversation.id(), activity);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("MissingProperty", e.body().error().code().toString());
            Assert.assertEquals("The bot referenced by the 'from' field is unrecognized", e.body().error().message());
        }
    }

    @Test
    public void SendCardToConversation() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withName("activity")
                .withText("TEST Send Card to Conversation")
                .withAttachments(Arrays.asList(
                        new Attachment()
                                .withContentType("application/vnd.microsoft.card.hero")
                                .withContent(new HeroCard()
                                        .withTitle("A static image")
                                        .withSubtitle("JPEG image")
                                        .withImages(Collections.singletonList(new CardImage()
                                                .withUrl("https://docs.microsoft.com/en-us/bot-framework/media/designing-bots/core/dialogs-screens.png")))),
                        new Attachment()
                                .withContentType("application/vnd.microsoft.card.hero")
                                .withContent(new HeroCard()
                                        .withTitle("An animation")
                                        .withSubtitle("GIF image")
                                        .withImages(Collections.singletonList(new CardImage()
                                                .withUrl("http://i.giphy.com/Ki55RUbOV5njy.gif"))))

                ));

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        ResourceResponseInner response = connector.conversations().sendToConversation(conversation.id(), activity);

        Assert.assertNotNull(response.id());
    }

    @Test
    public void GetActivityMembers() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Get Activity Members");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        List<ChannelAccountInner> members = connector.conversations().getActivityMembers(conversation.id(), conversation.activityId());

        boolean hasUser = false;

        for (ChannelAccountInner member : members) {
            hasUser = member.id().equals(user.id());
            if (hasUser) break;
        }

        Assert.assertTrue(hasUser);
    }

    @Test
    public void GetActivityMembersWithInvalidConversationId() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Get Activity Members");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        try {
            List<ChannelAccountInner> members = connector.conversations().getActivityMembers(conversation.id().concat("M"), conversation.activityId());
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("The specified channel was not found"));
        }
    }

    @Test
    public void ReplyToActivity() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Send to Conversation");

        ActivityInner reply = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Reply to Activity");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        ResourceResponseInner response = connector.conversations().sendToConversation(conversation.id(), activity);

        ResourceResponseInner replyResponse = connector.conversations().replyToActivity(conversation.id(), response.id(), reply);

        Assert.assertNotNull(replyResponse.id());
    }

    @Test
    public void ReplyToActivityWithInvalidConversationId() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Send to Conversation");

        ActivityInner reply = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Reply to Activity");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        ResourceResponseInner response = connector.conversations().sendToConversation(conversation.id(), activity);

        try {
            ResourceResponseInner replyResponse = connector.conversations().replyToActivity(conversation.id().concat("M"), response.id(), reply);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("The specified channel was not found"));
        }
    }

    @Test
    public void DeleteActivity() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Delete Activity");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        connector.conversations().deleteActivity(conversation.id(), conversation.activityId());

        Assert.assertNotNull(conversation.activityId());
    }

    @Test
    public void DeleteActivityWithInvalidConversationId() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Delete Activity");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        try {
            connector.conversations().deleteActivity("B21S8SG7K:T03CWQ0QB", conversation.activityId());
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("Invalid ConversationId"));
        }
    }

    @Test
    public void UpdateActivity() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Send to Conversation");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        ResourceResponseInner response = connector.conversations().sendToConversation(conversation.id(), activity);

        ActivityInner update = activity.withId(response.id())
                .withText("TEST Update Activity");

        ResourceResponseInner updateResponse = connector.conversations().updateActivity(conversation.id(), response.id(), update);

        Assert.assertNotNull(updateResponse.id());
    }

    @Test
    public void UpdateActivityWithInvalidConversationId() {

        ActivityInner activity = new ActivityInner()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Send to Conversation");

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        ResourceResponseInner response = connector.conversations().sendToConversation(conversation.id(), activity);

        ActivityInner update = activity.withId(response.id())
                .withText("TEST Update Activity");

        try {
            ResourceResponseInner updateResponse = connector.conversations().updateActivity("B21S8SG7K:T03CWQ0QB", response.id(), update);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("Invalid ConversationId"));
        }
    }

    @Test
    public void UploadAttachment() {

        AttachmentDataInner attachment = new AttachmentDataInner()
                .withName("bot-framework.png")
                .withType("image/png")
                .withOriginalBase64(encodeToBase64(new File(getClass().getClassLoader().getResource("bot-framework.png").getFile())));

        ConversationParametersInner createMessage = new ConversationParametersInner()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponseInner conversation = connector.conversations().createConversation(createMessage);

        ResourceResponseInner response = connector.conversations().uploadAttachment(conversation.id(), attachment);

        Assert.assertNotNull(response.id());
    }

    private byte[] encodeToBase64(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] result = new byte[(int)file.length()];
            int size = fis.read(result);
            return result;
        } catch (Exception ex) {
            return null;
        }
    }
}
