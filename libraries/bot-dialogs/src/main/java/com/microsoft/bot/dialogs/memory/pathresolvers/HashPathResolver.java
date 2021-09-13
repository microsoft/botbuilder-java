// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.memory.pathresolvers;

/**
 * Maps #xxx to turn.recognized.intents.xxx.
 */
public class HashPathResolver extends AliasPathResolver {

    /**
     *  Initializes a new instance of the HashPathResolver class.
     */
    public HashPathResolver() {
        super("#", "turn.recognized.intents.", null);
    }
}
