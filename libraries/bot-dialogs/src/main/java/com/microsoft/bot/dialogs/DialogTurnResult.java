// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

/**
 * Result returned to the caller of one of the various stack manipulation methods.
 */
public class DialogTurnResult {
    private DialogTurnStatus status;
    private Object result;
    private boolean parentEnded;

    /**
     * Creates a DialogTurnResult with a status.
     * @param withStatus The dialog status.
     */
    public DialogTurnResult(DialogTurnStatus withStatus) {
        this(withStatus, null);
    }

    /**
     * Creates a DialogTurnResult with a status and result.
     * @param withStatus The dialog status.
     * @param withResult The result.
     */
    public DialogTurnResult(DialogTurnStatus withStatus, Object withResult) {
        status = withStatus;
        result = withResult;
    }

    /**
     * Gets the current status of the stack.
     * @return The current status of the stack.
     */
    public DialogTurnStatus getStatus() {
        return status;
    }

    /**
     * Sets the current status of the stack.
     * @param withStatus The current status of the stack.
     */
    public void setStatus(DialogTurnStatus withStatus) {
        status = withStatus;
    }

    /**
     * Gets or sets the result returned by a dialog that was just ended.
     *
     * <p>This will only be populated in certain cases:
     * <br>- The bot calls `DialogContext.BeginDialogAsync()` to start a new dialog and the dialog
     * ends immediately.</br>
     * <br>- The bot calls `DialogContext.ContinueDialogAsync()` and a dialog that was active ends.</br></p>
     *
     * <p>In all cases where it's populated, {@link "DialogContext.ActiveDialog"} will be `null`.</p>
     * @return The result returned by a dialog that was just ended.
     */
    public Object getResult() {
        return result;
    }

    /**
     * Sets the result returned by a dialog that was just ended.
     * @param withResult The result returned by a dialog that was just ended.
     */
    public void setResult(Object withResult) {
        result = withResult;
    }

    /**
     * Indicates whether a DialogCommand has ended its parent container and the parent should
     * not perform any further processing.
     * @return Whether a DialogCommand has ended its parent container and the parent should
     * not perform any further processing.
     */
    public boolean hasParentEnded() {
        return parentEnded;
    }

    /**
     * Sets whether a DialogCommand has ended its parent container and the parent should
     * not perform any further processing.
     * @param withParentEnded Whether a DialogCommand has ended its parent container and the
     *                        parent should not perform any further processing.
     */
    public void setParentEnded(boolean withParentEnded) {
        parentEnded = withParentEnded;
    }
}
