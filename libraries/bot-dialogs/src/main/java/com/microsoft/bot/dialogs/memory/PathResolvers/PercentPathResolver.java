package com.microsoft.bot.dialogs.memory.pathresolvers;

/**
 * Maps %xxx => settings.xxx (aka activeDialog.Instance.xxx).
 */
public class PercentPathResolver extends AliasPathResolver {

    /**
     *  Initializes a new instance of the PercentPathResolver class.
     */
    public PercentPathResolver() {
        super("%", "class.", null);
    }
}
