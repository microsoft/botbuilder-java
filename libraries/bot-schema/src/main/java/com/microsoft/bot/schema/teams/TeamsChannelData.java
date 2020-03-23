// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Channel data specific to messages received in Microsoft Teams.
 */
public class TeamsChannelData {
    @JsonProperty(value = "teamsChannelId")
    private String teamsChannelId;

    @JsonProperty(value = "teamsTeamId")
    private String teamsTeamId;

    @JsonProperty(value = "channel")
    private ChannelInfo channel;

    /// Gets or sets type of event.
    @JsonProperty(value = "eventType")
    private String eventType;

    /// Gets or sets information about the team in which the message was
    /// sent
    @JsonProperty(value = "team")
    private TeamInfo team;

    /// Gets or sets notification settings for the message
    @JsonProperty(value = "notification")
    private NotificationInfo notification;

    /// Gets or sets information about the tenant in which the message was
    /// sent
    @JsonProperty(value = "tenant")
    private TenantInfo tenant;

    /**
     * Get unique identifier representing a channel.
     *
     * @return Unique identifier representing a channel.
     */
    public String getTeamsChannelId() {
        return teamsChannelId;
    }

    /**
     * Set unique identifier representing a channel.
     *
     * @param withTeamsChannelId Unique identifier representing a channel.
     */
    public void setTeamsChannelId(String withTeamsChannelId) {
        this.teamsChannelId = withTeamsChannelId;
    }

    /**
     * Get unique identifier representing a team.
     *
     * @return Unique identifier representing a team.
     */
    public String getTeamsTeamId() {
        return teamsTeamId;
    }

    /**
     * Set unique identifier representing a team.
     *
     * @param withTeamsTeamId Unique identifier representing a team.
     */
    public void setTeamsTeamId(String withTeamsTeamId) {
        this.teamsTeamId = withTeamsTeamId;
    }

    /**
     * Gets information about the channel in which the message was
     * sent.
     *
     * @return information about the channel in which the message was
     * sent.
     */
    public ChannelInfo getChannel() {
        return channel;
    }

    /**
     * Sets information about the channel in which the message was
     * sent.
     *
     * @param withChannel information about the channel in which the message was
     *                    sent.
     */
    public void setChannel(ChannelInfo withChannel) {
        this.channel = withChannel;
    }

    /**
     * Gets type of event.
     *
     * @return type of event.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets type of event.
     *
     * @param withEventType type of event.
     */
    public void setEventType(String withEventType) {
        this.eventType = withEventType;
    }

    /**
     * Gets information about the team in which the message was
     * sent.
     *
     * @return information about the team in which the message was
     * sent.
     */
    public TeamInfo getTeam() {
        return team;
    }

    /**
     * Sets information about the team in which the message was
     * sent.
     *
     * @param withTeam information about the team in which the message was
     *                 sent.
     */
    public void setTeam(TeamInfo withTeam) {
        this.team = withTeam;
    }

    /**
     * Gets notification settings for the message.
     *
     * @return notification settings for the message.
     */
    public NotificationInfo getNotification() {
        return notification;
    }

    /**
     * Sets notification settings for the message.
     *
     * @param withNotification settings for the message.
     */
    public void setNotification(NotificationInfo withNotification) {
        this.notification = withNotification;
    }

    /**
     * Gets information about the tenant in which the message was.
     *
     * @return information about the tenant in which the message was.
     */
    public TenantInfo getTenant() {
        return tenant;
    }

    /**
     * Sets information about the tenant in which the message was.
     *
     * @param withTenant information about the tenant in which the message was.
     */
    public void setTenant(TenantInfo withTenant) {
        this.tenant = withTenant;
    }

    /**
     * A new instance of TeamChannelData.
     *
     * @param withTeamsChannelId the channelId in Teams
     * @param withTeamsTeamId    the teamId in Teams
     * @param withChannel        information about the channel in which the message was sent.
     * @param withEventType      type of event.
     * @param withTeam           information about the team in which the message was
     *                           sent.
     * @param withNotification   Notification settings for the message.
     * @param withTenant         Information about the tenant in which the message was.
     */
    public TeamsChannelData(String withTeamsChannelId,
                            String withTeamsTeamId,
                            ChannelInfo withChannel,
                            String withEventType,
                            TeamInfo withTeam,
                            NotificationInfo withNotification,
                            TenantInfo withTenant) {
        this.teamsChannelId = withTeamsChannelId;
        this.teamsTeamId = withTeamsTeamId;
        this.channel = withChannel;
        this.eventType = withEventType;
        this.team = withTeam;
        this.notification = withNotification;
        this.tenant = withTenant;
    }

    /**
     * A new instance of TeamChannelData.
     */
    public TeamsChannelData() {
    }

}
