package com.microsoft.bot.dialogs;

/**
 * Exception used to report issues during the invoke method of the {@link SkillDialog} class.
 */
public class SkillInvokeException extends RuntimeException {

    /**
     * Serial Version for class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct with exception.
     *
     * @param t The cause.
     */
    public SkillInvokeException(Throwable t) {
        super(t);
    }

    /**
     * Construct with message.
     *
     * @param message The exception message.
     */
    public SkillInvokeException(String message) {
        super(message);
    }

    /**
     * Construct with caught exception and message.
     *
     * @param message The message.
     * @param t       The caught exception.
     */
    public SkillInvokeException(String message, Throwable t) {
        super(message, t);
    }

}
