// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.concurrent.CompletableFuture;

import javax.activation.UnsupportedDataTypeException;

import com.microsoft.bot.dialogs.DialogContext;

public class NumberPromptMock extends NumberPrompt<Integer> {

    public NumberPromptMock(String dialogId, PromptValidator<Integer> validator, String defaultLocale)
            throws UnsupportedDataTypeException {
        super(dialogId, validator, defaultLocale, Integer.class);
    }

    public CompletableFuture<Void> onPromptNullContext(Object options) {
        PromptOptions opt = (PromptOptions) options;

        // should throw ArgumentNullException
         return onPrompt(null, null, opt, false);
    }

    public CompletableFuture<Void> onPromptNullOptions(DialogContext dc) {
        // should throw ArgumentNullException
         return onPrompt(dc.getContext(), null, null, false);
    }

    public CompletableFuture<Void> onRecognizeNullContext() {
        // should throw ArgumentNullException
         onRecognize(null, null, null).join();
         return CompletableFuture.completedFuture(null);
    }
}
