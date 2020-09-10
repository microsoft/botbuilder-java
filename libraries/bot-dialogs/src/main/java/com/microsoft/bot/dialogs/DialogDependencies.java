package com.microsoft.bot.dialogs;

import java.util.List;

/**
 * Enumerate child dialog dependencies so they can be added to the containers dialogset.
 */
public interface DialogDependencies {

    /**
     * Enumerate child dialog dependencies so they can be added to the containers dialogset.
     * @return Dialog list
     */
    List<Dialog> getDependencies();
}
