package com.microsoft.bot.dialogs.memory.PathResolvers;

/**
 * Defines Path Resolver interface for transforming paths.
 */
public interface PathResolver {
    /**
     * Transform the path.
     *
     * @param path path to inspect.
     * @return transformed path.
     */
    String transformPath(String path);
}
