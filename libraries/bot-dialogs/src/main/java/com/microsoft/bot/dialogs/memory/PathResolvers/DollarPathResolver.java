// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.pathresolvers;

/**
 * Resolve $xxx.
 */
public class DollarPathResolver extends AliasPathResolver {

    /**
     *  Initializes a new instance of the DollarPathResolver class.
     */
    public DollarPathResolver() {
        super("$", "dialog.", null);
    }
}
