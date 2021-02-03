package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.applicationinsights.core.dependencies.google.gson.JsonSyntaxException;

import java.util.List;

public class DynamicList {
    /// <summary>
    /// Initializes a new instance of the <see cref="DynamicList"/> class.
    /// </summary>
    public DynamicList()
    {
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="DynamicList"/> class.
    /// </summary>
    /// <param name="entity">The name of the list entity to extend.</param>
    /// <param name="requestLists">The lists to append on the extended list entity.</param>
    public DynamicList(String entity, List<ListElement> requestLists)
    {
        entity = entity;
        list = requestLists;
    }

    /// <summary>
    /// Gets or sets the name of the list entity to extend.
    /// </summary>
    /// <value>
    /// The name of the list entity to extend.
    /// </value>
    @JsonProperty(value = "listEntityName")
    public String entity;

    /// <summary>
    /// Gets or sets the lists to append on the extended list entity.
    /// </summary>
    /// <value>
    /// The lists to append on the extended list entity.
    /// </value>
    @JsonProperty(value = "requestLists")
    public List<ListElement> list;

    /// <summary>
    /// Validate the object.
    /// </summary>
    /// <exception cref="Microsoft.Rest.ValidationException">
    /// Thrown if validation fails.
    /// </exception>
    public void validate()
    {
        // Required: ListEntityName, RequestLists
        if (entity == null || list == null) {
            throw new JsonSyntaxException("ExternalEntity requires an EntityName and EntityLength > 0");
        }

        for (ListElement e: list)
        {
            e.validate();
        }
    }
}
