// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        objectMapper.findAndRegisterModules();
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
}
