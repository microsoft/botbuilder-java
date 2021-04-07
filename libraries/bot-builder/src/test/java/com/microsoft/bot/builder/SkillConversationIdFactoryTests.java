// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import com.microsoft.bot.builder.skills.BotFrameworkSkill;
import com.microsoft.bot.builder.skills.SkillConversationIdFactory;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryOptions;
import com.microsoft.bot.builder.skills.SkillConversationReference;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationReference;

import org.junit.Test;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;


public class SkillConversationIdFactoryTests {

    private static final String SERVICE_URL = "http://testbot.com/api/messages";
    private final String skillId = "skill";

    private final SkillConversationIdFactory skillConversationIdFactory =
                                                 new SkillConversationIdFactory(new MemoryStorage());
    private final String applicationId = UUID.randomUUID().toString();
    private final String botId = UUID.randomUUID().toString();

    @Test
    public void SkillConversationIdFactoryHappyPath() {
        ConversationReference conversationReference = buildConversationReference();

        // Create skill conversation
        SkillConversationIdFactoryOptions options = new SkillConversationIdFactoryOptions();
        options.setActivity(buildMessageActivity(conversationReference));
        options.setBotFrameworkSkill(this.buildBotFrameworkSkill());
        options.setFromBotId(botId);
        options.setFromBotOAuthScope(botId);


        String skillConversationId =  skillConversationIdFactory.createSkillConversationId(options).join();

        Assert.assertFalse(StringUtils.isBlank(skillConversationId));

        // Retrieve skill conversation
        SkillConversationReference retrievedConversationReference =
             skillConversationIdFactory.getSkillConversationReference(skillConversationId).join();

        // Delete
         skillConversationIdFactory.deleteConversationReference(skillConversationId).join();

        // Retrieve again
        SkillConversationReference deletedConversationReference =
                    skillConversationIdFactory.getSkillConversationReference(skillConversationId).join();

        Assert.assertNotNull(retrievedConversationReference);
        Assert.assertNotNull(retrievedConversationReference.getConversationReference());
        Assert.assertTrue(compareConversationReferences(conversationReference,
                                                       retrievedConversationReference.getConversationReference()));
        Assert.assertNull(deletedConversationReference);
    }

    @Test
    public void IdIsUniqueEachTime() {
        ConversationReference conversationReference = buildConversationReference();

        // Create skill conversation
        SkillConversationIdFactoryOptions options1 = new SkillConversationIdFactoryOptions();
        options1.setActivity(buildMessageActivity(conversationReference));
        options1.setBotFrameworkSkill(buildBotFrameworkSkill());
        options1.setFromBotId(botId);
        options1.setFromBotOAuthScope(botId);

        String firstId = skillConversationIdFactory.createSkillConversationId(options1).join();


        SkillConversationIdFactoryOptions options2 = new SkillConversationIdFactoryOptions();
        options2.setActivity(buildMessageActivity(conversationReference));
        options2.setBotFrameworkSkill(buildBotFrameworkSkill());
        options2.setFromBotId(botId);
        options2.setFromBotOAuthScope(botId);

        String secondId = skillConversationIdFactory.createSkillConversationId(options2).join();

        // Ensure that we get a different conversationId each time we call CreateSkillConversationIdAsync
        Assert.assertNotEquals(firstId, secondId);
    }



    private static ConversationReference buildConversationReference() {
        ConversationReference conversationReference = new ConversationReference();
        conversationReference.setConversation(new ConversationAccount(UUID.randomUUID().toString()));
        conversationReference.setServiceUrl(SERVICE_URL);
        return conversationReference;
    }

    private static Activity buildMessageActivity(ConversationReference conversationReference) {
        if (conversationReference == null) {
            throw new  IllegalArgumentException("conversationReference cannot be null.");
        }

        Activity activity = Activity.createMessageActivity();
        activity.applyConversationReference(conversationReference);

        return activity;
    }

    private BotFrameworkSkill buildBotFrameworkSkill() {
        BotFrameworkSkill skill = new BotFrameworkSkill();
        skill.setAppId(applicationId);
        skill.setId(skillId);
        try {
            skill.setSkillEndpoint(new URI(SERVICE_URL));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return skill;
    }

    private static boolean compareConversationReferences(
        ConversationReference reference1,
        ConversationReference reference2
    ) {
        return reference1.getConversation().getId() == reference2.getConversation().getId()
               && reference1.getServiceUrl() == reference2.getServiceUrl();
    }
}
