package com.microsoft.bot.dialogs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains state information for the dialog stack.
 */
public class DialogState {
    @JsonProperty(value = "dialogStack")
    private List<DialogInstance> dialogStack = new ArrayList<DialogInstance>();

    /**
     * Initializes a new instance of the class with an empty stack.
     */
    public DialogState() {
        this(null);
    }

    /**
     * Initializes a new instance of the class.
     * @param withDialogStack The state information to initialize the stack with.
     */
    public DialogState(List<DialogInstance> withDialogStack) {
        dialogStack = withDialogStack != null ? withDialogStack : new ArrayList<DialogInstance>();
    }

    /**
     * Gets the state information for a dialog stack.
     * @return State information for a dialog stack.
     */
    public List<DialogInstance> getDialogStack() {
        return dialogStack;
    }

    /**
     * Sets the state information for a dialog stack.
     * @param withDialogStack State information for a dialog stack.
     */
    public void setDialogStack(List<DialogInstance> withDialogStack) {
        dialogStack = withDialogStack;
    }
}
