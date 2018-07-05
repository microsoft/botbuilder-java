package com.microsoft.bot.schema;



import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.schema.models.Entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



public class EntityImpl extends Entity {
    private ObjectMapper objectMapper = new ObjectMapper();

    /// <summary>
    /// Initializes a new instance of the Entity class.
    /// </summary>
    public EntityImpl() {
        CustomInit();
    }


    /// <summary>
    /// Initializes a new instance of the Entity class.
    /// </summary>
    /// <param name="type">Entity Type (typically from schema.org
    /// types)</param>
    public EntityImpl(String type) {
        this.type = type;
        CustomInit();
    }

    /// <summary>
    /// An initialization method that performs custom operations like setting defaults
    /// </summary>
    void CustomInit() {
    }
    /// <summary>
    /// Gets or sets entity Type (typically from schema.org types)
    /// </summary>
    public String type;


    /**
     * @return
     */
    private HashMap<String, JsonNode> properties = new HashMap<String, JsonNode>();

    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {

        return this.properties;

    }


    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }


    /// <summary>

    /// Retrieve internal payload.

    /// </summary>

    /// <typeparam name="T"></typeparam>

    /// <returns></returns>

    public <T> T GetAs(Class<T> type)  {

        // Serialize
        String tempJson;
        try {
            tempJson = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        // Deserialize
        T newObj = null;
        try {
            newObj = (T) objectMapper.readValue(tempJson, type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return newObj;


    }


    /// <summary>
    /// Set internal payload.
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="obj"></param>

    public <T> boolean SetAs(T obj) {
        // Serialize
        String tempJson;
        try {
            tempJson = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

        EntityImpl tempEntity;
        try {
            tempEntity = objectMapper.readValue(tempJson, EntityImpl.class);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        for (Map.Entry<String, JsonNode> entry : tempEntity.properties.entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }
        this.type = obj.getClass().getTypeName();

        return true;

    }

};

