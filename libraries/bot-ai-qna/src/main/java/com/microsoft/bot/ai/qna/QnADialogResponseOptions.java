// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import com.microsoft.bot.schema.Activity;

/**
 * QnA dialog response options class.
 */
public class QnADialogResponseOptions {
    private String activeLearningCardTitle;
    private String cardNoMatchText;
    private Activity noAnswer;
    private Activity cardNoMatchResponse;

    /**
     * Gets the active learning card title.
     *
     * @return The active learning card title
     */
    public String getActiveLearningCardTitle() {
        return activeLearningCardTitle;
    }

    /**
     * Sets the active learning card title.
     *
     * @param withActiveLearningCardTitle The active learning card title.
     */
    public void setActiveLearningCardTitle(String withActiveLearningCardTitle) {
        this.activeLearningCardTitle = withActiveLearningCardTitle;
    }

    /**
     * Gets the card no match text.
     *
     * @return The card no match text.
     */
    public String getCardNoMatchText() {
        return cardNoMatchText;
    }

    /**
     * Sets the card no match text.
     *
     * @param withCardNoMatchText The card no match text.
     */
    public void setCardNoMatchText(String withCardNoMatchText) {
        this.cardNoMatchText = withCardNoMatchText;
    }

    /**
     * Gets the no answer activity.
     *
     * @return The no answer activity.
     */
    public Activity getNoAnswer() {
        return noAnswer;
    }

    /**
     * Sets the no answer activity.
     *
     * @param withNoAnswer The no answer activity.
     */
    public void setNoAnswer(Activity withNoAnswer) {
        this.noAnswer = withNoAnswer;
    }

    /**
     * Gets the card no match response.
     *
     * @return The card no match response.
     */
    public Activity getCardNoMatchResponse() {
        return cardNoMatchResponse;
    }

    /**
     * Sets the card no match response.
     *
     * @param withCardNoMatchResponse The card no match response.
     */
    public void setCardNoMatchResponse(Activity withCardNoMatchResponse) {
        this.cardNoMatchResponse = withCardNoMatchResponse;
    }
}
