// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

/**
 * Defines path for available dialog contexts.
 */
public final class DialogContextPath {

    private DialogContextPath() {

    }

    /**
     * Memory Path to dialogContext's active dialog.
     */
    public static final String ACTIVEDIALOG = "dialogcontext.activeDialog";

    /**
     * Memory Path to dialogContext's parent dialog.
     */
    public static final String PARENT = "dialogcontext.parent";

    /**
     * Memory Path to dialogContext's stack.
     */
    public static final String STACK = "dialogContext.stack";
}
