// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.utils;

import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.schema.Activity;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

/**
 * Class to bind activities.
 */
public class BindToActivity {
    private final Activity activity;

    /**
     * Construct to bind an Activity.
     *
     * @param withActivity activity to bind.
     */
    public BindToActivity(Activity withActivity) {
        this.activity = withActivity;
    }

    /**
     *
     * @param context The context.
     * @param data    The data.
     * @return The activity.
     */
    public CompletableFuture<Activity> bind(DialogContext context, @Nullable Object data) {
        return CompletableFuture.completedFuture(this.activity);
    }

    /**
     * Get the activity text.
     *
     * @return The activity text.
     */
    public String toString() {
        return String.format("%s", this.activity.getText());
    }
}
