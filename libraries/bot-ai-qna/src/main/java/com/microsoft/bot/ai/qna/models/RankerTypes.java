// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.models;

/**
 * Enumeration of types of ranking.
 */
public final class RankerTypes {
    /**
     * Default Ranker Behaviour. i.e. Ranking based on Questions and Answer.
     */
    public static final String DEFAULT_RANKER_TYPE = "Default";

    /**
     * Ranker based on question Only.
     */
    public static final String QUESTION_ONLY = "QuestionOnly";

    /**
     * Ranker based on Autosuggest for question field Only.
     */
    public static final String AUTO_SUGGEST_QUESTION = "AutoSuggestQuestion";

    private RankerTypes() {
    }
}
