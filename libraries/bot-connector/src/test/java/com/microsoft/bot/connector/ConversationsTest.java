package com.microsoft.bot.connector;

import com.microsoft.bot.connector.models.ErrorResponseException;
import com.microsoft.bot.schema.models.*;
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

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Create Conversation");

        ConversationParameters params = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponse result = connector.conversations().createConversation(params);

        Assert.assertNotNull(result.activityId());
    }

    @Test
    public void CreateConversationWithInvalidBot() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Create Conversation");

        ConversationParameters params = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot.withId("invalid-id"))
                .withActivity(activity);

        try {
            ConversationResourceResponse result = connector.conversations().createConversation(params);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().startsWith("Invalid userId"));
        }
    }

    @Test
    public void CreateConversationWithoutMembers() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Create Conversation");

        ConversationParameters params = new ConversationParameters()
                .withMembers(Collections.<ChannelAccount>emptyList())
                .withBot(bot)
                .withActivity(activity);

        try {
            ConversationResourceResponse result = connector.conversations().createConversation(params);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("BadArgument", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().startsWith("Conversations"));
        }
    }

    @Test
    public void CreateConversationWithBotMember() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Create Conversation");

        ConversationParameters params = new ConversationParameters()
                .withMembers(Collections.singletonList(bot))
                .withBot(bot)
                .withActivity(activity);

        try {
            ConversationResourceResponse result = connector.conversations().createConversation(params);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("BadArgument", e.body().error().code().toString());
        }
    }

    @Test
    public void GetConversationMembers() {

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        List<ChannelAccount> members = connector.conversations().getConversationMembers(conversation.id());

        boolean hasUser = false;

        for (ChannelAccount member : members) {
            hasUser = member.id().equals(user.id());
            if (hasUser) break;
        }

        Assert.assertTrue(hasUser);
    }

    @Test
    public void GetConversationMembersWithInvalidConversationId() {

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        try {
            List<ChannelAccount> members = connector.conversations().getConversationMembers(conversation.id().concat("M"));
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("The specified channel was not found"));
        }
    }

    @Test
    public void SendToConversation() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withName("activity")
                .withText("TEST Send to Conversation");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        ResourceResponse response = connector.conversations().sendToConversation(conversation.id(), activity);

        Assert.assertNotNull(response.id());
    }

    @Test
    public void SendToConversationWithInvalidConversationId() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withName("activity")
                .withText("TEST Send to Conversation");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        try {
            ResourceResponse response = connector.conversations().sendToConversation(conversation.id().concat("M"), activity);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("The specified channel was not found"));
        }
    }

    @Test
    public void SendToConversationWithInvalidBotId() {

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot.withId("B21S8SG7K:T03CWQ0QB"))
                .withName("activity")
                .withText("TEST Send to Conversation");

        try {
            ResourceResponse response = connector.conversations().sendToConversation(conversation.id(), activity);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("MissingProperty", e.body().error().code().toString());
            Assert.assertEquals("The bot referenced by the 'from' field is unrecognized", e.body().error().message());
        }
    }

    @Test
    public void SendCardToConversation() {

        Activity activity = new Activity()
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

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        ResourceResponse response = connector.conversations().sendToConversation(conversation.id(), activity);

        Assert.assertNotNull(response.id());
    }

    @Test
    public void GetActivityMembers() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Get Activity Members");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        List<ChannelAccount> members = connector.conversations().getActivityMembers(conversation.id(), conversation.activityId());

        boolean hasUser = false;

        for (ChannelAccount member : members) {
            hasUser = member.id().equals(user.id());
            if (hasUser) break;
        }

        Assert.assertTrue(hasUser);
    }

    @Test
    public void GetActivityMembersWithInvalidConversationId() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Get Activity Members");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        try {
            List<ChannelAccount> members = connector.conversations().getActivityMembers(conversation.id().concat("M"), conversation.activityId());
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("The specified channel was not found"));
        }
    }

    @Test
    public void ReplyToActivity() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Send to Conversation");

        Activity reply = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Reply to Activity");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        ResourceResponse response = connector.conversations().sendToConversation(conversation.id(), activity);

        ResourceResponse replyResponse = connector.conversations().replyToActivity(conversation.id(), response.id(), reply);

        Assert.assertNotNull(replyResponse.id());
    }

    @Test
    public void ReplyToActivityWithInvalidConversationId() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Send to Conversation");

        Activity reply = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Reply to Activity");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        ResourceResponse response = connector.conversations().sendToConversation(conversation.id(), activity);

        try {
            ResourceResponse replyResponse = connector.conversations().replyToActivity(conversation.id().concat("M"), response.id(), reply);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("The specified channel was not found"));
        }
    }

    @Test
    public void DeleteActivity() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Delete Activity");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        connector.conversations().deleteActivity(conversation.id(), conversation.activityId());

        Assert.assertNotNull(conversation.activityId());
    }

    @Test
    public void DeleteActivityWithInvalidConversationId() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Delete Activity");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot)
                .withActivity(activity);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

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

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Send to Conversation");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        ResourceResponse response = connector.conversations().sendToConversation(conversation.id(), activity);

        Activity update = activity.withId(response.id())
                .withText("TEST Update Activity");

        ResourceResponse updateResponse = connector.conversations().updateActivity(conversation.id(), response.id(), update);

        Assert.assertNotNull(updateResponse.id());
    }

    @Test
    public void UpdateActivityWithInvalidConversationId() {

        Activity activity = new Activity()
                .withType(ActivityTypes.MESSAGE)
                .withRecipient(user)
                .withFrom(bot)
                .withText("TEST Send to Conversation");

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        ResourceResponse response = connector.conversations().sendToConversation(conversation.id(), activity);

        Activity update = activity.withId(response.id())
                .withText("TEST Update Activity");

        try {
            ResourceResponse updateResponse = connector.conversations().updateActivity("B21S8SG7K:T03CWQ0QB", response.id(), update);
            Assert.fail("expected exception was not occurred.");
        } catch (ErrorResponseException e) {
            Assert.assertEquals("ServiceError", e.body().error().code().toString());
            Assert.assertTrue(e.body().error().message().contains("Invalid ConversationId"));
        }
    }

    @Test
    public void UploadAttachment() {

        AttachmentData attachment = new AttachmentData()
                .withName("bot-framework.png")
                .withType("image/png")
                .withOriginalBase64(encodeToBase64(new File(getClass().getClassLoader().getResource("bot-framework.png").getFile())));

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        ResourceResponse response = connector.conversations().uploadAttachment(conversation.id(), attachment);

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
