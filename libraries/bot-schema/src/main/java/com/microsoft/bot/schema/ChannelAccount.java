// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Channel account information needed to route a message.
 */
public class ChannelAccount {
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    @JsonProperty(value = "name")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonProperty(value = "aadObjectId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String aadObjectId;

    @JsonProperty(value = "role")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private RoleTypes role;

    /**
     * Holds the overflow properties that aren't first class properties in the
     * object. This allows extensibility while maintaining the object.
     */
    private HashMap<String, JsonNode> properties = new HashMap<>();

    /**
     * Perform a deep copy of a ChannelAccount.
     *
     * @param channelAccount The ChannelAccount to copy.
     * @return A cloned copy of the ChannelAccount.
     */
    public static ChannelAccount clone(ChannelAccount channelAccount) {
        if (channelAccount == null) {
            return null;
        }

        ChannelAccount cloned = new ChannelAccount();
        cloned.setId(channelAccount.getId());
        cloned.setRole(channelAccount.getRole());
        cloned.setName(channelAccount.getName());
        cloned.setAadObjectId(channelAccount.getAadObjectId());

        for (String key : channelAccount.getProperties().keySet()) {
            cloned.setProperties(key, channelAccount.getProperties().get(key));
        }

        return cloned;
    }

    /**
     * Performs a deep copy of a List of ChannelAccounts.
     *
     * @param channelAccounts The List to clone.
     * @return A cloned List of ChannelAccounts.
     */
    public static List<ChannelAccount> cloneList(List<ChannelAccount> channelAccounts) {
        if (channelAccounts == null) {
            return null;
        }

        return channelAccounts.stream()
            .map(channelAccount -> ChannelAccount.clone(channelAccount))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Initializes a new instance of the ChannelAccount class.
     */
    public ChannelAccount() {

    }

    /**
     * Initializes a new instance of the ChannelAccount class.
     *
     * @param withId Channel id for the user or bot on this channel (Example:
     *               joe@smith.com, or @joesmith or 123456).
     */
    public ChannelAccount(String withId) {
        this(withId, null, null, null);
    }

    /**
     * Initializes a new instance of the ChannelAccount class.
     *
     * @param withId   Channel id for the user or bot on this channel (Example:
     *                 joe@smith.com, or @joesmith or 123456).
     * @param withName Display friendly name.
     */
    public ChannelAccount(String withId, String withName) {
        this(withId, withName, null, null);
    }

    /**
     * Initializes a new instance of the ChannelAccount class.
     *
     * @param withId   Channel id for the user or bot on this channel (Example:
     *                 joe@smith.com, or @joesmith or 123456).
     * @param withName Display friendly name.
     * @param withRole Role of the entity behind the account (Example User, Bot,
     *                 etc.). Possible values include: 'user', 'bot'
     */
    public ChannelAccount(String withId, String withName, RoleTypes withRole) {
        this(withId, withName, withRole, null);
    }

    /**
     * Initializes a new instance of the ChannelAccount class.
     *
     * @param withId          Channel id for the user or bot on this channel
     *                        (Example: joe@smith.com, or @joesmith or 123456).
     * @param withName        Display friendly name.
     * @param withRole        Role of the entity behind the account (Example User,
     *                        Bot, etc.). Possible values include: 'user', 'bot'
     * @param withAadObjectId This account's object ID within Azure Active Directory
     *                        (AAD).
     */
    public ChannelAccount(
        String withId,
        String withName,
        RoleTypes withRole,
        String withAadObjectId
    ) {
        this.id = withId;
        this.name = withName;
        this.role = withRole;
        this.aadObjectId = withAadObjectId;
    }

    /**
     * Channel id for the user or bot on this channel (Example: joe@smith.com,
     * or @joesmith or 123456).
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Channel id for the user or bot on this channel (Example: joe@smith.com,
     * or @joesmith or 123456).
     *
     * @param withId the id value to set.
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Display friendly name.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Display friendly name.
     *
     * @param withName the name value to set.
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * Role of the entity behind the account (Example: User, Bot, etc.).
     *
     * @return the role value.
     */
    public RoleTypes getRole() {
        return this.role;
    }

    /**
     * Role of the entity behind the account (Example: User, Bot, etc.).
     *
     * @param withRole the role value to set.
     */
    public void setRole(RoleTypes withRole) {
        this.role = withRole;
    }

    /**
     * Overflow properties. Properties that are not modelled as first class
     * properties in the object are accessible here. Note: A property value can be
     * be nested.
     *
     * @return A Key-Value map of the properties
     */
    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    /**
     * Set overflow properties.
     *
     * @param key   Key for the property
     * @param value JsonNode of value (can be nested)
     */
    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }

    /**
     * This account's object ID within Azure Active Directory (AAD).
     *
     * @return The aadObjectId value.
     */
    public String getAadObjectId() {
        return this.aadObjectId;
    }

    /**
     * This account's object ID within Azure Active Directory (AAD).
     *
     * @param withAadObjectId The aadObjectId value to set.
     */
    public void setAadObjectId(String withAadObjectId) {
        this.aadObjectId = withAadObjectId;
    }
}
