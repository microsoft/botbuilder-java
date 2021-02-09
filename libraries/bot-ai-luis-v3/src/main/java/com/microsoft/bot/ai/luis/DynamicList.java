// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DynamicList {

    /**
     * Initializes a new instance of the DynamicList class.
     */
    public DynamicList() {
    }

    /**
     * Initializes a new instance of the DynamicList class.
     */
    public DynamicList(String entity, List<ListElement> requestLists) {
        this.entity = entity;
        this.list = requestLists;
    }

    @JsonProperty(value = "listEntityName")
    private String entity;

    @JsonProperty(value = "requestLists")
    private List<ListElement> list;

    /**
     * Gets the entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Sets the entity name
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     * Gets the List
     */
    public List<ListElement> getList() {
        return list;
    }

    /**
     * Sets the List
     */
    public void setList(List<ListElement> list) {
        this.list = list;
    }

    /**
     * Validate the object
     * @throws IllegalArgumentException on null or invalid values
     */
    public void validate() throws IllegalArgumentException {
        // Required: ListEntityName, RequestLists
        if (entity == null || list == null) {
            throw new IllegalArgumentException("ExternalEntity requires an EntityName and EntityLength > 0");
        }

        for (ListElement e: list)
        {
            e.validate();
        }
    }
}
