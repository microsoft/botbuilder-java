// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.concurrent.CompletableFuture;

/**
 * A interface definition of a Waterfall step. This is implemented by
 * application code.
 */
public interface WaterfallStep {
    /**
     * A interface definition of a Waterfall step. This is implemented by
     * application code.
     *
     * @param stepContext The WaterfallStepContext for this waterfall dialog.
     *
     * @return A {@link CompletableFuture} of {@link DialogTurnResult} representing
     *         the asynchronous operation.
     */
    CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext);
}
