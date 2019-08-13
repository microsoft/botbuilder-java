package com.microsoft.bot.builder.dialogs;

// TODO: daveta remove this - not sure where this came from
/**
 * Interface for all Dialog objects that can be added to a `DialogSet`. The dialog should generally
 * be a singleton and added to a dialog set using `DialogSet.add()` at which point it will be 
 * assigned a unique ID.
 */
public interface IDialog
{
    /**
     * Method called when a new dialog has been pushed onto the stack and is being activated.
     * @param dc The dialog context for the current turn of conversation.
     * @param dialogArgs (Optional) arguments that were passed to the dialog during `begin()` call that started the instance.  
     */
    //CompleteableFuture DialogBegin(DialogContext dc, IDictionary<string, object> dialogArgs = null);
    //CompleteableFuture DialogBegin(DialogContext dc, HashMap<string, object> dialogArgs);
    //CompleteableFuture DialogBegin(DialogContext dc);
}
