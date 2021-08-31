// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.Async;

/**
 * Provides context for a step in a {@link WaterfallDialog} .
 *
 * The {@link DialogContext} property contains the {@link TurnContext}
 * for the current turn.
 */
public class WaterfallStepContext extends DialogContext {

    private final WaterfallDialog parentWaterfall;
    private Boolean nextCalled;

    /**
     * Initializes a new instance of the {@link WaterfallStepContext} class.
     *
     * @param parentWaterfall  The parent of the waterfall dialog.
     * @param dc               The dialog's context.
     * @param options          Any options to call the waterfall dialog with.
     * @param values           A dictionary of values which will be persisted across all
     *                         waterfall steps.
     * @param index            The index of the current waterfall to execute.
     * @param reason           The reason the waterfall step is being executed.
     * @param result           Results returned by a dialog called in the previous waterfall
     *                         step.
     */
    public WaterfallStepContext(
        WaterfallDialog parentWaterfall,
        DialogContext dc,
        Object options,
        Map<String, Object> values,
        int index,
        DialogReason reason,
        Object result) {
        super(dc.getDialogs(), dc, new DialogState(dc.getStack()));
        this.parentWaterfall = parentWaterfall;
        this.nextCalled = false;
        this.setParent(dc.getParent());
        this.index = index;
        this.options = options;
        this.reason = reason;
        this.result = result;
        this.values = values;
    }

    private int index;

    private Object options;

    private DialogReason reason;

    private Object result;

    private Map<String, Object> values;

    /**
     * Gets the index of the current waterfall step being executed.
    * @return returns the index value;
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Gets any options the waterfall dialog was called with.
     * @return The options.
     */
    public Object getOptions() {
        return this.options;
    }

    /**
     * Gets the reason the waterfall step is being executed.
    * @return The DialogReason
    */
    public DialogReason getReason() {
        return this.reason;
    }

    /**
     * Gets the result from the previous waterfall step.
     *
     * The result is often the return value of a child dialog that was started in the previous step
     * of the waterfall.
     * @return the Result value.
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * Gets a dictionary of values which will be persisted across all waterfall actions.
     * @return The Dictionary of values.
     */
    public Map<String, Object> getValues() {
        return this.values;
    }

    /**
     * Skips to the next step of the waterfall.
     *
     * @param result  Optional, result to pass to the next step of the current waterfall
     *                dialog.
     * @return A CompletableFuture that represents the work queued to execute.
     *
     * In the next step of the waterfall, the {@link result} property of the waterfall step context
     * will contain the value of the .
     */

    public CompletableFuture<DialogTurnResult> next(Object result) {
        // Ensure next hasn't been called
        if (nextCalled) {
            return Async.completeExceptionally(new IllegalStateException(
                String.format("WaterfallStepContext.next(): method already called for dialog and step '%0 %1",
            parentWaterfall.getId(), index)
            ));
        }

        // Trigger next step
        nextCalled = true;
        return parentWaterfall.resumeDialog(this, DialogReason.NEXT_CALLED, result);
    }

}
