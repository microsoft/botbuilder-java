// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.dialogs;

import com.microsoft.bot.ai.qna.QnADialogResponseOptions;
import com.microsoft.bot.ai.qna.QnAMakerOptions;

/**
 * Defines Dialog Options for QnAMakerDialog.
 */
public class QnAMakerDialogOptions {
    private QnAMakerOptions qnaMakerOptions;

    private QnADialogResponseOptions responseOptions;

    /**
     * Gets the options for the QnAMaker service.
     *
     * @return The options for the QnAMaker service.
     */
    public QnAMakerOptions getQnAMakerOptions() {
        return this.qnaMakerOptions;
    }

    /**
     * Sets the options for the QnAMaker service.
     *
     * @param withQnAMakerOptions The options for the QnAMaker service.
     */
    public void setQnAMakerOptions(QnAMakerOptions withQnAMakerOptions) {
        this.qnaMakerOptions = withQnAMakerOptions;
    }

    /**
     * Gets the response options for the QnAMakerDialog.
     *
     * @return The response options for the QnAMakerDialog.
     */
    public QnADialogResponseOptions getResponseOptions() {
        return this.responseOptions;
    }

    /**
     * Sets the response options for the QnAMakerDialog.
     *
     * @param withResponseOptions The response options for the QnAMakerDialog.
     */
    public void setResponseOptions(QnADialogResponseOptions withResponseOptions) {
        this.responseOptions = withResponseOptions;
    }
}
