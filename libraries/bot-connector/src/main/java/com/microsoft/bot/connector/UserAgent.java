package com.microsoft.bot.connector;

import com.microsoft.bot.connector.implementation.ConnectorClientImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Retrieve the User Agent string that BotBuilder uses
 * <p>
 * Conforms to spec:
 * https://github.com/Microsoft/botbuilder-dotnet/blob/d342cd66d159a023ac435aec0fdf791f93118f5f/doc/UserAgents.md
 * <p>
 */
public class UserAgent {


    // os/java and botbuilder will never change - static initialize once
    private static String os_java_botbuilder_cache;

    static {
        String build_version;
        final Properties properties = new Properties();
        try {
            InputStream propStream = ConnectorClientImpl.class.getClassLoader().getResourceAsStream("project.properties");
            properties.load(propStream);
            build_version = properties.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
            build_version = "4.0.0";
        }
        String os_version = System.getProperty("os.name");
        String java_version = System.getProperty("java.version");
        os_java_botbuilder_cache = String.format("BotBuilder/%s (JVM %s; %s)", build_version, java_version, os_version);
    }

    /**
     * Private Constructor - Static Object
     */
    private UserAgent() {

    }

    /**
     * Retrieve the user agent string for BotBuilder.
     */
    public static String value() {
        return os_java_botbuilder_cache;
    }

}
