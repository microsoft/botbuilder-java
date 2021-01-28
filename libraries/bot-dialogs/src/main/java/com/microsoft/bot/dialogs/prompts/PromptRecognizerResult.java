// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

/**
 * Contains the result returned by the recognition method of a {@link Prompt{T}}
 * .
 *
 * @param <T> The type of value the prompt returns.
 */
public class PromptRecognizerResult<T> {

    private T value;
    private Boolean succeeded;
    private Boolean allowInterruption = false;

    /**
     * Initializes a new instance of the {@link PromptRecognizerResult{T}} class.
     */
    public PromptRecognizerResult() {
        succeeded = false;
    }

    /**
     * Gets the recognition value.
     *
     * @return The recognition value.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the recogntion value.
     *
     * @param value Value to set the recognition value to.
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Gets a value indicating whether the recognition attempt succeeded.
     *
     * @return True if the recognition attempt succeeded; otherwise, false.
     */
    public Boolean getSucceeded() {
        return this.succeeded;
    }

    /**
     * Sets a value indicating whether the recognition attempt succeeded.
     *
     * @param succeeded True if the recognition attempt succeeded; otherwise, false.
     */

    public void setSucceeded(Boolean succeeded) {
        this.succeeded = succeeded;
    }

    /**
     * Gets a value indicating whether flag indicating whether or not parent dialogs
     * should be allowed to interrupt the prompt.
     *
     * @return The default value is `false`.
     */
    public Boolean getAllowInterruption() {
        return this.allowInterruption;
    }

    /**
     * Sets a value indicating whether flag indicating whether or not parent dialogs
     * should be allowed to interrupt the prompt.
     *
     * @param allowInterruption The default value is `false`.
     */
    public void setAllowInterruption(Boolean allowInterruption) {
        this.allowInterruption = allowInterruption;
    }
}
