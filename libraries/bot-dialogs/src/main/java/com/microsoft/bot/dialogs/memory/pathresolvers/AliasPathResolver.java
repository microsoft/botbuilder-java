// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.pathresolvers;

import com.microsoft.bot.dialogs.memory.PathResolver;

/**
 * Maps aliasXXX to path.xxx ($foo to dialog.foo).
 */
public class AliasPathResolver implements PathResolver {

    private final String postfix;
    private final String prefix;

    /**
     *
     * @param alias   Alias name.
     * @param prefix  Prefix name.
     * @param postfix Postfix name.
     */
    public AliasPathResolver(String alias, String prefix, String postfix) {
        if (alias == null) {
            throw new IllegalArgumentException("alias cannot be null");
        }

        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be null.");
        }

        this.prefix = prefix.trim();

        setAlias(alias.trim());

        if (postfix == null) {
            this.postfix = "";
        } else {
            this.postfix = postfix;
        }
    }

    /**
     * @return Gets the alias name.
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * @param alias Sets the alias name.
     */
    private void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * The alias name.
     */
    private String alias;

    /**
     * @param path Path to transform.
     * @return The transformed path.
     */
    public String transformPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null.");
        }

        path = path.trim();
        if (path.startsWith(getAlias()) && path.length() > getAlias().length()
                && isPathChar(path.charAt(getAlias().length()))) {
            // here we only deals with trailing alias, alias in middle be handled in further
            // breakdown
            // $xxx -> path.xxx
            return prefix + path.substring(getAlias().length()) + postfix;
        }

        return path;
    }

    /**
     *
     * @param ch Character to verify.
     * @return true if the character is valid for a path; otherwise, false.
     */
    protected Boolean isPathChar(char ch) {
        return Character.isLetter(ch) || ch == '_';
    }
}
