// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.utils;

import com.microsoft.bot.ai.qna.models.QueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Active learning helper class.
 */
public final class ActiveLearningUtils {
    /**
     * Previous Low Score Variation Multiplier.ActiveLearningUtils.
     */
    private static final Float PREVIOUS_LOW_SCORE_VARIATION_MULTIPLIER = 0.7f;

    /**
     * Max Low Score Variation Multiplier.
     */
    private static final Float MAX_LOW_SCORE_VARIATION_MULTIPLIER = 1.0f;

    private static final Integer PERCENTAGE_DIVISOR = 100;

    private static final Float MAXIMUM_SCORE_VARIATION = 95.0F;

    private static final Float MINIMUM_SCORE_VARIATION = 20.0F;

    private static Float maximumScoreForLowScoreVariation = MAXIMUM_SCORE_VARIATION;

    private static Float minimumScoreForLowScoreVariation = MINIMUM_SCORE_VARIATION;

    private ActiveLearningUtils() {
    }

    /**
     * Gets maximum Score For Low Score Variation.
     *
     * @return Maximum Score For Low Score Variation.
     */
    public static Float getMaximumScoreForLowScoreVariation() {
        return ActiveLearningUtils.maximumScoreForLowScoreVariation;
    }

    /**
     * Sets maximum Score For Low Score Variation.
     *
     * @param withMaximumScoreForLowScoreVariation Maximum Score For Low Score
     *                                             Variation.
     */
    public static void setMaximumScoreForLowScoreVariation(Float withMaximumScoreForLowScoreVariation) {
        ActiveLearningUtils.maximumScoreForLowScoreVariation = withMaximumScoreForLowScoreVariation;
    }

    /**
     * Gets minimum Score For Low Score Variation.
     *
     * @return Minimum Score For Low Score Variation.
     */
    public static Float getMinimumScoreForLowScoreVariation() {
        return ActiveLearningUtils.minimumScoreForLowScoreVariation;
    }

    /**
     * Sets minimum Score For Low Score Variation.
     *
     * @param withMinimumScoreForLowScoreVariation Minimum Score For Low Score
     *                                             Variation.
     */
    public static void setMinimumScoreForLowScoreVariation(Float withMinimumScoreForLowScoreVariation) {
        ActiveLearningUtils.minimumScoreForLowScoreVariation = withMinimumScoreForLowScoreVariation;
    }

    /**
     * Returns list of qnaSearch results which have low score variation.
     *
     * @param qnaSearchResults List of QnaSearch results.
     * @return List of filtered qnaSearch results.
     */
    public static List<QueryResult> getLowScoreVariation(List<QueryResult> qnaSearchResults) {
        List<QueryResult> filteredQnaSearchResult = new ArrayList<QueryResult>();

        if (qnaSearchResults == null || qnaSearchResults.isEmpty()) {
            return filteredQnaSearchResult;
        }

        if (qnaSearchResults.size() == 1) {
            return qnaSearchResults;
        }

        Float topAnswerScore = qnaSearchResults.get(0).getScore() * PERCENTAGE_DIVISOR;
        if (topAnswerScore > ActiveLearningUtils.maximumScoreForLowScoreVariation) {
            filteredQnaSearchResult.add(qnaSearchResults.get(0));
            return filteredQnaSearchResult;
        }

        Float prevScore = topAnswerScore;

        if (topAnswerScore > ActiveLearningUtils.minimumScoreForLowScoreVariation) {
            filteredQnaSearchResult.add(qnaSearchResults.get(0));

            for (int i = 1; i < qnaSearchResults.size(); i++) {
                if (
                    ActiveLearningUtils.includeForClustering(
                        prevScore,
                        qnaSearchResults.get(i).getScore() * PERCENTAGE_DIVISOR,
                        ActiveLearningUtils.PREVIOUS_LOW_SCORE_VARIATION_MULTIPLIER
                    ) && ActiveLearningUtils.includeForClustering(
                        topAnswerScore,
                        qnaSearchResults.get(i).getScore() * PERCENTAGE_DIVISOR,
                        ActiveLearningUtils.MAX_LOW_SCORE_VARIATION_MULTIPLIER
                    )
                ) {
                    prevScore = qnaSearchResults.get(i).getScore() * PERCENTAGE_DIVISOR;
                    filteredQnaSearchResult.add(qnaSearchResults.get(i));
                }
            }
        }

        return filteredQnaSearchResult;
    }

    private static Boolean includeForClustering(Float prevScore, Float currentScore, Float multiplier) {
        return (prevScore - currentScore) < (multiplier * Math.sqrt(prevScore));
    }
}
