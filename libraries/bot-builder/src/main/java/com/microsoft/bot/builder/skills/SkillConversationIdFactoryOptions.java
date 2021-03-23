// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.skills;

import com.microsoft.bot.schema.Activity;

/**
 * A class defining the parameters used in
 * {@link SkillConversationIdFactoryBase#createSkillConversationId(SkillConversationI
 * FactoryOptions,System#getThreading()#getCancellationToken())} .
 */
public class SkillConversationIdFactoryOptions {

    private String fromBotOAuthScope;

    private String fromBotId;

    private Activity activity;

    private BotFrameworkSkill botFrameworkSkill;

    /**
     * Gets the oauth audience scope, used during token retrieval
     * (either https://api.getbotframework().com or bot app id).
     * @return the FromBotOAuthScope value as a String.
     */
    public String getFromBotOAuthScope() {
        return this.fromBotOAuthScope;
    }

    /**
     * Sets the oauth audience scope, used during token retrieval
     * (either https://api.getbotframework().com or bot app id).
     * @param withFromBotOAuthScope The FromBotOAuthScope value.
     */
    public void setFromBotOAuthScope(String withFromBotOAuthScope) {
        this.fromBotOAuthScope = withFromBotOAuthScope;
    }

    /**
     * Gets the id of the parent bot that is messaging the skill.
     * @return the FromBotId value as a String.
     */
    public String getFromBotId() {
        return this.fromBotId;
    }

    /**
     * Sets the id of the parent bot that is messaging the skill.
     * @param withFromBotId The FromBotId value.
     */
    public void setFromBotId(String withFromBotId) {
        this.fromBotId = withFromBotId;
    }

    /**
     * Gets the activity which will be sent to the skill.
     * @return the Activity value as a getActivity().
     */
    public Activity getActivity() {
        return this.activity;
    }

    /**
     * Sets the activity which will be sent to the skill.
     * @param withActivity The Activity value.
     */
    public void setActivity(Activity withActivity) {
        this.activity = withActivity;
    }
    /**
     * Gets the skill to create the conversation Id for.
     * @return the BotFrameworkSkill value as a getBotFrameworkSkill().
     */
    public BotFrameworkSkill getBotFrameworkSkill() {
        return this.botFrameworkSkill;
    }

    /**
     * Sets the skill to create the conversation Id for.
     * @param withBotFrameworkSkill The BotFrameworkSkill value.
     */
    public void setBotFrameworkSkill(BotFrameworkSkill withBotFrameworkSkill) {
        this.botFrameworkSkill = withBotFrameworkSkill;
    }
}

