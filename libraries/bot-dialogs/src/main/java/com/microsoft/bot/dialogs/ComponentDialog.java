package com.microsoft.bot.dialogs;

import java.util.concurrent.CompletableFuture;

/**
 * A Dialog that is composed of other dialogs.
 */
public class ComponentDialog extends DialogContainer {

    /// <summary>
    /// The id for the persisted dialog state.
    /// </summary>
    public static final String PERSISTEDDIALOGSTATE = "dialogs";

    //private Boolean initialized;

    @Override
    /**
     *
     */
    public DialogContext createChildContext(DialogContext dc) {

        return null;
    }

    @Override
    /**
     *
     */
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {

        return null;
    }

        //private Boolean initialized;

}
