// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.utils;

import com.microsoft.bot.connector.Async;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.ai.qna.QnAMakerEndpoint;
import com.microsoft.bot.connector.UserAgent;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.LoggerFactory;

/**
 * Helper for HTTP requests.
 */
public class HttpRequestUtils {
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * Execute Http request.
     *
     * @param requestUrl  Http request url.
     * @param payloadBody Http request body.
     * @param endpoint    QnA Maker endpoint details.
     * @return Returns http response object.
     */
    public CompletableFuture<JsonNode> executeHttpRequest(
        String requestUrl,
        String payloadBody,
        QnAMakerEndpoint endpoint
    ) {
        if (requestUrl == null) {
            return Async
                .completeExceptionally(new IllegalArgumentException("requestUrl: Request url can not be null."));
        }

        if (payloadBody == null) {
            return Async
                .completeExceptionally(new IllegalArgumentException("payloadBody: Payload body can not be null."));
        }

        if (endpoint == null) {
            return Async.completeExceptionally(new IllegalArgumentException("endpoint"));
        }

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String endpointKey = endpoint.getEndpointKey();
        Response response;
        JsonNode qnaResponse = null;
        try {
            Request request = buildRequest(requestUrl, endpointKey, buildRequestBody(payloadBody));
            response = this.httpClient.newCall(request).execute();
            qnaResponse = mapper.readTree(response.body().string());
            if (!response.isSuccessful()) {
                String message = "Unexpected code " + response.code();
                return Async.completeExceptionally(new Exception(message));
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(HttpRequestUtils.class).error("findPackages", e);
            return Async.completeExceptionally(e);
        }

        return CompletableFuture.completedFuture(qnaResponse);
    }

    private Request buildRequest(String requestUrl, String endpointKey, RequestBody body) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(requestUrl).newBuilder();
        Request.Builder requestBuilder = new Request.Builder().url(httpBuilder.build())
            .addHeader("Authorization", String.format("EndpointKey %s", endpointKey))
            .addHeader("Ocp-Apim-Subscription-Key", endpointKey)
            .addHeader("User-Agent", UserAgent.value())
            .post(body);
        return requestBuilder.build();
    }

    private RequestBody buildRequestBody(String payloadBody) throws JsonProcessingException {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payloadBody);
    }
}
