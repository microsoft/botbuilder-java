package com.microsoft.bot.integration;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.LoggerFactory;

/**
 * Provides access to properties defined in a Properties file located on the classpath.
 */
public class ClasspathPropertiesConfiguration implements Configuration {
    private Properties properties;

    /**
     * Loads properties from the 'application.properties' file.
     * @throws IOException
     */
    public ClasspathPropertiesConfiguration() {
        try {
            properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        }
        catch (IOException e) {
            (LoggerFactory.getLogger(ClasspathPropertiesConfiguration.class)).error("Unable to load properties", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
