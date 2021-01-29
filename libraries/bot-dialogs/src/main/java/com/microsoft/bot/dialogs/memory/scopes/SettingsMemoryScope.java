// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.scopes;

import java.util.Properties;
import java.util.TreeMap;

import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.ScopePath;
import com.microsoft.bot.integration.Configuration;

/**
 * TurnMemoryScope represents memory scoped to the current turn.
 */
public class SettingsMemoryScope extends MemoryScope {
    /**
     * Initializes a new instance of the TurnMemoryScope class.
     */
    public SettingsMemoryScope() {
        super(ScopePath.SETTINGS, false);
    }

    /**
     * Get the backing memory for this scope.
     */
    @Override
    public final Object getMemory(DialogContext dialogContext) {
        if (dialogContext == null) {
            throw new IllegalArgumentException("dialogContext cannot be null.");
        }

        Object returnValue;

        returnValue = dialogContext.getContext().getTurnState().get(ScopePath.TURN);
        if (returnValue == null) {
            Configuration configuration = dialogContext.getContext().getTurnState().get(Configuration.class);
            if (configuration != null) {
                returnValue = loadSettings(configuration);
                dialogContext.getContext().getTurnState().add(ScopePath.SETTINGS, returnValue);
            }
        }
        return returnValue;
    }

    /**
     * Changes the backing Object for the memory scope.
     */
    @Override
    public final void setMemory(DialogContext dialogContext, Object memory) {
        throw new UnsupportedOperationException("You cannot set the memory for a final memory scope");
    }

    /**
     * Loads the settings from configuration.
     *
     * @param configuration The configuration to load Settings from.
     * @return The collection of settings.
     */
    protected static TreeMap<String, Object> loadSettings(Configuration configuration) {
        TreeMap<String, Object> settings = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

        if (configuration != null) {
            Properties properties = configuration.getProperties();
            properties.forEach((k, v) -> {
                settings.put((String) k, v);
            });
        }

        return settings;
    }

}
