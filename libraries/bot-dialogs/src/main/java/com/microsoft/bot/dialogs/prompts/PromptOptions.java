// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import com.microsoft.bot.dialogs.choices.Choice;
import com.microsoft.bot.dialogs.choices.ListStyle;
import com.microsoft.bot.schema.Activity;
import java.util.List;


/**
 * Contains settings to pass to a {@link com.} when the prompt is started.
 */
public class PromptOptions {

    private Activity prompt;

    private Activity retryPrompt;

    private List<Choice> choices;

    private ListStyle style;

    private Object validations;


    /**
     * @return Activity
     */
    public Activity getPrompt() {
        return this.prompt;
    }


    /**
     * @param withPrompt value to set the Prompt property to
     */
    public void setPrompt(Activity withPrompt) {
        this.prompt = withPrompt;
    }


    /**
     * @return Activity
     */
    public Activity getRetryPrompt() {
        return this.retryPrompt;
    }


    /**
     * @param withRetryPrompt value to set the Retry property to
     */
    public void setRetryPrompt(Activity withRetryPrompt) {
        this.retryPrompt = withRetryPrompt;
    }


    /**
     * @return List<Choice>
     */
    public List<Choice> getChoices() {
        return this.choices;
    }


    /**
     * @param withChoices value to set the Choices property to
     */
    public void setChoices(List<Choice> withChoices) {
        this.choices = withChoices;
    }


    /**
     * @return ListStyle
     */
    public ListStyle getStyle() {
        return this.style;
    }


    /**
     * @param withStyle value to set the Style property to
     */
    public void setStyle(ListStyle withStyle) {
        this.style = withStyle;
    }


    /**
     * @return Object
     */
    public Object getValidations() {
        return this.validations;
    }


    /**
     * @param withValidations value to set the Validations property to
     */
    public void setValidations(Object withValidations) {
        this.validations = withValidations;
    }

}
