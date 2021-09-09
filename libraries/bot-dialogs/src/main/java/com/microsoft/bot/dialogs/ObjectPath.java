// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.TurnContextStateCollection;
import com.microsoft.bot.schema.Serialization;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper methods for working with dynamic json objects.
 */
public final class ObjectPath {
    private ObjectPath() { }

    /**
     * Does an Object have a subpath.
     * @param obj Object.
     * @param path path to evaluate.
     * @return true if the path is there.
     */
    public static boolean hasValue(Object obj, String path) {
        return tryGetPathValue(obj, path, Object.class) != null;
    }

    /**
     * Get the value for a path relative to an Object.
     * @param <T> type to return.
     * @param obj Object to start with.
     * @param path path to evaluate.
     * @param valueType Type of T
     * @return value or default(T).
     */
    @SuppressWarnings("checkstyle:InnerAssignment")
    public static <T> T getPathValue(Object obj, String path, Class<T> valueType) {
        T value;
        if ((value = tryGetPathValue(obj, path, valueType)) != null) {
            return value;
        }

        throw new IllegalArgumentException(path);
    }

    /**
     * Get the value for a path relative to an Object.
     * @param <T> type to return.
     * @param obj Object to start with.
     * @param path path to evaluate.
     * @param valueType type of T
     * @param defaultValue  default value to use if any part of the path is missing.
     * @return value or default(T).
     */
    @SuppressWarnings("checkstyle:InnerAssignment")
    public static <T> T getPathValue(Object obj, String path, Class<T> valueType, T defaultValue) {
        T value;
        if ((value = tryGetPathValue(obj, path, valueType)) != null) {
            return value;
        }

        return defaultValue;
    }

    /**
     * Get the value for a path relative to an Object.
     * @param <T> type to return.
     * @param obj Object to start with.
     * @param path path to evaluate.
     * @param valueType value for the path.
     * @return true if successful.
     */
    public static <T> T tryGetPathValue(Object obj, String path, Class<T> valueType) {
        if (obj == null || path == null) {
            return null;
        }

        if (path.length() == 0) {
            return mapValueTo(obj, valueType);
        }

        Segments segments = tryResolvePath(obj, path);
        if (segments == null) {
            return null;
        }

        Object result = resolveSegments(obj, segments);
        if (result == null) {
            return null;
        }

        // look to see if it's ExpressionProperty and bind it if it is
        // NOTE: this bit of duck typing keeps us from adding dependency between adaptiveExpressions and Dialogs.
        /*
        if (result.GetType().GetProperty("ExpressionText") != null)
        {
            var method = result.GetType().GetMethod("GetValue", new[] { typeof(Object) });
            if (method != null)
            {
                result = method.Invoke(result, new[] { obj });
            }
        }
        */

        return mapValueTo(result, valueType);
    }

    /**
     * Given an Object evaluate a path to set the value.
     * @param obj Object to start with.
     * @param path path to evaluate.
     * @param value value to store.
     */
    public static void setPathValue(Object obj, String path, Object value) {
        setPathValue(obj, path, value, true);
    }

    /**
     * Given an Object evaluate a path to set the value.
     * @param obj Object to start with.
     * @param path path to evaluate.
     * @param value value to store.
     * @param json if true, sets the value as primitive JSON Objects.
     */
    public static void setPathValue(Object obj, String path, Object value, boolean json) {
        Segments segments = tryResolvePath(obj, path);
        if (segments == null) {
            return;
        }

        Object current = obj;
        for (int i = 0; i < segments.size() - 1; i++) {
            SegmentType segment = segments.getSegment(i);
            Object next;
            if (segment.isInt) {
                if (((Segments) current).size() <= segment.intValue) {
                    // TODO make sure growBy is correct
                    // Expand array to index
                    int growBy = segment.intValue - ((Segments) current).size();
                    ((ArrayNode) current).add(growBy);
                }
                next = ((ArrayNode) current).get(segment.intValue);
            } else {
                next = getObjectProperty(current, segment.stringValue);
                if (next == null) {
                    // Create Object or array base on next segment
                    SegmentType nextSegment = new SegmentType(segments.get(i + 1));
                    if (nextSegment.stringValue != null) {
                        setObjectSegment(current, segment.stringValue, JsonNodeFactory.instance.objectNode());
                    } else {
                        setObjectSegment(current, segment.stringValue, JsonNodeFactory.instance.arrayNode());
                    }
                    next = getObjectProperty(current, segment.stringValue);
                }
            }

            current = next;
        }

        Object lastSegment = segments.last();
        setObjectSegment(current, lastSegment, value, json);
    }

