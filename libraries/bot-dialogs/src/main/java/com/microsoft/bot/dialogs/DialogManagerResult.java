// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.microsoft.bot.schema.Activity;

/**
 * Represents the result of the Dialog Manager turn.
 */
public class DialogManagerResult {
    /**
     * The result returned to the caller.
     */
    private DialogTurnResult turnResult;

    /**
     * The array of resulting activities.
     */
    private Activity[] activities;

    /**
     * The resulting new state.
     */
    private PersistedState newState;

    /**
     * @return DialogTurnResult
     */
    public DialogTurnResult getTurnResult() {
        return this.turnResult;
    }

    /**
     * @param withTurnResult Sets the turnResult.
     */
    public void setTurnResult(DialogTurnResult withTurnResult) {
        this.turnResult = withTurnResult;
    }

    /**
     * @return Activity[]
     */
    public Activity[] getActivities() {
        return this.activities;
    }

    /**
     * @param withActivities Sets the activites.
     */
    public void setActivities(Activity[] withActivities) {
        this.activities = withActivities;
    }

    /**
     * @return PersistedState
     */
    public PersistedState getNewState() {
        return this.newState;
    }

    /**
     * @param withNewState sets the newState.
     */
    public void setNewState(PersistedState withNewState) {
        this.newState = withNewState;
    }
}
