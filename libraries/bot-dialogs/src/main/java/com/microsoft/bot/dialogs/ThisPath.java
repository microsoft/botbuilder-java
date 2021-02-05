// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

/**
 * Defines path passed to the active dialog.
 */
public final class ThisPath {

    private ThisPath() {
    }

    /**
     * The options that were passed to the active dialog via options argument of
     * BeginDialog.
     */
    public static final String OPTIONS = "this.options";
}
