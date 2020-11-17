package com.microsoft.bot.dialogs.memory.PathResolvers;

/**
 * Maps #xxx => turn.recognized.intents.xxx.
 */
public class HashPathResolver extends AliasPathResolver {

    /**
     *  Initializes a new instance of the HashPathResolver class.
     */
    public HashPathResolver() {
        super("#", "turn.recognized.intents.", null);
    }
}
