package com.microsoft.bot.dialogs;
/**
 * Class which runs the dialog system.
 */
public class DialogManager {
    /**
     * Initializes a new instance of the
     * {@link com.microsoft.bot.dialogs.DialogManager} class.
     *
     * @param rootDialog          Root dialog to use.
     * @param dialogStateProperty Alternate name for the dialogState property.
     *                            (Default is "DialogState").
     */
    public DialogManager(Dialog rootDialog, String dialogStateProperty) {
        if (dialogStateProperty == null) {
            throw new IllegalArgumentException();
        }
        if (rootDialog == null) {
            throw new IllegalArgumentException();
        }
    }
}
