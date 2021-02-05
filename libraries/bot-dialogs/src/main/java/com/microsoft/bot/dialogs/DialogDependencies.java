// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