    /**
     * Remove path from Object.
     * @param obj Object to change.
     * @param path Path to remove.
     */
    public static void removePathValue(Object obj, String path) {
        Segments segments = tryResolvePath(obj, path);
        if (segments == null) {
            return;
        }

        Object current = obj;
        for (int i = 0; i < segments.size() - 1; i++) {
            Object segment = segments.get(i);
            current = resolveSegment(current, segment);
            if (current == null) {
                return;
            }
        }

        if (current != null) {
            Object lastSegment = segments.last();
            if (lastSegment instanceof String) {
                // lastSegment is a field name
                if (current instanceof Map) {
                    ((Map<String, Object>) current).remove((String) lastSegment);
                } else {
                    ((ObjectNode) current).remove((String) lastSegment);
                }
            } else {
                // lastSegment is an index
                ((ArrayNode) current).set((int) lastSegment, null);
            }
        }
    }

    /**
     * Apply an action to all properties in an Object.
     * @param obj Object to map against.
     * @param action Action to take.
     */
    public static void forEachProperty(Object obj, BiConsumer<String, Object> action) {
        if (obj instanceof Map) {
            ((Map<String, Object>) obj).forEach(action);
        } else if (obj instanceof ObjectNode) {
            ObjectNode node = (ObjectNode) obj;
            Iterator<String> fields = node.fieldNames();

            while (fields.hasNext()) {
                String field = fields.next();
                action.accept(field, node.findValue(field));
            }
        }
    }

