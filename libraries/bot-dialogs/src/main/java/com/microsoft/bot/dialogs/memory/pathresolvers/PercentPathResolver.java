// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.pathresolvers;

/**
 * Maps %xxx to settings.xxx (aka activeDialog.Instance.xxx).
 */
public class PercentPathResolver extends AliasPathResolver {

    /**
     *  Initializes a new instance of the PercentPathResolver class.
     */
    public PercentPathResolver() {
        super("%", "class.", null);
    }
}
