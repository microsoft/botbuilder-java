/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector;

import org.slf4j.LoggerFactory;

/**
 * Retrieve the User Agent string that Bot SDK uses.
 * <p>
 * Conforms to spec:
 * https://github.com/Microsoft/botbuilder-dotnet/blob/d342cd66d159a023ac435aec0fdf791f93118f5f/doc/UserAgents.md
 * 
 */
public final class UserAgent {
    // os/java and botbuilder will never change - static initialize once
    private static String osJavaBotbuilderCache;

    static {
        new ConnectorConfiguration().process(properties -> {
            String buildVersion = properties.getProperty("version");
            String osVersion = System.getProperty("os.name");
            String javaVersion = System.getProperty("java.version");
            osJavaBotbuilderCache =
                String.format("BotBuilder/%s (JVM %s; %s)", buildVersion, javaVersion, osVersion);

            LoggerFactory.getLogger(UserAgent.class).info("UserAgent: {}", osJavaBotbuilderCache);
        });
    }

    /**
     * Private Constructor - Static Object.
     */
    private UserAgent() {

    }

    /**
     * Retrieve the user agent string for BotBuilder.
     * 
     * @return THe user agent string.
     */
    public static String value() {
        return osJavaBotbuilderCache;
    }
}
