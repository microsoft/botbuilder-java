// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient.serializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Custom serializer for deserializing complex types with wrapped properties.
 * For example, a property with annotation @JsonProperty(value = "properties.name")
 * will be mapped to a top level "name" property in the POJO model.
 */
public final class FlatteningDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {
    /**
     * The default mapperAdapter for the current type.
     */
    private final JsonDeserializer<?> defaultDeserializer;

    /**
     * The object mapper for default deserializations.
     */
    private final ObjectMapper mapper;

    /**
     * Creates an instance of FlatteningDeserializer.
     * @param vc handled type
     * @param defaultDeserializer the default JSON mapperAdapter
     * @param mapper the object mapper for default deserializations
     */
    protected FlatteningDeserializer(Class<?> vc, JsonDeserializer<?> defaultDeserializer, ObjectMapper mapper) {
        super(vc);
        this.defaultDeserializer = defaultDeserializer;
        this.mapper = mapper;
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @param mapper the object mapper for default deserializations
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule(final ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (BeanDeserializer.class.isAssignableFrom(deserializer.getClass())) {
                    // Apply flattening deserializer on all POJO types.
                    return new FlatteningDeserializer(beanDesc.getBeanClass(), deserializer, mapper);
                } else {
                    return deserializer;
                }
            }
        });
        return module;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext cxt, TypeDeserializer tDeserializer) throws IOException {
        // This method will be called by Jackson for each "Json object with TypeId" in the input wire stream
        // it is trying to deserialize.
        // The below variable 'currentJsonNode' will hold the JsonNode corresponds to current
        // Json object this method is called to handle.
        //
        JsonNode currentJsonNode = mapper.readTree(jp);
        final Class<?> tClass = this.defaultDeserializer.handledType();
        for (Class<?> c : TypeToken.of(tClass).getTypes().classes().rawTypes()) {
            if (!c.isAssignableFrom(Object.class)) {
                final JsonTypeInfo typeInfo = c.getAnnotation(JsonTypeInfo.class);
                if (typeInfo != null) {
                    String typeId = typeInfo.property();
                    if (containsDot(typeId)) {
                        final String typeIdOnWire = unescapeEscapedDots(typeId);
                        JsonNode typeIdValue = ((ObjectNode) currentJsonNode).remove(typeIdOnWire);
                        if (typeIdValue != null) {
                            ((ObjectNode) currentJsonNode).set(typeId, typeIdValue);
                        }
                    }
                }
            }
        }
        return tDeserializer.deserializeTypedFromAny(newJsonParserForNode(currentJsonNode), cxt);
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext cxt) throws IOException {
        // This method will be called by Jackson for each "Json object" in the input wire stream
        // it is trying to deserialize.
        // The below variable 'currentJsonNode' will hold the JsonNode corresponds to current
        // Json object this method is called to handle.
        //
        JsonNode currentJsonNode = mapper.readTree(jp);
        if (currentJsonNode.isNull()) {
            currentJsonNode = mapper.getNodeFactory().objectNode();
        }
        final Class<?> tClass = this.defaultDeserializer.handledType();
        for (Class<?> c : TypeToken.of(tClass).getTypes().classes().rawTypes()) {
            if (!c.isAssignableFrom(Object.class)) {
                for (Field classField : c.getDeclaredFields()) {
                    handleFlatteningForField(classField, currentJsonNode);
                }
            }
        }
        return this.defaultDeserializer.deserialize(newJsonParserForNode(currentJsonNode), cxt);
    }

    @Override
    public void resolve(DeserializationContext cxt) throws JsonMappingException {
        ((ResolvableDeserializer) this.defaultDeserializer).resolve(cxt);
    }

    /**
     * Given a field of a POJO class and JsonNode corresponds to the same POJO class,
     * check field's {@link JsonProperty} has flattening dots in it if so
     * flatten the nested child JsonNode corresponds to the field in the given JsonNode.
     *
     * @param classField the field in a POJO class
     * @param jsonNode the json node corresponds to POJO class that field belongs to
     */
    @SuppressWarnings("unchecked")
    private static void handleFlatteningForField(Field classField, JsonNode jsonNode) {
        final JsonProperty jsonProperty = classField.getAnnotation(JsonProperty.class);
        if (jsonProperty != null) {
            final String jsonPropValue = jsonProperty.value();
            if (containsFlatteningDots(jsonPropValue)) {
                JsonNode childJsonNode = findNestedNode(jsonNode, jsonPropValue);
                ((ObjectNode) jsonNode).set(jsonPropValue, childJsonNode);
            }
        }
    }

    /**
     * Given a json node, find a nested node using given composed key.
     *
     * @param jsonNode the parent json node
     * @param composedKey a key combines multiple keys using flattening dots.
     *                    Flattening dots are dot character '.' those are not preceded by slash '\'
     *                    Each flattening dot represents a level with following key as field key in that level
     * @return nested json node located using given composed key
     */
    private static JsonNode findNestedNode(JsonNode jsonNode, String composedKey) {
        String[] jsonNodeKeys = splitKeyByFlatteningDots(composedKey);
        for (String jsonNodeKey : jsonNodeKeys) {
            jsonNode = jsonNode.get(unescapeEscapedDots(jsonNodeKey));
            if (jsonNode == null) {
                return null;
            }
        }
        return jsonNode;
    }

    /**
     * Checks whether the given key has flattening dots in it.
     * Flattening dots are dot character '.' those are not preceded by slash '\'
     *
     * @param key the key
     * @return true if the key has flattening dots, false otherwise.
     */
    private static boolean containsFlatteningDots(String key) {
        return key.matches(".+[^\\\\]\\..+");
    }

    /**
     * Split the key by flattening dots.
     * Flattening dots are dot character '.' those are not preceded by slash '\'
     *
     * @param key the key to split
     * @return the array of sub keys
     */
    private static String[] splitKeyByFlatteningDots(String key) {
        return key.split("((?<!\\\\))\\.");
    }

    /**
     * Unescape the escaped dots in the key.
     * Escaped dots are non-flattening dots those are preceded by slash '\'
     *
     * @param key the key unescape
     * @return unescaped key
     */
    private static String unescapeEscapedDots(String key) {
        // Replace '\.' with '.'
        return key.replace("\\.", ".");
    }

    /**
     * Checks the given string contains 0 or more dots.
     *
     * @param str the string to check
     * @return true if at least one dot found
     */
    private static boolean containsDot(String str) {
       return str != null && str.length() > 0 && str.contains(".");
    }

    /**
     * Create a JsonParser for a given json node.
     * @param jsonNode the json node
     * @return the json parser
     * @throws IOException
     */
    private static JsonParser newJsonParserForNode(JsonNode jsonNode) throws IOException {
        JsonParser parser = new JsonFactory().createParser(jsonNode.toString());
        parser.nextToken();
        return parser;
    }
}
