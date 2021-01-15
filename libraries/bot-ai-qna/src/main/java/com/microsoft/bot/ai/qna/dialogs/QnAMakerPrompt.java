// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.dialogs;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Prompt Object.
 */
public class QnAMakerPrompt {
    private static final Integer DEFAULT_DISPLAY_ORDER = 0;

    @JsonProperty("displayOrder")
    private Integer displayOrder = QnAMakerPrompt.DEFAULT_DISPLAY_ORDER;

    @JsonProperty("qnaId")
    private Integer qnaId;

    @JsonProperty("displayText")
    private String displayText = new String();

    @JsonProperty("qna")
    private Object qna;

    /**
     * Gets displayOrder - index of the prompt - used in ordering of the prompts.
     *
     * @return Display order.
     */
    public Integer getDisplayOrder() {
        return this.displayOrder;
    }

    /**
     * Sets displayOrder - index of the prompt - used in ordering of the prompts.
     *
     * @param withDisplayOrder Display order.
     */
    public void setDisplayOrder(Integer withDisplayOrder) {
        this.displayOrder = withDisplayOrder;
    }

    /**
     * Gets qna id corresponding to the prompt - if QnaId is present, QnADTO object
     * is ignored.
     *
     * @return QnA Id.
     */
    public Integer getQnaId() {
        return this.qnaId;
    }

    /**
     * Sets qna id corresponding to the prompt - if QnaId is present, QnADTO object
     * is ignored.
     *
     * @param withQnaId QnA Id.
     */
    public void setQnaId(Integer withQnaId) {
        this.qnaId = withQnaId;
    }

    /**
     * Gets displayText - Text displayed to represent a follow up question prompt.
     *
     * @return Display test.
     */
    public String getDisplayText() {
        return this.displayText;
    }

    /**
     * Sets displayText - Text displayed to represent a follow up question prompt.
     *
     * @param withDisplayText Display test.
     */
    public void setDisplayText(String withDisplayText) {
        this.displayText = withDisplayText;
    }

    /**
     * Gets the QnADTO returned from the API.
     *
     * @return The QnA DTO.
     */
    public Object getQna() {
        return this.qna;
    }

    /**
     * Sets the QnADTO returned from the API.
     *
     * @param withQna The QnA DTO.
     */
    public void setQna(Object withQna) {
        this.qna = withQna;
    }
}
