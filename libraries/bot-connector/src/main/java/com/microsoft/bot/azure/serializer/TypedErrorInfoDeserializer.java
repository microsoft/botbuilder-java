/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.azure.serializer;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.azure.PolicyViolation;
import com.microsoft.bot.azure.TypedErrorInfo;

/**
 * Custom serializer for serializing {@link TypedErrorInfo} objects.
 */
public class TypedErrorInfoDeserializer extends JsonDeserializer<TypedErrorInfo> {
    private static final String TYPE_FIELD_NAME = "type";
    private static final String INFO_FIELD_NAME = "info";

    @Override
    public TypedErrorInfo deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode errorInfoNode = p.readValueAsTree();
        if (errorInfoNode == null) {
            return null;
        }

        JsonNode typeNode = errorInfoNode.get(TYPE_FIELD_NAME);
        JsonNode infoNode = errorInfoNode.get(INFO_FIELD_NAME);
        if (typeNode == null || infoNode == null) {
            Iterator<String> fieldNames = errorInfoNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                if (typeNode == null && TYPE_FIELD_NAME.equalsIgnoreCase(fieldName)) {
                    typeNode = errorInfoNode.get(fieldName);

                }

                if (infoNode == null && INFO_FIELD_NAME.equalsIgnoreCase(fieldName)) {
                    infoNode = errorInfoNode.get(fieldName);

                }
            }
        }

        if (typeNode == null || infoNode == null || !(infoNode instanceof ObjectNode)) {
            return null;
        }

        // deserialize to any strongly typed error defined
        switch (typeNode.asText()) {
            case "PolicyViolation":
                return new PolicyViolation(typeNode.asText(), (ObjectNode) infoNode);

            default:
                return new TypedErrorInfo(typeNode.asText(), (ObjectNode) infoNode);
        }
    }
}
