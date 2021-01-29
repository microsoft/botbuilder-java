// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory;
/**
 * Interface for declaring path resolvers in the memory system.
 */
public interface ComponentPathResolvers {
    /**
     * Return enumeration of pathresolvers.
     *
     * @return collection of PathResolvers.
     */
    Iterable<PathResolver> getPathResolvers();
}
