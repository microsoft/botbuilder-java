// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bot.ai.qna.dialogs.QnAMakerPrompt;

/**
 * The context associated with QnA. Used to mark if the qna response has related
 * prompts to display.
 */
public class QnAResponseContext {
    @JsonProperty("prompts")
    private QnAMakerPrompt[] prompts;

    /**
     * Gets the prompts collection of related prompts.
     *
     * @return The QnA prompts array.
     */
    public QnAMakerPrompt[] getPrompts() {
        return this.prompts;
    }

    /**
     * Sets the prompts collection of related prompts.
     *
     * @param withPrompts The QnA prompts array.
     */
    public void setPrompts(QnAMakerPrompt[] withPrompts) {
        this.prompts = withPrompts;
    }
}
