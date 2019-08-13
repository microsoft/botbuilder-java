// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.dialogs;

import java.util.HashMap;

/**
 * Result returned to the caller of one of the various stack manipulation methods and used to
 * return the result from a final call to `DialogContext.end()` to the bots logic.
 */
public class DialogCompletion
{


    /**
     * If 'true' the dialog is still active.
     */
    boolean _isActive;
    public void setIsActive(boolean isActive) {
        this._isActive =  isActive;
    }
    public boolean getIsActive() {
        return this._isActive;
    }

    /**
     * If 'true' the dialog just completed and the final [result](#result) can be retrieved.
     */
    boolean _isCompleted;
    public void setIsCompleted(boolean isCompleted)
    {
        this._isCompleted = isCompleted;
    }
    public boolean getIsCompleted()
    {
        return this._isCompleted;
    }

    /**
     * Result returned by a dialog that was just ended.This will only be populated in certain
     * cases:
     * - The bot calls `dc.begin()` to start a new dialog and the dialog ends immediately.
     * - The bot calls `dc.continue()` and a dialog that was active ends.
     * In all cases where it's populated, [active](#active) will be `false`.        
     */
    HashMap<String, Object> _result;
    public HashMap<String, Object> getResult() {
        return _result;
    }
    public void setResult(HashMap<String, Object> result) {
        this._result = result;
    }
}
