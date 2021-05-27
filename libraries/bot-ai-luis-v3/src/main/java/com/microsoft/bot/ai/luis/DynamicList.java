// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request Body element to use when passing Dynamic lists to the Luis Service
 * call. Defines an extension for a list entity.
 *
 */
public class DynamicList {

    /**
     * Initializes a new instance of the DynamicList class.
     */
    public DynamicList() {
    }

    /**
     * Initializes a new instance of the DynamicList class.
     *
     * @param entity       Entity field.
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
     * Gets the name of the list entity to extend.
     *
     * @return The name of the list entity to extend.
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Sets the name of the list entity to extend.
     *
     * @param entity The name of the list entity to extend.
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     * Gets the lists to append on the extended list entity.
     *
     * @return The lists to append on the extended list entity.
     */
    public List<ListElement> getList() {
        return list;
    }

    /**
     * Sets the lists to append on the extended list entity.
     *
     * @param list The lists to append on the extended list entity.
     */
    public void setList(List<ListElement> list) {
        this.list = list;
    }

    /**
     * Validate the object.
     *
     * @throws IllegalArgumentException on null or invalid values.
     */
    public void validate() throws IllegalArgumentException {
        // Required: ListEntityName, RequestLists
        if (entity == null || list == null) {
            throw new IllegalArgumentException("DynamicList requires listEntityName and requestsLists to be defined.");
        }

        for (ListElement e : list) {
            e.validate();
        }
    }
}
