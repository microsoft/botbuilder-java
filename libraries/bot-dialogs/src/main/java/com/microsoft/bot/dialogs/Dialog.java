// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.dialogs;


import com.microsoft.bot.builder.BotAssert;
import com.microsoft.bot.builder.TurnContext;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for controls
 */
public abstract class Dialog
{
    /**
     * Starts the dialog. Depending on the dialog, its possible for the dialog to finish
     * immediately so it's advised to check the completion Object returned by `begin()` and ensure
     * that the dialog is still active before continuing.
     * @param context Context for the current turn of the conversation with the user.
     * @param state A state Object that the dialog will use to persist its current state. This should be an empty Object which the dialog will populate. The bot should persist this with its other conversation state for as long as the dialog is still active.
     * @param options (Optional) additional options supported by the dialog.
     * @return DialogCompletion result
     */
    public CompletableFuture<DialogCompletion> Begin(TurnContext context, HashMap<String, Object> state)
    {
        return Begin(context, state, null);
    }
    public CompletableFuture<DialogCompletion> Begin(TurnContext context, HashMap<String, Object> state, HashMap<String, Object> options)
    {
        BotAssert.ContextNotNull(context);
        if (state == null)
            throw new NullPointerException("HashMap<String, Object> state");

        // Create empty dialog set and ourselves to it
        // TODO: Complete
        //DialogSet dialogs = new DialogSet();
        //dialogs.Add("dialog", (IDialog)this);

        // Start the control
        //HashMap<String, Object> result = null;

        /*
        // TODO Complete

        await dc.Begin("dialog", options);
        */
        CompletableFuture<DialogCompletion> result = null;
        /*
        if (dc.ActiveDialog != null) {
            result = new DialogCompletion();
            result.setIsActive(true);
            result.setIsCompleted(false);
        }
        else{
            result = new DialogCompletion();
            result.setIsActive(false);
            result.setIsCompleted(true);
            result.setResult(result);
        }
        */
        return result;
    }

    /**
     * Passes a users reply to the dialog for further processing.The bot should keep calling
     * 'continue()' for future turns until the dialog returns a completion Object with
     * 'isCompleted == true'. To cancel or interrupt the prompt simply delete the `state` Object
     * being persisted.
     * @param context Context for the current turn of the conversation with the user.
     * @param state A state Object that was previously initialized by a call to [begin()](#begin).
     * @return DialogCompletion result
     */
    public CompletableFuture<DialogCompletion> Continue(TurnContext context, HashMap<String, Object> state)
    {
        BotAssert.ContextNotNull(context);
        if (state == null)
            throw new NullPointerException("HashMap<String, Object>");

        // Create empty dialog set and ourselves to it
        // TODO: daveta
        //DialogSet dialogs = new DialogSet();
        //dialogs.Add("dialog", (IDialog)this);

        // Continue the dialog
        //HashMap<String, Object> result = null;
        CompletableFuture<DialogCompletion> result = null;
        /*
        TODO: daveta
        var dc = new DialogContext(dialogs, context, state, (r) => { result = r; });
        if (dc.ActiveDialog != null)
        {
            await dc.Continue();
            return dc.ActiveDialog != null
                    ?
                new DialogCompletion { IsActive = true, IsCompleted = false }
                    :
                new DialogCompletion { IsActive = false, IsCompleted = true, Result = result };
        }
        else
        {
            return new DialogCompletion { IsActive = false, IsCompleted = false };
        }
        */

        return result;

    }
}

