/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 */

package com.microsoft.bot.schema.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Channel account information for a conversation.
 */
public class ConversationAccount {
    /**
     * Indicates whether the conversation contains more than two participants
     * at the time the activity was generated.
     */
    @JsonProperty(value = "isGroup")
    private Boolean isGroup;

    /**
     * Indicates the type of the conversation in channels that distinguish
     * between conversation types.
     */
    @JsonProperty(value = "conversationType")
    private String conversationType;

    /**
     * Channel id for the user or bot on this channel (Example: joe@smith.com,
     * or @joesmith or 123456).
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * Display friendly name.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Role of the entity behind the account (Example: User, Bot, etc.).
     * Possible values include: 'user', 'bot'.
     */
    @JsonProperty(value = "role")
    private RoleTypes role;

    /**
     * Get the isGroup value.
     *
     * @return the isGroup value
     */
    public Boolean isGroup() {
        return this.isGroup;
    }

    /**
     * Set the isGroup value.
     *
     * @param isGroup the isGroup value to set
     * @return the ConversationAccount object itself.
     */
    public ConversationAccount withIsGroup(Boolean isGroup) {
        this.isGroup = isGroup;
        return this;
    }

    /**
     * Get the conversationType value.
     *
     * @return the conversationType value
     */
    public String conversationType() {
        return this.conversationType;
    }

    /**
     * Set the conversationType value.
     *
     * @param conversationType the conversationType value to set
     * @return the ConversationAccount object itself.
     */
    public ConversationAccount withConversationType(String conversationType) {
        this.conversationType = conversationType;
        return this;
    }

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the ConversationAccount object itself.
     */
    public ConversationAccount withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the ConversationAccount object itself.
     */
    public ConversationAccount withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the role value.
     *
     * @return the role value
     */
    public RoleTypes role() {
        return this.role;
    }

    /**
     * Set the role value.
     *
     * @param role the role value to set
     * @return the ConversationAccount object itself.
     */
    public ConversationAccount withRole(RoleTypes role) {
        this.role = role;
        return this;
    }
    /**
     * Holds the overflow properties that aren't first class
     * properties in the object.  This allows extensibility
     * while maintaining the object.
     *
     */
    private HashMap<String, JsonNode> properties = new HashMap<String, JsonNode>();

    /**
     * Overflow properties.
     * Properties that are not modelled as first class properties in the object are accessible here.
     * Note: A property value can be be nested.
     *
     * @return A Key-Value map of the properties
     */
    @JsonAnyGetter
    public Map<String, JsonNode> properties() {
        return this.properties;
    }

    /**
     * Set overflow properties.
     *
     * @param key Key for the property
     * @param value JsonNode of value (can be nested)
     *
     */

    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }


}
