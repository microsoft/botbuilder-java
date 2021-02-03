// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

/**
 * Result returned to the caller of one of the various stack manipulation methods.
 */
public enum DialogTurnStatus {
    /// Indicates that there is currently nothing on the dialog stack.
    EMPTY,

    /// Indicates that the dialog on top is waiting for a response from the user.
    WAITING,

    /// Indicates that a dialog completed successfully, the result is available, and no child
    /// dialogs to the current context are on the dialog stack.
    COMPLETE,

    /// Indicates that the dialog was canceled, and no child
    /// dialogs to the current context are on the dialog stack.
    CANCELLED,

    /// Current dialog completed successfully, but turn should end.
    COMPLETEANDWAIT,
}
