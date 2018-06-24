package com.microsoft.bot.connector;

import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.joining;

/**
 * Retrieve the User Agent string that BotBuilder uses
 * <p>
 * Conforms to spec:
 * https://github.com/Microsoft/botbuilder-dotnet/blob/d342cd66d159a023ac435aec0fdf791f93118f5f/doc/UserAgents.md
 * <p>
 */
public class UserAgent {
    private static Object synclock = new Object();
    private static Map<String, String> middlewareSet = new ConcurrentHashMap<String, String>();
    private static Map<String, String> storageSet = new ConcurrentHashMap<String, String>();
    private static String luisVersion = null;
    private static String qnaVersion = null;
    private static boolean hasChanged = true;
    private static String cache_value = null;


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
     *
     */
    public static String value() {

        synchronized (synclock) {
            if (!hasChanged) {
                return cache_value;
            }
        }


        StringBuilder val = new StringBuilder();
        val.append(os_java_botbuilder_cache);
        if (middlewareSet.size() > 0) {
            String middleware = middlewareSet.values().stream().collect(joining(";"));
            val.append(String.format(" Middleware(%s)", middleware));
        }
        if (storageSet.size() > 0) {
            String storage = storageSet.values().stream().collect(joining(";"));
            val.append(String.format(" Storage(%s)", storage));
        }
        if (!StringUtils.isBlank(luisVersion))
            val.append(String.format(" LUIS(%s)", luisVersion));
        if (!StringUtils.isBlank(qnaVersion))
            val.append(String.format(" Qna(%s)", qnaVersion));
        synchronized (synclock) {
            cache_value = val.toString();
            hasChanged = false;
        }
        return cache_value;
    }

    /**
     * Add a Middleware component to the agent string.
     * Idempotent and last component wins.  For example, if two versions of a component registers with two different
     * versions, (1) only one component will be in the agent string, (2) the first version that registers will be
     * the one reported.
     *
     * @param middlewareName    Friendly name of the Middleware component
     * @param middlewareVersion Component version in form v1.1.0
     */
    public static void AddMiddlewareComponent(String middlewareName, String middlewareVersion) {
        synchronized (synclock) {
            hasChanged = true;
            middlewareSet.put(middlewareName, middlewareName + "/" + middlewareVersion);
        }
    }

    /**
     * Add a Storage component to the agent string.
     * Idempotent and last component wins.  For example, if two versions of a component registers with two different
     * versions, (1) only one component will be in the agent string, (2) the first version that registers will be
     * the one reported.
     *
     * @param storageName    Friendly name of the Storage component
     * @param storageVersion Component version in the form v1.1.0
     */
    public static void AddStorageComponent(String storageName, String storageVersion) {
        synchronized (synclock) {
            hasChanged = true;
            storageSet.put(storageName, storageName + "/" + storageVersion);
        }
    }

    /**
     * Add a LUIS version to the agent string.
     * Idempotent and last call wins.  For example, if two versions of a component registers with two different
     * versions, (1) only one LUIS component will be in the agent string, (2) the first version that registers will be
     * the one reported.
     *
     * @param version LUIS component version in the form v1.1.0
     */
    public static void SetLuisVersion(String version) {
        if (StringUtils.isBlank(luisVersion)) {
            synchronized (synclock) {
                hasChanged = true;
                luisVersion = version;
            }
        }
    }

    /**
     * Add a Qna version to the agent string.
     * Idempotent and last call wins.  For example, if two versions of a component registers with two different
     * versions, (1) only one Qna component will be in the agent string, (2) the first version that registers will be
     * the one reported.
     *
     * @param version Qna component version in the form v1.1.0
     */
    public static void SetQnaVersion(String version) {
        if (StringUtils.isBlank(qnaVersion)) {
            synchronized (synclock) {
                hasChanged = true;
                qnaVersion = version;
            }
        }
    }
}
