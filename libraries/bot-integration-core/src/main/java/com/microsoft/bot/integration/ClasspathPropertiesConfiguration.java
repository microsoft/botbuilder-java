package com.microsoft.bot.integration;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Provides access to properties defined in a Properties file located on the classpath.
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
        try {
            properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.properties"));
        } catch (IOException e) {
            (LoggerFactory.getLogger(ClasspathPropertiesConfiguration.class)).error("Unable to load properties", e);
        }
    }

    /**
     * Returns a value for the specified property name.
     * @param key The property name.
     * @return The property value.
     */
    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
