// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.schema.Activity;

public class EventActivityPrompt extends ActivityPrompt {

    public EventActivityPrompt(String dialogId, PromptValidator<Activity> activityPromptTestValidator) {
        super(dialogId, activityPromptTestValidator);
    }

    public CompletableFuture<Void> onPromptNullContext(Object options) {
        PromptOptions opt = (PromptOptions) options;
        // should throw ArgumentNullException
         return super.onPrompt(null, null, opt, false);
    }

    public CompletableFuture<Void> onPromptNullOptions(DialogContext dc) {
        // should throw ArgumentNullException
        return super.onPrompt(dc.getContext(), null, null, false);
    }

}
