// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.concurrent.CompletableFuture;

/**
 * An inteface to run the test.
 */
interface DialogTestFunction {
    /**
     * Method to run the test.
     * @param dc DialogContext to run the test against.
     * @return a CompletableFuture
     */
    CompletableFuture<Void> runTest(DialogContext dc);
}

/**
 * Dialog that can process a test.
 */
public class LamdbaDialog extends Dialog {

    private DialogTestFunction func;

    /**
     *
     * @param testName The name of the test being performed
     * @param function The function that will perform the test.
     */
    public LamdbaDialog(String testName, DialogTestFunction function) {
        super(testName);
        func = function;
    }

    /**
     * Override the beginDialog method and call our test function here.
     */
    @Override
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
        return func.runTest(dc).thenCompose(result -> dc.endDialog());
    }
}
