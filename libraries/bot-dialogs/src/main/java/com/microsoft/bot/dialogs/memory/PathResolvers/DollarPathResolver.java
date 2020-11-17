package com.microsoft.bot.dialogs.memory.PathResolvers;

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
