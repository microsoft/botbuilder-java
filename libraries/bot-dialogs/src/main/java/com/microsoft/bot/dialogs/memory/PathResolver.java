// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory;
/**
 * Defines Path Resolver interface for transforming paths.
 */
public interface PathResolver {
    /**
     *
     * @param path path to inspect.
     * @return transformed path.
     */
    String transformPath(String path);

}
