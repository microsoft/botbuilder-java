// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.skills;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Registration for a BotFrameworkHttpProtocol super. Skill endpoint.
 */
public class BotFrameworkSkill {

    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    @JsonProperty(value = "appId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String appId;

    @JsonProperty(value = "skillEndpoint")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private URI skillEndpoint;

    /**
     * Gets Id of the skill.
     * @return the Id value as a String.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets Id of the skill.
     * @param withId The Id value.
     */
    public void setId(String withId) {
        this.id = withId;
    }
    /**
     * Gets appId of the skill.
     * @return the AppId value as a String.
     */
    public String getAppId() {
        return this.appId;
    }

    /**
     * Sets appId of the skill.
     * @param withAppId The AppId value.
     */
    public void setAppId(String withAppId) {
        this.appId = withAppId;
    }
    /**
     * Gets /api/messages endpoint for the skill.
     * @return the SkillEndpoint value as a Uri.
     */
    public URI getSkillEndpoint() {
        return this.skillEndpoint;
    }

    /**
     * Sets /api/messages endpoint for the skill.
     * @param withSkillEndpoint The SkillEndpoint value.
     */
    public void setSkillEndpoint(URI withSkillEndpoint) {
        this.skillEndpoint = withSkillEndpoint;
    }
}

