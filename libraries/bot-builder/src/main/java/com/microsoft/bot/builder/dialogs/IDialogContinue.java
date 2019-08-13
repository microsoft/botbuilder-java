// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.builder.dialogs;


import java.util.concurrent.CompletableFuture;

/**
 * Interface Dialog objects that can be continued.
 */
public interface IDialogContinue extends IDialog
{
    /**
     * Method called when an instance of the dialog is the "current" dialog and the 
     * user replies with a new activity. The dialog will generally continue to receive the users 
     * replies until it calls either `DialogSet.end()` or `DialogSet.begin()`.
     * If this method is NOT implemented then the dialog will automatically be ended when the user replies.
     * @param dc The dialog context for the current turn of conversation.
     */
    CompletableFuture DialogContinue(DialogContext dc);
}
