// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Serialization helpers.
 */
public final class Serialization {
    private Serialization() {

    }

    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.findAndRegisterModules();

        // NOTE: Undetermined if we should accommodate non-public fields.  The normal
        // Bean pattern, and Jackson default, is for public fields or accessors.
        //objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    /**
     * Deserialize a value.
     *
     * @param obj       The object to deserialize.
     * @param classType The class type to convert to.
     * @param <T>       The type of the return value.
     * @return A deserialized POJO, or null for error.
     */
    public static <T> T getAs(Object obj, Class<T> classType) {
        try {
            return safeGetAs(obj, classType);
        } catch (JsonProcessingException jpe) {
            return null;
        }
    }

    /**
     * Deserialize a value.
     *
     * @param obj       The object to deserialize.
     * @param classType The class type to convert to.
     * @param <T>       The type of the return value.
     * @return A deserialized POJO, or null.
     * @throws JsonProcessingException The JSON processing exception.
     */
    public static <T> T safeGetAs(Object obj, Class<T> classType) throws JsonProcessingException {
        if (obj == null) {
            return null;
        }

        JsonNode node = objectMapper.valueToTree(obj);
        return objectMapper.treeToValue(node, classType);
    }


    /**
     * @param obj The Object to clone
     * @return Object The cloned Object
     */
    public static Object clone(Object obj) {
        if (obj == null) {
            return null;
        }

        JsonNode node = objectMapper.valueToTree(obj);
        try {
            return objectMapper.treeToValue(node, obj.getClass());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param <T> The Type of the Class
     * @param src The source JsonNode
     * @param cls The Class to Map
     * @return the result of the mapping
     */
    public static <T> T treeToValue(JsonNode src, Class<T> cls) {
        try {
            return objectMapper.treeToValue(src, cls);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Convert Object to JsonNode.
     * @param obj The object to convert.
     * @return The JsonNode for the object tree.
     */
    public static JsonNode objectToTree(Object obj) {
        return objectMapper.valueToTree(obj);
    }

    /**
     * Deserializes an object to a type as a future to ease CompletableFuture
     * chaining.
     *
     * @param obj       The object to deserialize.
     * @param classType Class information to convert to.
     * @param <R>       The return Type.
     * @return A CompletableFuture containing the value or exception for an error.
     */
    public static <R> CompletableFuture<R> futureGetAs(Object obj, Class<R> classType) {
        CompletableFuture<R> futureResult = new CompletableFuture<>();

        try {
            futureResult.complete(Serialization.safeGetAs(obj, classType));
        } catch (JsonProcessingException jpe) {
            futureResult
                .completeExceptionally(new CompletionException("Unable to deserialize", jpe));
        }

        return futureResult;
    }

    /**
     * Converts an input object to another type.
     *
     * @param source  The object to convert.
     * @param toClass The class to convert to.
     * @param <T>     Type of return value.
     * @return The converted object, or null.
     */
    public static <T> T convert(Object source, Class<T> toClass) {
        return getAs(source, toClass);
    }

    /**
     * Convert an object to a JSON string.
     *
     * @param source The object to convert.
     * @return The JSON string value.
     * @throws JsonProcessingException Error converting to JSON
     */
    public static String toString(Object source) throws JsonProcessingException {
        return objectMapper.writeValueAsString(source);
    }

    /**
     * Convert an object to a JSON string.
     *
     * @param source The object to convert.
     * @return The JSON string value.
     */
    public static String toStringSilent(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Parses a JSON document.
     *
     * @param json The JSON to parse.
     * @return A JsonNode containing the node tree.
     * @throws IOException Error parsing json.
     */
    public static JsonNode jsonToTree(String json) throws IOException {
        return objectMapper.readTree(json);
    }


    /**
     * @param s The string to convert to a JsonNode
     * @return JsonNode
     */
    public static JsonNode asNode(String s) {
        return objectMapper.getNodeFactory().textNode(s);
    }


    /**
     * @param i The int to convert to a JsonNode
     * @return JsonNode
     */
    public static JsonNode asNode(int i) {
        return objectMapper.getNodeFactory().numberNode(i);
    }


    /**
     * @param l The long to convert to a JsonNode
     * @return JsonNode
     */
    public static JsonNode asNode(long l) {
        return objectMapper.getNodeFactory().numberNode(l);
    }


    /**
     * @param f The float to convert to a JsonNode
     * @return JsonNode
     */
    public static JsonNode asNode(float f) {
        return objectMapper.getNodeFactory().numberNode(f);
    }


    /**
     * @param d The double to convert to a JsonNode
     * @return JsonNode
     */
    public static JsonNode asNode(double d) {
        return objectMapper.getNodeFactory().numberNode(d);
    }


    /**
     * @param s The short to convert to a JsonNode
     * @return JsonNode
     */
    public static JsonNode asNode(short s) {
        return objectMapper.getNodeFactory().numberNode(s);
    }


    /**
     * @param b The boolean to convert to a JsonNode
     * @return JsonNode
     */
    public static JsonNode asNode(boolean b) {
        return objectMapper.getNodeFactory().booleanNode(b);
    }


    /**
     * @param b The byte to convert to a JsonNode
     * @return JsonNode
     */
    public static JsonNode asNode(byte b) {
        return objectMapper.getNodeFactory().numberNode(b);
    }

    /**
     * Creates an ObjectNode.
     * @return ObjectNode.
     */
    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    /**
     * Creates an ArrayNode.
     * @return ArrayNode.
     */
    public static ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }
}

