// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines an endpoint used to connect to a QnA Maker Knowledge base.
 */
public class QnAMakerEndpoint {
    @JsonProperty("knowledgeBaseId")
    private String knowledgeBaseId;

    @JsonProperty("endpointKey")
    private String endpointKey;

    @JsonProperty("host")
    private String host;

    /**
     * Gets the knowledge base ID.
     *
     * @return The knowledge base ID.
     */
    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    /**
     * Sets the knowledge base ID.
     *
     * @param withKnowledgeBaseId The knowledge base ID.
     */
    public void setKnowledgeBaseId(String withKnowledgeBaseId) {
        this.knowledgeBaseId = withKnowledgeBaseId;
    }

    /**
     * Gets the endpoint key for the knowledge base.
     *
     * @return The endpoint key for the knowledge base.
     */
    public String getEndpointKey() {
        return endpointKey;
    }

    /**
     * Sets the endpoint key for the knowledge base.
     *
     * @param withEndpointKey The endpoint key for the knowledge base.
     */
    public void setEndpointKey(String withEndpointKey) {
        this.endpointKey = withEndpointKey;
    }

    /**
     * Gets the host path. For example
     * "https://westus.api.cognitive.microsoft.com/qnamaker/v2.0".
     *
     * @return The host path.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host path. For example
     * "https://westus.api.cognitive.microsoft.com/qnamaker/v2.0".
     *
     * @param withHost The host path.
     */
    public void setHost(String withHost) {
        this.host = withHost;
    }

    /**
     * Initializes a new instance of the {@link QnAMakerEndpoint} class.
     */
    public QnAMakerEndpoint() {

    }
}
