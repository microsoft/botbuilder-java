package com.microsoft.bot.dialogs.memory.PathResolvers;

/**
 * Maps @@ => turn.recognized.entitites.xxx array.
 */
public class AtAtPathResolver extends AliasPathResolver {

    /**
     *  Initializes a new instance of the AtAtPathResolver class.
     */
    public AtAtPathResolver() {
        super("@@", "turn.recognized.entities.", null);
    }
}