    /**
     * Get all properties in an Object.
     * @param obj Object to enumerate property names.
     * @return enumeration of property names on the Object if it is not a value type.
     */
    public static Collection<String> getProperties(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        } else if (obj instanceof Map) {
            return ((Map<String, Object>) obj).keySet();
        } else if (obj instanceof JsonNode) {
            List<String> fields = new ArrayList<>();
            ((JsonNode) obj).fieldNames().forEachRemaining(fields::add);
            return fields;
        } else {
            List<String> fields = new ArrayList<>();
            for (Field field : obj.getClass().getDeclaredFields()) {
                fields.add(field.getName());
            }

            return fields;
        }
    }

    /**
     * Detects if property exists on Object.
     * @param obj Object.
     * @param name name of the property.
     * @return true if found.
     */
    public static boolean containsProperty(Object obj, String name) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Map) {
            return ((Map<String, Object>) obj).containsKey(name);
        }

        if (obj instanceof JsonNode) {
            return ((JsonNode) obj).findValue(name) != null;
        }

        for (Field field : obj.getClass().getDeclaredFields()) {
            if (StringUtils.equalsIgnoreCase(field.getName(), name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clone an Object.
     * @param <T> Type to clone.
     * @param obj The Object.
     * @return The Object as Json.
     */
    public static <T> T clone(T obj) {
        return (T) Serialization.getAs(obj, obj.getClass());
    }

    /**
     * Equivalent to javascripts ObjectPath.Assign, creates a new Object from startObject
     * overlaying any non-null values from the overlay Object.
     * @param <T> The Object type.
     * @param startObject Intial Object.
     * @param overlayObject Overlay Object.
     * @return merged Object.
     */
    public static <T> T merge(Object startObject, Object overlayObject) {
        return (T) assign(startObject, overlayObject);
    }

    /**
     * Equivalent to javascripts ObjectPath.Assign, creates a new Object from startObject
     * overlaying any non-null values from the overlay Object.
     * @param <T> The Object type.
     * @param startObject Intial Object.
     * @param overlayObject Overlay Object.
     * @param type Type of T
     * @return merged Object.
     */
    public static <T> T merge(Object startObject, Object overlayObject, Class<T> type) {
        return (T) assign(startObject, overlayObject, type);
    }

    /**
     * Equivalent to javascripts ObjectPath.Assign, creates a new Object from startObject
     * overlaying any non-null values from the overlay Object.
     * @param <T> The target type.
     * @param startObject overlay Object of any type.
     * @param overlayObject overlay Object of any type.
     * @return merged Object.
     */
    public static <T> T assign(T startObject, Object overlayObject) {
        // FIXME this won't work for null startObject
        return (T) assign(startObject, overlayObject, startObject.getClass());
    }

    /**
     * Equivalent to javascripts ObjectPath.Assign, creates a new Object from startObject
     * overlaying any non-null values from the overlay Object.
     * @param <T> The Target type.
     * @param startObject intial Object of any type.
     * @param overlayObject overlay Object of any type.
     * @param type type to output.
     * @return merged Object.
     */
    public static <T> T assign(Object startObject, Object overlayObject, Class<T> type) {
        if (startObject != null && overlayObject != null) {
            // make a deep clone JsonNode of the startObject
            JsonNode merged = startObject instanceof JsonNode
                ? (JsonNode) clone(startObject)
                : Serialization.objectToTree(startObject);

            // get a JsonNode of the overlay Object
            JsonNode overlay = overlayObject instanceof JsonNode
                ? (JsonNode) overlayObject
                : Serialization.objectToTree(overlayObject);

            merge(merged, overlay);

            return Serialization.treeToValue(merged, type);
        }

        Object singleObject = startObject != null ? startObject : overlayObject;
        if (singleObject != null) {
            if (singleObject instanceof JsonNode) {
                return Serialization.treeToValue((JsonNode) singleObject, type);
            }

            return (T) singleObject;
        }

        return null;
// TODO default object
//        try {
//            return singleObject.newInstance();
//        } catch (InstantiationException | IllegalAccessException e) {
//            return null;
//        }
    }

    private static void merge(JsonNode startObject, JsonNode overlayObject) {
        Set<String> keySet = mergeKeys(startObject, overlayObject);

        for (String key : keySet) {
            JsonNode targetValue = startObject.findValue(key);
            JsonNode sourceValue = overlayObject.findValue(key);

            // skip empty overlay items
            if (!isNull(sourceValue)) {
                if (sourceValue instanceof ObjectNode) {
                    if (isNull(targetValue)) {
                        ((ObjectNode) startObject).set(key, clone(sourceValue));
                    } else {
                        merge(targetValue, sourceValue);
                    }
                } else { //if (targetValue instanceof NullNode) {
                    ((ObjectNode) startObject).set(key, clone(sourceValue));
                }
            }
        }
    }

    private static boolean isNull(JsonNode node) {
        return node == null || node instanceof NullNode;
    }

    private static Set<String> mergeKeys(JsonNode startObject, JsonNode overlayObject) {
        Set<String> keySet = new HashSet<>();
        Iterator<Entry<String, JsonNode>> iter = startObject.fields();
        while (iter.hasNext()) {
            Entry<String, JsonNode> entry = iter.next();
            keySet.add(entry.getKey());
        }

        iter = overlayObject.fields();
        while (iter.hasNext()) {
            Entry<String, JsonNode> entry = iter.next();
            keySet.add(entry.getKey());
        }

        return keySet;
    }

    /// <summary>
    /// Convert a generic Object to a typed Object.
    /// </summary>
    /// <typeparam name="T">type to convert to.</typeparam>
    /// <param name="val">value to convert.</param>
    /// <returns>converted value.</returns>
    /**
     * Convert a generic Object to a typed Object.
     * @param <T> type to convert to.
     * @param val value to convert.
     * @param valueType Type of T
     * @return converted value.
     */
    public static <T> T mapValueTo(Object val, Class<T> valueType) {
        if (val.getClass().equals(valueType)) {
            return (T) val;
        }

        if (val instanceof JsonNode) {
            return Serialization.treeToValue((JsonNode) val, valueType);
        }

        return Serialization.getAs(val, valueType);

        /*
        if (val instanceof JValue)
        {
            return ((JValue)val).ToObject<T>();
        }

        if (typeof(T) == typeof(Object))
        {
            return (T)val;
        }

        if (val is JArray)
        {
            return ((JArray)val).ToObject<T>();
        }

        if (val is JObject)
        {
            return ((JObject)val).ToObject<T>();
        }

        if (typeof(T) == typeof(JObject))
        {
            return (T)(Object)JObject.FromObject(val);
        }

        if (typeof(T) == typeof(JArray))
        {
            return (T)(Object)JArray.FromObject(val);
        }

        if (typeof(T) == typeof(JValue))
        {
            return (T)(Object)JValue.FromObject(val);
        }

        if (val is T)
        {
            return (T)val;
        }

        return JsonConvert.DeserializeObject<T>(JsonConvert.SerializeObject(val, _expressionCaseSettings));
        */
    }

    /**
     * Given an root Object and property path, resolve to a constant if eval = true or a constant path otherwise.
     * conversation[user.name][user.age] to ['conversation', 'joe', 32].
     * @param <T> Type of T
     * @param obj root Object.
     * @param propertyPath property path to resolve.
     * @return True if it was able to resolve all nested references.
     */
    public static <T> Segments tryResolvePath(Object obj, String propertyPath) {
        return tryResolvePath(obj, propertyPath, false);
    }

    /**
     * Given an root Object and property path, resolve to a constant if eval = true or a constant path otherwise.
     * conversation[user.name][user.age] to ['conversation', 'joe', 32].
     * @param <T> Type of T
     * @param obj root Object.
     * @param propertyPath property path to resolve.
     * @param eval True to evaluate resulting segments.
     * @return True if it was able to resolve all nested references.
     */
    public static <T> Segments tryResolvePath(Object obj, String propertyPath, boolean eval) {
        Segments soFar = new Segments();
        char first = propertyPath.length() > 0 ? propertyPath.charAt(0) : ' ';
        if (first == '\'' || first == '"') {
            if (!propertyPath.endsWith(String.valueOf(first))) {
                return null;
            }

            soFar.add(propertyPath.substring(1, propertyPath.length() - 1));
        } else if (isInt(propertyPath)) {
            soFar.add(Integer.parseInt(propertyPath));
        } else {
            int start = 0;
            int i;

            // Scan path evaluating as we go
            for (i = 0; i < propertyPath.length(); ++i) {
                char ch = propertyPath.charAt(i);
                if (ch == '.' || ch == '[') {
                    // emit
                    String segment = propertyPath.substring(start, i);
                    if (!StringUtils.isEmpty(segment)) {
                        soFar.add(segment);
                    }
                    start = i + 1;
                }

                if (ch == '[') {
                    // Bracket expression
                    int nesting = 1;
                    while (++i < propertyPath.length()) {
                        ch = propertyPath.charAt(i);
                        if (ch == '[') {
                            ++nesting;
                        } else if (ch == ']') {
                            --nesting;
                            if (nesting == 0) {
                                break;
                            }
                        }
                    }

                    if (nesting > 0) {
                        // Unbalanced brackets
                        return null;
                    }

                    String expr = propertyPath.substring(start, i);
                    start = i + 1;
                    Segments indexer = tryResolvePath(obj, expr, true);
                    if (indexer == null  || indexer.size() != 1) {
                        // Could not resolve bracket expression
                        return null;
                    }

                    String result = mapValueTo(indexer.first(), String.class);
                    if (isInt(result)) {
                        soFar.add(Integer.parseInt(result));
                    } else {
                        soFar.add(result);
                    }
                }
            }

            // emit
            String segment = propertyPath.substring(start, i);
            if (!StringUtils.isEmpty(segment)) {
                soFar.add(segment);
            }
            start = i + 1;

            if (eval) {
                Object result = resolveSegments(obj, soFar);
                if (result == null) {
                    return null;
                }

                soFar.clear();
                soFar.add(mapValueTo(result, Object.class));
            }
        }

        return soFar;
    }

    private static Object resolveSegment(Object current, Object segment) {
        if (current != null) {
            if (segment instanceof Integer) {
                int index = (Integer) segment;

                if (current instanceof List) {
                    current = ((List<Object>) current).get(index);
                } else if (current instanceof ArrayNode) {
                    current = ((ArrayNode) current).get(index);
                } else {
                    current = Array.get(current, index);
                }
            } else {
                current = getObjectProperty(current, (String) segment);
            }
        }

        return current;
    }

    private static Object resolveSegments(Object current, Segments segments) {
        Object result = current;
        for (Object segment : segments) {
            result = resolveSegment(result, segment);
            if (result == null) {
                return null;
            }
        }

        return result;
    }

    /// <summary>
    /// Get a property or array element from an Object.
    /// </summary>
    /// <param name="obj">Object.</param>
    /// <param name="property">property or array segment to get relative to the Object.</param>
    /// <returns>the value or null if not found.</returns>
    private static Object getObjectProperty(Object obj, String property) {
        if (obj == null) {
            return null;
        }

        // Because TurnContextStateCollection is not implemented as a Map<String, Object> we need to
        // set obj to the Map<String, Object> which holds the state values which is retrieved from calling
        // getTurnStateServices()
        if (obj instanceof TurnContextStateCollection) {
            Map<String, Object> dict = ((TurnContextStateCollection) obj).getTurnStateServices();
            List<Entry<String, Object>> matches = dict.entrySet().stream()
                .filter(key -> key.getKey().equalsIgnoreCase(property))
                .collect(Collectors.toList());

            if (matches.size() > 0) {
                return matches.get(0).getValue();
            }

            return null;
        }

        if (obj instanceof Map) {
            Map<String, Object> dict = (Map<String, Object>) obj;
            List<Entry<String, Object>> matches = dict.entrySet().stream()
                .filter(key -> key.getKey().equalsIgnoreCase(property))
                .collect(Collectors.toList());

            if (matches.size() > 0) {
                return matches.get(0).getValue();
            }

            return null;
        }

        if (obj instanceof JsonNode) {
            JsonNode node = (JsonNode) obj;
            Iterator<String> fields = node.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                if (field.equalsIgnoreCase(property)) {
                    return node.findValue(field);
                }
            }
            return null;
        }

        /*
        //!!! not sure Java equiv
        if (obj is JValue jval)
        {
            // in order to make things like "this.value.Length" work, when "this.value" is a String.
            return getObjectProperty(jval.Value, property);
        }
        */

        // reflection on Object
        List<Object> matches = Arrays.stream(obj.getClass().getDeclaredFields())
            .filter(field -> field.getName().equalsIgnoreCase(property))
            .map(field -> {
                try {
                    return field.get(obj);
                } catch (IllegalAccessException e) {
                    return null;
                }
            })
            .collect(Collectors.toList());

        if (matches.size() > 0) {
            return matches.get(0);
        }
        return null;
    }

    /// <summary>
    /// Given an Object, set a property or array element on it with a value.
    /// </summary>
    /// <param name="obj">Object to modify.</param>
    /// <param name="segment">property or array segment to put the value in.</param>
    /// <param name="value">value to store.</param>
    /// <param name="json">if true, value will be normalized to JSON primitive Objects.</param>
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private static void setObjectSegment(Object obj, Object segment, Object value) {
        setObjectSegment(obj, segment, value, true);
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private static void setObjectSegment(Object obj, Object segment, Object value, boolean json) {
        Object normalizedValue = getNormalizedValue(value, json);

        // Json Array
        if (segment instanceof Integer) {
            ArrayNode jar = (ArrayNode) obj;
            int index = (Integer) segment;

            if (index >= jar.size()) {
                jar.add(index + 1 - jar.size());
            }

            jar.set(index, Serialization.objectToTree(normalizedValue));
            return;
        }

        // Map
        String property = (String) segment;
        if (obj instanceof Map) {
            Boolean wasSet = false;
            Map<String, Object> dict = (Map<String, Object>) obj;
            for (String key : dict.keySet()) {
                if (key.equalsIgnoreCase(property)) {
                    wasSet = true;
                    dict.put(key, normalizedValue);
                    break;
                }
            }
            if (!wasSet) {
                dict.put(property, normalizedValue);
            }

            return;
        }

        // ObjectNode
        if (obj instanceof ObjectNode) {
            boolean wasSet = false;
            ObjectNode node = (ObjectNode) obj;
            Iterator<String> fields = node.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                if (field.equalsIgnoreCase(property)) {
                    wasSet = true;
                    node.set(property, Serialization.objectToTree(normalizedValue));
                    break;
                }
            }
            if (!wasSet) {
                node.set(property, Serialization.objectToTree(normalizedValue));
            }

            return;
        }

        // reflection
        if (obj != null) {
            for (Field f : obj.getClass().getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(property)) {
                    try {
                        f.set(obj, normalizedValue);
                    } catch (IllegalAccessException ignore) {
                    }
                }
            }
        }
    }

    /// <summary>
    /// Normalize value as json Objects.
    /// </summary>
    /// <param name="value">value to normalize.</param>
    /// <param name="json">normalize as json Objects.</param>
    /// <returns>normalized value.</returns>
    private static Object getNormalizedValue(Object value, boolean json) {
        Object val;

        if (json) {
            //TODO revisit this (from dotnet)
            if (value instanceof JsonNode) {
                val = clone(value);
            } else if (value == null) {
                val = null;
            } else {
                val = clone(value);
            }
        } else {
            val = value;
        }

        return val;
    }

    private static boolean isInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
