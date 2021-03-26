// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import java.io.IOException;
import java.util.UUID;

import com.microsoft.bot.ai.qna.models.QnAMakerTraceInfo;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;

import org.junit.Assert;
import org.junit.Test;

public class QnAMakerTraceInfoTests {

    @Test
    public void qnaMakerTraceInfoSerialization() throws IOException {
        QueryResult queryResult = new QueryResult();
        queryResult.setQuestions(new String[] { "What's your name?" });
        queryResult.setAnswer("My name is Mike");
        queryResult.setScore(0.9f);
        QueryResult[] queryResults = new QueryResult[] { queryResult };

        QnAMakerTraceInfo qnaMakerTraceInfo = new QnAMakerTraceInfo();
        qnaMakerTraceInfo.setQueryResults(queryResults);
        qnaMakerTraceInfo.setKnowledgeBaseId(UUID.randomUUID().toString());
        qnaMakerTraceInfo.setScoreThreshold(0.5f);
        qnaMakerTraceInfo.setTop(1);

        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        String serialized = jacksonAdapter.serialize(qnaMakerTraceInfo);
        QnAMakerTraceInfo deserialized = jacksonAdapter.deserialize(serialized, QnAMakerTraceInfo.class);

        Assert.assertNotNull(deserialized);
        Assert.assertNotNull(deserialized.getQueryResults());
        Assert.assertNotNull(deserialized.getKnowledgeBaseId());
        Assert.assertEquals(0.5, deserialized.getScoreThreshold(), 0);
        Assert.assertEquals(1, deserialized.getTop(), 0);
        Assert.assertEquals(qnaMakerTraceInfo.getQueryResults()[0].getQuestions()[0],
                deserialized.getQueryResults()[0].getQuestions()[0]);
        Assert.assertEquals(qnaMakerTraceInfo.getQueryResults()[0].getAnswer(),
                deserialized.getQueryResults()[0].getAnswer());
        Assert.assertEquals(qnaMakerTraceInfo.getKnowledgeBaseId(), deserialized.getKnowledgeBaseId());
        Assert.assertEquals(qnaMakerTraceInfo.getScoreThreshold(), deserialized.getScoreThreshold());
        Assert.assertEquals(qnaMakerTraceInfo.getTop(), deserialized.getTop());
    }
}
