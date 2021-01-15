// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.utils;

import com.microsoft.bot.schema.Activity;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

// TODO: missing ITemplate interface and DialogContext class
public class BindToActivity implements ITemplate<Activity> {
    private Activity activity;

    public BindToActivity(Activity withActivity) {
        this.activity = withActivity;
    }

    public CompletableFuture<Activity> bind(DialogContext context, @Nullable Object data) {
        return CompletableFuture.completedFuture(this.activity);
    }

    @Override
    public String toString() {
        return String.format("%s", this.activity.getText());
    }
}
