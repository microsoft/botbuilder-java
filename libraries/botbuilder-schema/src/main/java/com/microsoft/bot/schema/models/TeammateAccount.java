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
 * Teammate account information needed to fetch team members.
 */
public class TeammateAccount {
    /**
     * Channel id for the user on this channel (Example: joe@smith.com,
     * or @joesmith or 123456).
     */
    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "objectId")
    private String objectId;

    @JsonProperty(value = "givenName")
    private String givenName;

    @JsonProperty(value = "surname")
    private String surname;

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "userPrincipalName")
    private String userPrincipalName;

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
     * @return the TeammateAccount object itself.
     */
    public TeammateAccount withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the objectId value.
     *
     * @return the objectId value
     */
    public String objectId() {
        return this.objectId;
    }

    /**
     * Set the objectId value.
     *
     * @param objectId the name value to set
     * @return the TeammateAccount object itself.
     */
    public TeammateAccount withObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    /**
     * Get the givenName value.
     *
     * @return the givenName value
     */
    public String givenName() {
        return this.givenName;
    }

    /**
     * Set the givenName value.
     *
     * @param givenName the name value to set
     * @return the TeammateAccount object itself.
     */
    public TeammateAccount withGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    /**
     * Get the surname value.
     *
     * @return the surname value
     */
    public String surname() {
        return this.givenName;
    }

    /**
     * Set the surname value.
     *
     * @param surname the name value to set
     * @return the TeammateAccount object itself.
     */
    public TeammateAccount withSurname(String surname) {
        this.surname = surname;
        return this;
    }

    /**
     * Get the email value.
     *
     * @return the email value
     */
    public String email() {
        return this.email;
    }

    /**
     * Set the email value.
     *
     * @param email the name value to set
     * @return the TeammateAccount object itself.
     */
    public TeammateAccount withEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Get the userPrincipalName value.
     *
     * @return the userPrincipalName value
     */
    public String userPrincipalName() {
        return this.userPrincipalName;
    }

    /**
     * Set the email value.
     *
     * @param userPrincipalName the name value to set
     * @return the TeammateAccount object itself.
     */
    public TeammateAccount withUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
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
