// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.scopes;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.bot.dialogs.ObjectPath;

/**
 * ReadOnlyObject is a wrapper around any Object to prevent setting of
 * properties on the Object.
 */
public final class ReadOnlyObject implements Map<String, Object> {

    private final String notSupported = "This Object is final and cannot be modified.";

    private Object obj;

    /**
     *
     * @param obj Object to wrap. Any expression properties on it will be evaluated
     *            using the dc.
     */
    public ReadOnlyObject(Object obj) {
        this.obj = obj;
    }

    /**
     * @return The number of items.
     */
    @Override
    public int size() {
        return ObjectPath.getProperties(obj).size();
    }

    /**
     *
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return ObjectPath.containsProperty(obj, (String) key);
    }

    /**
     *
     */
    @Override
    public Set<String> keySet() {
        return new HashSet<String>(ObjectPath.getProperties(obj));
    }

    /**
     *
     */
    @Override
    public Object get(Object key) {

        if (!(key instanceof String)) {
            throw new IllegalArgumentException("key is required and must be a String type.");
        }

        return ObjectPath.tryGetPathValue(obj, (String) key, Object.class);
    }

    /**
     *
     */
    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException(notSupported);
    }

    /**
     *
     */
    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException(notSupported);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException(notSupported);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(notSupported);
    }

    @Override
    public Collection<Object> values() {
        Set<String> keys = this.keySet();
        List<Object> objectList = new ArrayList<Object>();
        for (String key : keys) {
            Object foundValue = ObjectPath.tryGetPathValue(obj, key, Object.class);
            if (foundValue != null) {
                objectList.add(foundValue);
            }
        }
        return objectList;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> items = new HashSet<Entry<String, Object>>();
        Set<String> keys = this.keySet();
        for (String key : keys) {
            Object foundValue = ObjectPath.tryGetPathValue(obj, key, Object.class);
            if (foundValue != null) {
                items.add(new SimpleEntry<String, Object>(key, foundValue));
            }
        }
        return items;
    }

}
