package com.microsoft.bot.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.schema.models.Entity;

import java.io.IOException;

public class EntityImpl extends Entity
{
    private static final ObjectMapper mapper = new ObjectMapper();
    /**
     * Extension data for overflow of properties
     */
    private ObjectNode _properties = JsonNodeFactory.instance.objectNode();
    public ObjectNode getProperties() {
            return _properties;
    }
    public void setProperties(ObjectNode properties) {
        _properties = properties;
    }


    /**
     * Set internal payload.
     * @param T 
     * @param obj 
     */
    private String objectData;

    public <T> void SetAs(T obj) throws JsonProcessingException {
        this.withType(obj.getClass().getTypeName());
        this.objectData = mapper.writeValueAsString(obj);

    }

    /**
     * Retrieve internal payload.
     * @param T 
     * @return 
     */
    public <T> T GetAs(Class<T> classType) throws IOException {
        String classTypeName= classType.getName();

        if (classType.getName().equals(this.type())) {
            return mapper.readValue(this.objectData, classType);
        }
        // Type mismatch - throw?
        return null;

    }
}
