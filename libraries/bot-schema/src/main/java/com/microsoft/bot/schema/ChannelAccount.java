/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

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
    /**
     * Channel id for the user or bot on this channel (Example: joe@smith.com,
     * or @joesmith or 123456).
     */
    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    /**
     * Display friendly name.
     */
    @JsonProperty(value = "name")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    /**
     * This account's object ID within Azure Active Directory (AAD).
     */
    @JsonProperty(value = "aadObjectId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String aadObjectId;

    /**
     * Role of the entity behind the account (Example: User, Bot, etc.).
     * Possible values include: 'user', 'bot'.
     */
    @JsonProperty(value = "role")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private RoleTypes role;

    /**
     * Holds the overflow properties that aren't first class
     * properties in the object.  This allows extensibility
     * while maintaining the object.
     */
    private HashMap<String, JsonNode> properties = new HashMap<String, JsonNode>();

    public static ChannelAccount clone(ChannelAccount channelAccount) {
        if (channelAccount == null) {
            return null;
        }

        return new ChannelAccount() {{
            setId(channelAccount.getId());
            setRole(channelAccount.getRole());
            setName(channelAccount.getName());
            setAadObjectId(channelAccount.getAadObjectId());

            for (String key : channelAccount.getProperties().keySet()) {
                this.setProperties(key, channelAccount.getProperties().get(key));
            }
        }};
    }

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
     * @param withId Channel id for the user or bot on this channel (Example: joe@smith.com, or @joesmith or 123456).
     */
    public ChannelAccount(String withId) {
        this(withId, null, null, null);
    }

    /**
     * Initializes a new instance of the ChannelAccount class.
     * @param withId Channel id for the user or bot on this channel (Example: joe@smith.com, or @joesmith or 123456).
     * @param withName Display friendly name.
     */
    public ChannelAccount(String withId, String withName) {
        this(withId, withName, null, null);
    }

    /**
     * Initializes a new instance of the ChannelAccount class.
     * @param withId Channel id for the user or bot on this channel (Example: joe@smith.com, or @joesmith or 123456).
     * @param withName Display friendly name.
     * @param withRole Role of the entity behind the account (Example User, Bot, etc.). Possible values
     *                 include: 'user', 'bot'
     */
    public ChannelAccount(String withId, String withName, RoleTypes withRole) {
        this(withId, withName, withRole, null);
    }

    /**
     * Initializes a new instance of the ChannelAccount class.
     * @param withId Channel id for the user or bot on this channel (Example: joe@smith.com, or @joesmith or 123456).
     * @param withName Display friendly name.
     * @param withRole Role of the entity behind the account (Example User, Bot, etc.). Possible values
     *                 include: 'user', 'bot'
     * @param withAadObjectId This account's object ID within Azure Active Directory (AAD).
     */
    public ChannelAccount(String withId, String withName, RoleTypes withRole, String withAadObjectId) {
        this.id = withId;
        this.name = withName;
        this.role = withRole;
        this.aadObjectId = withAadObjectId;
    }

    /**
     * Get the {@link #role} value.
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the {@link #id} value.
     * @param withId the id value to set.
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Get the {@link #name} value.
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the {@link #name} value.
     * @param withName the name value to set.
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * Get the {@link #role} value.
     * @return the role value.
     */
    public RoleTypes getRole() {
        return this.role;
    }

    /**
     * Set the {@link #role} value.
     * @param withRole the role value to set.
     */
    public void setRole(RoleTypes withRole) {
        this.role = withRole;
    }

    /**
     * Overflow properties.
     * Properties that are not modelled as first class properties in the object are accessible here.
     * Note: A property value can be be nested.
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
     * Gets the {@link #aadObjectId} value.
     * @return The aadObjectId value.
     */
    public String getAadObjectId() {
        return this.aadObjectId;
    }

    /**
     * Sets the {@link #aadObjectId} value.
     * @param withAadObjectId The aadObjectId value to set.
     */
    public void setAadObjectId(String withAadObjectId) {
        this.aadObjectId = withAadObjectId;
    }
}
