// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ComponentRegistration is a signature class for discovering assets from components.
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class ComponentRegistration {

    private static final ConcurrentHashMap<Class<?>, ComponentRegistration> COMPONENTS =
        new ConcurrentHashMap<Class<?>, ComponentRegistration>();

    /**
     * Add a component which implements registration methods.
     *
     * @param componentRegistration The component to add to the registration.
     */
    public static void add(ComponentRegistration componentRegistration) {
        COMPONENTS.put(componentRegistration.getClass(), componentRegistration);
    }

    /**
     * Gets list of all ComponentRegistration objects registered.
     *
     * @return A array of ComponentRegistration objects.
     */
    public static Iterable<ComponentRegistration> getComponents() {
        return COMPONENTS.values();
    }
}
