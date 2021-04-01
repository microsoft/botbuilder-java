// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Conversation account represents the identity of the conversation within a
 * channel.
 */
public class ConversationAccount {
    /**
     * Indicates whether the conversation contains more than two participants at the
     * time the activity was generated. The default value is false.
     */
    @JsonProperty(value = "isGroup")
    private boolean isGroup = false;

    /**
     * Indicates the type of the conversation in channels that distinguish between
     * conversation types.
     */
    @JsonProperty(value = "conversationType")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String conversationType;

    /**
     * This conversation's tenant ID.
     */
    @JsonProperty(value = "tenantId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String tenantId;

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
     * Role of the entity behind the account (Example: User, Bot, etc.). Possible
     * values include: 'user', 'bot'.
     */
    @JsonProperty(value = "role")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private RoleTypes role;

    /**
     * Empty ConversationAccount.
     */
    public ConversationAccount() {

    }

    /**
     * Initializes a new instance of the ConversationAccount class.
     *
     * @param withId Channel id for the user or bot on this channel (Example:
     *               joe@smith.com, or @joesmith or 123456).
     */
    public ConversationAccount(String withId) {
        this(false, withId, null);
    }

    /**
     * Initializes a new instance of the ConversationAccount class.
     *
     * @param withIsGroup Indicates whether the conversation contains more than two
     *                    participants at the time the activity was.
     * @param withId      Channel id for the user or bot on this channel (Example:
     *                    joe@smith.com, or @joesmith or 123456).
     * @param withName    Display friendly name.
     */
    public ConversationAccount(boolean withIsGroup, String withId, String withName) {
        this(withIsGroup, null, withId, withName, null, null, null);
    }

    /**
     * Initializes a new instance of the ConversationAccount class.
     *
     * @param withIsGroup          Indicates whether the conversation contains more
     *                             than two participants at the time the activity
     *                             was.
     * @param withConversationType Indicates the type of the conversation in
     *                             channels that distinguish between conversation.
     * @param withId               Channel id for the user or bot on this channel
     *                             (Example: joe@smith.com, or @joesmith or 123456).
     * @param withName             Display friendly name.
     * @param withAadObjectId      This account's object ID within Azure Active
     *                             Directory (AAD).
     * @param withRole             Role of the entity behind the account (Example:
     *                             User, Bot, etc.). Possible values include:
     *                             'user', 'bot'.
     * @param withTenantId         This conversation's tenant ID.
     */
    public ConversationAccount(
        boolean withIsGroup,
        String withConversationType,
        String withId,
        String withName,
        String withAadObjectId,
        RoleTypes withRole,
        String withTenantId
    ) {
        this.isGroup = withIsGroup;
        this.conversationType = withConversationType;
        this.id = withId;
        this.name = withName;
        this.aadObjectId = withAadObjectId;
        this.role = withRole;
        this.tenantId = withTenantId;
    }

    /**
     * Get the {@link #isGroup} value.
     * 
     * @return the isGroup value
     */
    public boolean isGroup() {
        return this.isGroup;
    }

    /**
     * Set the {@link #isGroup} value.
     * 
     * @param withIsGroup the isGroup value to set
     */
    public void setIsGroup(boolean withIsGroup) {
        this.isGroup = withIsGroup;
    }

    /**
     * Get the {@link #conversationType} value.
     * 
     * @return the conversationType value
     */
    public String getConversationType() {
        return this.conversationType;
    }

    /**
     * Set the {@link #conversationType} value.
     * 
     * @param withConversationType the conversationType value to set
     */
    public void setConversationType(String withConversationType) {
        this.conversationType = withConversationType;
    }

    /**
     * Gets this conversation's {@link #tenantId}.
     * 
     * @return The tenantId value.
     */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * Sets this conversation's {@link #tenantId}.
     * 
     * @param withTenantId this conversation's tenant ID
     */
    public void setTenantId(String withTenantId) {
        this.tenantId = withTenantId;
    }

    /**
     * Get the {@link #id} value.
     * 
     * @return the id value
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the {@link #id} value.
     * 
     * @param withId the id value to set
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Get the {@link #name} value.
     * 
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the {@link #name} value.
     * 
     * @param withName the name value to set
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * Gets this account's {@link #aadObjectId} within Azure Active Directory (AAD).
     * 
     * @return The AAD object id.
     */
    public String getAadObjectId() {
        return this.aadObjectId;
    }

    /**
     * Sets this account's {@link #aadObjectId} within Azure Active Directory (AAD).
     * 
     * @param withAadObjectId the AAD ID to set
     */
    public void setAadObjectId(String withAadObjectId) {
        this.aadObjectId = withAadObjectId;
    }

    /**
     * Get the {@link #role} value.
     * 
     * @return the role value
     */
    public RoleTypes getRole() {
        return this.role;
    }

    /**
     * Set the {@link #role} value.
     * 
     * @param withRole the role value to set
     */
    public void setRole(RoleTypes withRole) {
        this.role = withRole;
    }

    /**
     * Holds the overflow properties that aren't first class properties in the
     * object. This allows extensibility while maintaining the object.
     */
    private HashMap<String, JsonNode> properties = new HashMap<>();

    /**
     * Performs a deep copy of a ConversationAccount.
     * 
     * @param conversationAccount The ConversationAccount to copy.
     * @return The cloned ConversationAccount.
     */
    public static ConversationAccount clone(ConversationAccount conversationAccount) {
        if (conversationAccount == null) {
            return null;
        }

        ConversationAccount cloned = new ConversationAccount();
        cloned.setId(conversationAccount.getId());
        cloned.setName(conversationAccount.getName());
        cloned.setIsGroup(conversationAccount.isGroup());
        cloned.setConversationType(conversationAccount.getConversationType());
        cloned.setAadObjectId(conversationAccount.getAadObjectId());
        cloned.setRole(conversationAccount.getRole());
        cloned.setAadObjectId(conversationAccount.getAadObjectId());
        for (String key : conversationAccount.getProperties().keySet()) {
            cloned.setProperties(key, conversationAccount.getProperties().get(key));
        }

        return cloned;
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
}
