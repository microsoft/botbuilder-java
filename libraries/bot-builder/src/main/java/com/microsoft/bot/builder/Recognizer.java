// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

public interface Recognizer {
    CompletableFuture<RecognizerResult> recognize(TurnContext turnContext);
}
