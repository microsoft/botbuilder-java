// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

/**
 * Indicates in which a dialog-related method is being called.
 */
public enum DialogReason {
    /// A dialog was started.
    BEGIN_CALLED,

    /// A dialog was continued.
    CONTINUE_CALLED,

    /// A dialog was ended normally.
    END_CALLED,

    /// A dialog was ending because it was replaced.
    REPLACE_CALLED,

    /// A dialog was canceled.
    CANCEL_CALLED,

    /// A preceding step of the dialog was skipped.
    NEXT_CALLED
}
