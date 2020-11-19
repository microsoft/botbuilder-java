package com.microsoft.bot.dialogs.memory.scopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;

import com.microsoft.bot.dialogs.ObjectPath;

/**
 * ReadOnlyObject is a wrapper around any Object to prevent setting of
 * properties on the Object.
 */
public class ReadOnlyObject extends Dictionary<String, Object> {

    private final String notSupported = "This Object is final.";

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
     * @return The number of items.
     */
    public int count() {
        return size();
    }

    /**
     *
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     *
     */
    @Override
    public Enumeration<String> keys() {
        return Collections.enumeration(ObjectPath.getProperties(obj));
    }

    /**
     *
     */
    @Override
    public Enumeration<Object> elements() {
        Enumeration<String> keys = this.keys();
        ArrayList<Object> elements = new ArrayList<Object>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            elements.add(getValue(key));
        }
        return Collections.enumeration(elements);
    }

    /**
     *
     * @return The values.
     */
    public Enumeration<Object> values() {
        return elements();
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

    /**
     * Get a value based on a key.
     *
     * @param name Key of the value.
     * @return The value associated with the provided key.
     */
    public Object getValue(String name) {
        Object value = ObjectPath.tryGetPathValue(obj, name, Object.class);
        if (value != null) {
            return new ReadOnlyObject(value);
        } else {
            return null;
        }
    }

}
