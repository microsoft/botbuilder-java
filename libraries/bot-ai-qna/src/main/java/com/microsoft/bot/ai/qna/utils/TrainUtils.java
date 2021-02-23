// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.utils;

import com.microsoft.bot.ai.qna.QnAMakerEndpoint;
import com.microsoft.bot.ai.qna.models.FeedbackRecords;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Helper class for train API.
 */
public class TrainUtils {
    private QnAMakerEndpoint endpoint;

    /**
     * Initializes a new instance of the {@link TrainUtils} class.
     *
     * @param withEndpoint QnA Maker endpoint details.
     */
    public TrainUtils(QnAMakerEndpoint withEndpoint) {
        this.endpoint = withEndpoint;
    }

    /**
     * Train API to provide feedback.
     *
     * @param feedbackRecords Feedback record list.
     * @return A Task representing the asynchronous operation.
     * @throws IOException IOException
     */
    public CompletableFuture<Void> callTrain(FeedbackRecords feedbackRecords) throws IOException {
        if (feedbackRecords == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException("feedbackRecords: Feedback records cannot be null.")
            );
        }

        if (feedbackRecords.getRecords() == null || feedbackRecords.getRecords().length == 0) {
            return CompletableFuture.completedFuture(null);
        }

        // Call train
        return this.queryTrain(feedbackRecords);
    }

    private CompletableFuture<Void> queryTrain(FeedbackRecords feedbackRecords) throws IOException {
        String requestUrl = String
            .format("%1$s/knowledgebases/%2$s/train", this.endpoint.getHost(), this.endpoint.getKnowledgeBaseId());

        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        String jsonRequest = jacksonAdapter.serialize(feedbackRecords);

        HttpRequestUtils httpRequestHelper = new HttpRequestUtils();
        return httpRequestHelper.executeHttpRequest(requestUrl, jsonRequest, this.endpoint).thenApply(result -> null);
    }
}
