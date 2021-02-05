// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Entity;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class MentionTests {
    static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.findAndRegisterModules();
    }

    @Test
    public void Mention_Skype() throws IOException {
        // A Skype mention contains the user mention enclosed in <at> tags. But the
        // activity.getText() (as below)
        // does not.
        String mentionJson = "{\"mentioned\": {\"id\": \"recipientid\"},\"text\": \"<at id='28: 841caffa-9e92-425d-8d84-b503b3ded285'>botname</at>\"}";
        Entity mention = mapper.readValue(mentionJson, Entity.class);
        mention.setType("mention");

        Activity activity = MessageFactory.text("botname sometext");
        activity.setChannelId("skype");
        activity.getEntities().add(mention);

        // Normalize the Skype mention so that it is in a format RemoveMentionText can
        // handle.
        // If SkypeMentionNormalizeMiddleware is added to the adapters Middleware set,
        // this
        // will be called on every Skype message.
        SkypeMentionNormalizeMiddleware.normalizeSkypeMentionText(activity);

        // This will remove the Mention.Text from the activity.getText(). This should
        // just leave before/after the
        // mention.
        activity.removeMentionText("recipientid");

        Assert.assertEquals(activity.getText(), "sometext");
    }

    @Test
    public void Mention_Teams() throws IOException {
        String mentionJson = "{\"mentioned\": {\"id\": \"recipientid\"},\"text\": \"<at>botname</at>\"}";
        Entity mention = mapper.readValue(mentionJson, Entity.class);
        mention.setType("mention");

        Activity activity = MessageFactory.text("<at>botname</at> sometext");
        activity.getEntities().add(mention);

        activity.removeMentionText("recipientid");

        Assert.assertEquals(activity.getText(), "sometext");
    }

    @Test
    public void Mention_slack() throws IOException {
        String mentionJson = "{\"mentioned\": {\"id\": \"recipientid\"},\"text\": \"@botname\"}";
        Entity mention = mapper.readValue(mentionJson, Entity.class);
        mention.setType("mention");

        Activity activity = MessageFactory.text("@botname sometext");
        activity.getEntities().add(mention);

        activity.removeMentionText("recipientid");

        Assert.assertEquals(activity.getText(), "sometext");
    }

    @Test
    public void Mention_GroupMe() throws IOException {
        String mentionJson = "{\"mentioned\": {\"id\": \"recipientid\"},\"text\": \"@bot name\"}";
        Entity mention = mapper.readValue(mentionJson, Entity.class);
        mention.setType("mention");

        Activity activity = MessageFactory.text("@bot name sometext");
        activity.getEntities().add(mention);

        activity.removeMentionText("recipientid");

        Assert.assertEquals(activity.getText(), "sometext");
    }

    @Test
    public void Mention_Telegram() throws IOException {
        String mentionJson = "{\"mentioned\": {\"id\": \"recipientid\"},\"text\": \"botname\"}";
        Entity mention = mapper.readValue(mentionJson, Entity.class);
        mention.setType("mention");

        Activity activity = MessageFactory.text("botname sometext");
        activity.getEntities().add(mention);

        activity.removeMentionText("recipientid");

        Assert.assertEquals(activity.getText(), "sometext");
    }

    @Test
    public void Mention_Facebook() {
        // no-op for now: Facebook mentions unknown at this time
    }

    @Test
    public void Mention_Email() {
        // no-op for now: EMail mentions not included in activity.getText()?
    }

    @Test
    public void Mention_Cortana() {
        // no-op for now: Cortana mentions unknown at this time
    }

    @Test
    public void Mention_Kik() {
        // no-op for now: bot mentions in Kik don't get Entity info and not included in
        // activity.getText()
    }

    @Test
    public void Mention_Twilio() {
        // no-op for now: Twilio mentions unknown at this time. Could not determine if
        // they are supported.
    }
}
