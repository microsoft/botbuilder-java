// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.skills.BotFrameworkClient;
import com.microsoft.bot.builder.skills.BotFrameworkSkill;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryBase;

/**
 * Defines the options that will be used to execute a {@link SkillDialog} .
 */
public class SkillDialogOptions {

    @JsonProperty(value = "botId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String botId;

    private BotFrameworkClient skillClient;

    @JsonProperty(value = "skillHostEndpoint")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private URI skillHostEndpoint;

    @JsonProperty(value = "skill")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private BotFrameworkSkill skill;

    private SkillConversationIdFactoryBase conversationIdFactory;

    private ConversationState conversationState;

    @JsonProperty(value = "connectionName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionName;

    /**
     * Gets the Microsoft app ID of the bot calling the skill.
     * @return the BotId value as a String.
     */
    public String getBotId() {
        return this.botId;
    }

    /**
     * Sets the Microsoft app ID of the bot calling the skill.
     * @param withBotId The BotId value.
     */
    public void setBotId(String withBotId) {
        this.botId = withBotId;
    }
    /**
     * Gets the {@link BotFrameworkClient} used to call the remote
     * skill.
     * @return the SkillClient value as a BotFrameworkClient.
     */
    public BotFrameworkClient getSkillClient() {
        return this.skillClient;
    }

    /**
     * Sets the {@link BotFrameworkClient} used to call the remote
     * skill.
     * @param withSkillClient The SkillClient value.
     */
    public void setSkillClient(BotFrameworkClient withSkillClient) {
        this.skillClient = withSkillClient;
    }
    /**
     * Gets the callback Url for the skill host.
     * @return the SkillHostEndpoint value as a Uri.
     */
    public URI getSkillHostEndpoint() {
        return this.skillHostEndpoint;
    }

    /**
     * Sets the callback Url for the skill host.
     * @param withSkillHostEndpoint The SkillHostEndpoint value.
     */
    public void setSkillHostEndpoint(URI withSkillHostEndpoint) {
        this.skillHostEndpoint = withSkillHostEndpoint;
    }
    /**
     * Gets the {@link BotFrameworkSkill} that the dialog will call.
     * @return the Skill value as a BotFrameworkSkill.
     */
    public BotFrameworkSkill getSkill() {
        return this.skill;
    }

    /**
     * Sets the {@link BotFrameworkSkill} that the dialog will call.
     * @param withSkill The Skill value.
     */
    public void setSkill(BotFrameworkSkill withSkill) {
        this.skill = withSkill;
    }
    /**
     * Gets an instance of a {@link SkillConversationIdFactoryBase}
     * used to generate conversation IDs for interacting with the skill.
     * @return the ConversationIdFactory value as a SkillConversationIdFactoryBase.
     */
    public SkillConversationIdFactoryBase getConversationIdFactory() {
        return this.conversationIdFactory;
    }

    /**
     * Sets an instance of a {@link SkillConversationIdFactoryBase}
     * used to generate conversation IDs for interacting with the skill.
     * @param withConversationIdFactory The ConversationIdFactory value.
     */
    public void setConversationIdFactory(SkillConversationIdFactoryBase withConversationIdFactory) {
        this.conversationIdFactory = withConversationIdFactory;
    }
    /**
     * Gets the {@link ConversationState} to be used by the dialog.
     * @return the ConversationState value as a getConversationState().
     */
    public ConversationState getConversationState() {
        return this.conversationState;
    }

    /**
     * Sets the {@link ConversationState} to be used by the dialog.
     * @param withConversationState The ConversationState value.
     */
    public void setConversationState(ConversationState withConversationState) {
        this.conversationState = withConversationState;
    }
    /**
     * Gets the OAuth Connection Name, that would be used to perform
     * Single SignOn with a skill.
     * @return the ConnectionName value as a String.
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    /**
     * Sets the OAuth Connection Name, that would be used to perform
     * Single SignOn with a skill.
     * @param withConnectionName The ConnectionName value.
     */
    public void setConnectionName(String withConnectionName) {
        this.connectionName = withConnectionName;
    }
}
