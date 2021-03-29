// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure.blobs;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.microsoft.bot.azure.AzureEmulatorUtils;
import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.StorageBaseTests;
import com.microsoft.bot.builder.StoreItem;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlobsStorageTests extends StorageBaseTests {

    @Rule
    public TestName testName = new TestName();

    private final String connectionString = "AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;DefaultEndpointsProtocol=http;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;TableEndpoint=http://127.0.0.1:10002/devstoreaccount1;";

    public String getContainerName() {
        return "blobs" + testName.getMethodName().toLowerCase().replace("_", "");
    }

    @Before
    public void beforeTest() {
        org.junit.Assume.assumeTrue(AzureEmulatorUtils.isStorageEmulatorAvailable());
    }

    @After
    public void testCleanup() {
        if (AzureEmulatorUtils.isStorageEmulatorAvailable()) {
            BlobContainerClient containerClient = new BlobContainerClientBuilder().connectionString(connectionString)
                    .containerName(getContainerName()).buildClient();

            if (containerClient.exists()) {
                containerClient.delete();
            }
        }
    }

    @Test
    public void blobStorageParamTest() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new BlobsStorage(null, getContainerName()));
        Assert.assertThrows(IllegalArgumentException.class, () -> new BlobsStorage(connectionString, null));
        Assert.assertThrows(IllegalArgumentException.class, () -> new BlobsStorage(new String(), getContainerName()));
        Assert.assertThrows(IllegalArgumentException.class, () -> new BlobsStorage(connectionString, new String()));
    }

    @Test
    public void testBlobStorageWriteRead() {
            // Arrange
            Storage storage = new BlobsStorage(connectionString, getContainerName());

            Map<String, Object> changes = new HashMap();
            changes.put("x", "hello");
            changes.put("y", "world");

            // Act
            storage.write(changes).join();
            Map<String, Object> result = storage.read(new String[] { "x", "y" }).join();

            // Assert
            Assert.assertEquals(2, result.size());
            Assert.assertEquals("hello", result.get("x"));
            Assert.assertEquals("world", result.get("y"));
    }

    @Test
    public void testBlobStorageWriteDeleteRead() {
            // Arrange
            Storage storage = new BlobsStorage(connectionString, getContainerName());

            Map<String, Object> changes = new HashMap();
            changes.put("x", "hello");
            changes.put("y", "world");

            // Act
            storage.write(changes).join();
            storage.delete(new String[] { "x" }).join();
            Map<String, Object> result = storage.read(new String[] { "x", "y" }).join();

            // Assert
            Assert.assertEquals(1, result.size());
            Assert.assertEquals("world", result.get("y"));
    }

    @Test
    public void testBlobStorageChanges() {
            // Arrange
            Storage storage = new BlobsStorage(connectionString, getContainerName());

            // Act
            Map<String, Object> changes = new HashMap();
            changes.put("a", "1.0");
            changes.put("b", "2.0");
            storage.write(changes).join();

            changes.clear();
            changes.put("c", "3.0");
            storage.write(changes).join();
            storage.delete(new String[] { "b" }).join();

            changes.clear();
            changes.put("a", "1.1");
            storage.write(changes).join();

            Map<String, Object> result = storage.read(new String[] { "a", "b", "c", "d", "e" }).join();

            // Assert
            Assert.assertEquals(2, result.size());
            Assert.assertEquals("1.1", result.get("a"));
            Assert.assertEquals("3.0", result.get("c"));
    }

    @Test
    public void testConversationStateBlobStorage() {
            // Arrange
            Storage storage = new BlobsStorage(connectionString, getContainerName());

            ConversationState conversationState = new ConversationState(storage);
            StatePropertyAccessor<Prop> propAccessor = conversationState.createProperty("prop");

            TestStorageAdapter adapter = new TestStorageAdapter();
            Activity activity = new Activity(ActivityTypes.MESSAGE);
            activity.setChannelId("123");
            ConversationAccount conversationAccount = new ConversationAccount();
            conversationAccount.setId("abc");
            activity.setConversation(conversationAccount);

            // Act
            TurnContext turnContext1 = new TurnContextImpl(adapter, activity);
            Prop propValue1 = propAccessor.get(turnContext1, Prop::new).join();
            propValue1.setX("hello");
            propValue1.setY("world");
            conversationState.saveChanges(turnContext1).join();

            TurnContext turnContext2 = new TurnContextImpl(adapter, activity);
            Prop propValue2 = propAccessor.get(turnContext2).join();

            // Assert
            Assert.assertEquals("hello", propValue2.getX());
            Assert.assertEquals("world", propValue2.getY());

            propAccessor.delete(turnContext1).join();
            conversationState.saveChanges(turnContext1).join();
    }

    @Test
    public void testConversationStateBlobStorage_TypeNameHandlingDefault() {
            Storage storage = new BlobsStorage(connectionString, getContainerName());
            testConversationStateBlobStorage_Method(storage);
    }

    @Test
    public void statePersistsThroughMultiTurn_TypeNameHandlingNone() {
            Storage storage = new BlobsStorage(connectionString, getContainerName());
            statePersistsThroughMultiTurn(storage);
    }

    private void testConversationStateBlobStorage_Method(Storage blobs) {
            // Arrange
            ConversationState conversationState = new ConversationState(blobs);
            StatePropertyAccessor<Prop> propAccessor = conversationState.createProperty("prop");
            TestStorageAdapter adapter = new TestStorageAdapter();
            Activity activity = new Activity(ActivityTypes.MESSAGE);
            activity.setChannelId("123");
            ConversationAccount conversationAccount = new ConversationAccount();
            conversationAccount.setId("abc");
            activity.setConversation(conversationAccount);

            // Act
            TurnContext turnContext1 = new TurnContextImpl(adapter, activity);
            Prop propValue1 = propAccessor.get(turnContext1, Prop::new).join();
            propValue1.setX("hello");
            propValue1.setY("world");
            conversationState.saveChanges(turnContext1).join();

            TurnContext turnContext2 = new TurnContextImpl(adapter, activity);
            Prop propValue2 = propAccessor.get(turnContext2).join();

            // Assert
            Assert.assertEquals("hello", propValue2.getX());
            Assert.assertEquals("world", propValue2.getY());
    }

    private class TestStorageAdapter extends BotAdapter {

        @Override
        public CompletableFuture<ResourceResponse[]> sendActivities(TurnContext context, List<Activity> activities) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<ResourceResponse> updateActivity(TurnContext context, Activity activity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<Void> deleteActivity(TurnContext context, ConversationReference reference) {
            throw new UnsupportedOperationException();
        }
    }

    private static class Prop {
        private String X;
        private String Y;
        StoreItem storeItem;

        public String getX() {
            return X;
        }

        public void setX(String x) {
            X = x;
        }

        public String getY() {
            return Y;
        }

        public void setY(String y) {
            Y = y;
        }

        public StoreItem getStoreItem() {
            return storeItem;
        }

        public void setStoreItem(StoreItem storeItem) {
            this.storeItem = storeItem;
        }
    }
}
