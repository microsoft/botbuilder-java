package com.microsoft.bot.azure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.azure.AzureAsyncOperation;
import com.microsoft.bot.azure.serializer.AzureJacksonAdapter;
import com.microsoft.bot.azure.CloudError;
import com.microsoft.bot.rest.protocol.SerializerAdapter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class AzureAsyncOperationDeserializerTests {
    @Test
    public void DeserializeLROResult() throws IOException {
        SerializerAdapter<ObjectMapper> serializerAdapter = new AzureJacksonAdapter();

        String bodyString =
                "{"  +
                "        \"name\":\"1431219a-acad-4d70-9a17-f8b7a5a143cb\",\"status\":\"InProgress\"" +
                "}";

        AzureAsyncOperation asyncOperation = serializerAdapter.deserialize(bodyString, AzureAsyncOperation.class);

        Assert.assertEquals("InProgress", asyncOperation.status());
        Assert.assertEquals(null, asyncOperation.getError());

        Exception e = null;
         asyncOperation = null;
        bodyString =
                "{"  +
                "        \"name\":\"1431219a-acad-4d70-9a17-f8b7a5a143cb\",\"status\":\"InProgress\"," +
                "        \"error\":{" +
                "                  }" +
                "}";
        try {
            asyncOperation = serializerAdapter.deserialize(bodyString, AzureAsyncOperation.class);
        } catch (Exception ex) {
            e = ex;
        }

        Assert.assertNull(e);
        Assert.assertEquals("InProgress", asyncOperation.status());
        CloudError error = asyncOperation.getError();
        Assert.assertNotNull(error);
        Assert.assertNull(error.message());
        Assert.assertNull(error.code());

        asyncOperation = null;
        bodyString =
                "{"  +
                "        \"name\":\"1431219a-acad-4d70-9a17-f8b7a5a143cb\",\"status\":\"InProgress\"," +
                "        \"error\":{" +
                "                  \"code\":\"None\",\"message\":null,\"target\":\"e1a19fd1-8110-470a-a82f-9f789c2b2917\"" +
                "                  }" +
                "}";

        asyncOperation = serializerAdapter.deserialize(bodyString, AzureAsyncOperation.class);
        Assert.assertEquals("InProgress", asyncOperation.status());
        error = asyncOperation.getError();
        Assert.assertNotNull(error);
        Assert.assertEquals("None", error.code());
        Assert.assertNull(error.message());
        Assert.assertEquals("e1a19fd1-8110-470a-a82f-9f789c2b2917", error.target());
    }
}
