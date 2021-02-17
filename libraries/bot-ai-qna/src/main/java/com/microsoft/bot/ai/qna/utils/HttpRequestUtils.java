// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.bot.ai.qna.QnAMakerEndpoint;
import com.microsoft.bot.connector.UserAgent;

import org.slf4j.LoggerFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Helper for HTTP requests.
 */
public class HttpRequestUtils {
    /**
     * Execute Http request.
     *
     * @param requestUrl  Http request url.
     * @param payloadBody Http request body.
     * @param endpoint    QnA Maker endpoint details.
     * @return Returns http response object.
     */
    public CompletableFuture<Response> executeHttpRequest(String requestUrl, String payloadBody,
            QnAMakerEndpoint endpoint) {
        if (requestUrl == null) {
            throw new IllegalArgumentException("requestUrl: Request url can not be null.");
        }

        if (payloadBody == null) {
            throw new IllegalArgumentException("payloadBody: Payload body can not be null.");
        }

        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint");
        }

        return CompletableFuture.supplyAsync(() -> {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), payloadBody);
            OkHttpClient client = new OkHttpClient();
            String endpointKey = String.format("%s", endpoint.getEndpointKey());

            Request request = new Request.Builder().url(requestUrl).header("Authorization", String.format("EndpointKey %s", endpointKey))
                    .header("Ocp-Apim-Subscription-Key", endpointKey).header("User-Agent", UserAgent.value())
                    .post(requestBody).build();

            Response response;
            try {
                response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    String message = new StringBuilder("The call to the translation service returned HTTP status code ")
                            .append(response.code()).append(".").toString();
                    throw new Exception(message);
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(HttpRequestUtils.class).error("findPackages", e);
                throw new CompletionException(e);
            }

            return response;
        });
    }
}
