// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request Body element to use when passing Dynamic lists to the Luis Service call.
 *
 */
public class DynamicList {

    /**
     * Initializes a new instance of the DynamicList class.
     * @param entity Entity field.
     * @param requestLists List Elements to use when querying Luis Service.
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
     * Gets the entity.
     * @return Entity name.
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Sets the entity name.
     * @param entity entity name.
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     * Gets the List.
     * @return Element list of the Dynamic List.
     */
    public List<ListElement> getList() {
        return list;
    }

    /**
     * Sets the List.
     * @param list Element list of the Dynamic List.
     */
    public void setList(List<ListElement> list) {
        this.list = list;
    }

    /**
     * Validate the object.
     * @throws IllegalArgumentException on null or invalid values.
     */
    public void validate() throws IllegalArgumentException {
        // Required: ListEntityName, RequestLists
        if (entity == null || list == null) {
            throw new IllegalArgumentException("ExternalEntity requires an EntityName and EntityLength > 0");
        }

        for (ListElement e: list) {
            e.validate();
        }
    }
}
