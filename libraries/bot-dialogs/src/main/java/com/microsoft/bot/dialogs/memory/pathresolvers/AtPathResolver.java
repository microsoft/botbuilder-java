// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.pathresolvers;

/**
 * Maps @@ to turn.recognized.entitites.xxx array.
 */
public class AtPathResolver extends AliasPathResolver {

    private final String prefix = "turn.recognized.entities.";

    private static final char[] DELIMS = {'.', '[' };

    /**
     * Initializes a new instance of the AtPathResolver class.
     */
    public AtPathResolver() {
        super("@", "", null);
    }

    /**
     * Transforms the path.
     *
     * @param path Path to transform.
     * @return The transformed path.
     */
    @Override
    public String transformPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null.");
        }

        path = path.trim();
        if (path.startsWith("@") && path.length() > 1 && isPathChar(path.charAt(1))) {
            int end = 0;
            int endperiod = path.indexOf(DELIMS[0]);
            int endbracket = path.indexOf(DELIMS[1]);
            if (endperiod == -1 && endbracket == -1) {
                end = path.length();
            } else {
                end = Math.max(endperiod, endbracket);
            }

            String property = path.substring(1, end);
            String suffix = path.substring(end);
            path = prefix + property + ".first()" + suffix;
        }

        return path;
    }

}
