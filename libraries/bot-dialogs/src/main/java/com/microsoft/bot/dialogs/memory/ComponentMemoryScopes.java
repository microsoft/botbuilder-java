// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory;

import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;

/**
 * Defines Component Memory Scopes interface for enumerating memory scopes.
 */
public interface ComponentMemoryScopes {

    /**
     * Gets the memory scopes.
     *
     * @return A reference with the memory scopes.
     */
        Iterable<MemoryScope> getMemoryScopes();
}
