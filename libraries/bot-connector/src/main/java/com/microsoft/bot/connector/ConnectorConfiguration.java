/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector;

import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Loads configuration properties for bot-connector.
 *
 * The version of the package will be in the project.properties file.
 */
public class ConnectorConfiguration {
    /**
     * Load and pass properties to a function.
     *
     * @param func The function to process the loaded properties.
     */
    public void process(Consumer<Properties> func) {
        final Properties properties = new Properties();
        try (InputStream propStream =
            UserAgent.class.getClassLoader().getResourceAsStream("project.properties")) {

            properties.load(propStream);
            if (!properties.containsKey("version")) {
                properties.setProperty("version", "4.0.0");
            }
            func.accept(properties);
        } catch (Throwable t) {
            Properties p = new Properties();
            p.setProperty("version", "4.0.0");
            func.accept(p);
        }
    }
}
