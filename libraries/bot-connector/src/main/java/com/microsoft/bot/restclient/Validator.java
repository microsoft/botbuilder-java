// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Validates user provided parameters are not null if they are required.
 */
public final class Validator {
    /**
     * Hidden constructor for utility class.
     */
    private Validator() { }

    /**
     * Validates a user provided required parameter to be not null.
     * An {@link IllegalArgumentException} is thrown if a property fails the validation.
     *
     * @param parameter the parameter to validate
     * @throws IllegalArgumentException thrown when the Validator determines the argument is invalid
     */
    public static void validate(Object parameter) {
        // Validation of top level payload is done outside
        if (parameter == null) {
            return;
        }

        Class<?> parameterType = parameter.getClass();
        TypeToken<?> parameterToken = TypeToken.of(parameterType);
        if (Primitives.isWrapperType(parameterType)) {
            parameterToken = parameterToken.unwrap();
        }
        if (parameterToken.isPrimitive()
                || parameterType.isEnum()
                || parameterType == Class.class
                || parameterToken.isSupertypeOf(OffsetDateTime.class)
                || parameterToken.isSupertypeOf(ZonedDateTime.class)
                || parameterToken.isSupertypeOf(String.class)
                || parameterToken.isSupertypeOf(Period.class)) {
            return;
        }

        Annotation skipParentAnnotation = parameterType.getAnnotation(SkipParentValidation.class);

        if (skipParentAnnotation == null) {
            for (Class<?> c : parameterToken.getTypes().classes().rawTypes()) {
                validateClass(c, parameter);
            }
        } else {
            validateClass(parameterType, parameter);
        }
    }

    private static void validateClass(Class<?> c, Object parameter) {
        // Ignore checks for Object type.
        if (c.isAssignableFrom(Object.class)) {
            return;
        }
        for (Field field : c.getDeclaredFields()) {
            field.setAccessible(true);
            int mod = field.getModifiers();
            // Skip static fields since we don't have any, skip final fields since users can't modify them
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                continue;
            }
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            // Skip read-only properties (WRITE_ONLY)
            if (annotation != null && annotation.access().equals(JsonProperty.Access.WRITE_ONLY)) {
                continue;
            }
            Object property;
            try {
                property = field.get(parameter);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            if (property == null) {
                if (annotation != null && annotation.required()) {
                    throw new IllegalArgumentException(field.getName() + " is required and cannot be null.");
                }
            } else {
                try {
                    Class<?> propertyType = property.getClass();
                    if (TypeToken.of(List.class).isSupertypeOf(propertyType)) {
                        List<?> items = (List<?>) property;
                        for (Object item : items) {
                            Validator.validate(item);
                        }
                    }
                    else if (TypeToken.of(Map.class).isSupertypeOf(propertyType)) {
                        Map<?, ?> entries = (Map<?, ?>) property;
                        for (Map.Entry<?, ?> entry : entries.entrySet()) {
                            Validator.validate(entry.getKey());
                            Validator.validate(entry.getValue());
                        }
                    }
                    else if (parameter.getClass() != propertyType) {
                        Validator.validate(property);
                    }
                } catch (IllegalArgumentException ex) {
                    if (ex.getCause() == null) {
                        // Build property chain
                        throw new IllegalArgumentException(field.getName() + "." + ex.getMessage());
                    } else {
                        throw ex;
                    }
                }
            }
        }
    }
}
