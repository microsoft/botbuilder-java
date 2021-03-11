// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.microsoft.bot.schema.Activity;

/**
 * A class with dialog arguments for a {@link SkillDialog} .
 */
public class BeginSkillDialogOptions {

    private Activity activity;

    /**
     * Gets the {@link Activity} to send to the skill.
     * @return the Activity value as a getActivity().
     */
    public Activity getActivity() {
        return this.activity;
    }

    /**
     * Sets the {@link Activity} to send to the skill.
     * @param withActivity The Activity value.
     */
    public void setActivity(Activity withActivity) {
        this.activity = withActivity;
    }

}
