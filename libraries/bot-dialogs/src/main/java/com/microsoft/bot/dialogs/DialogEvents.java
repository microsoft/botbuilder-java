// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;
/**
 * Represents the events related to the "lifecycle" of the dialog.
 */
public final class DialogEvents {
    private DialogEvents() { }

    /// Event fired when a dialog beginDialog() is called.
    public static final String BEGIN_DIALOG = "beginDialog";

    /// Event fired when a dialog RepromptDialog is Called.
    public static final String REPROMPT_DIALOG = "repromptDialog";

    /// Event fired when a dialog is canceled.
    public static final String CANCEL_DIALOG = "cancelDialog";

    /// Event fired when an activity is received from the adapter (or a request to reprocess an activity).
    public static final String ACTIVITY_RECEIVED = "activityReceived";

    /// Event which is fired when the system has detected that deployed code has changed the execution of dialogs
    /// between turns.
    public static final String VERSION_CHANGED = "versionChanged";

    /// Event fired when there was an exception thrown in the system.
    public static final String ERROR = "error";
}
