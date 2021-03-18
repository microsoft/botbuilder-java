// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.integration;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides access to properties defined in a Properties file located on the
 * classpath.
 */
public class ClasspathPropertiesConfiguration implements Configuration {
    /**
     * Holds the properties in application.properties.
     */
    private Properties properties;

    /**
     * Loads properties from the 'application.properties' file.
     */
    public ClasspathPropertiesConfiguration() {
        try (
            InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.properties")
        ) {
            properties = new Properties();
            properties.load(input);
        } catch (IOException e) {
            (LoggerFactory.getLogger(ClasspathPropertiesConfiguration.class))
                .error("Unable to load properties", e);
        }
    }

    /**
     * Returns a value for the specified property name.
     *
     * @param key The property name.
     * @return The property value.
     */
    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * @return The Properties value.
     */
    @Override
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Returns an array of values from an entry that is comma delimited.
     * @param key The property name.
     * @return The property values as a String array.
     */
    @Override
    public String[] getProperties(String key) {
        String baseProperty = properties.getProperty(key);
        if (baseProperty != null) {
            String[] splitProperties = baseProperty.split(",");
            return splitProperties;
        } else {
            return null;
        }
    }
}
