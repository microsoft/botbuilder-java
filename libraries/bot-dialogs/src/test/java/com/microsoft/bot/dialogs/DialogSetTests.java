// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.CompletionException;

import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.NullBotTelemetryClient;
import com.microsoft.bot.builder.Severity;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TestUtilities;
import com.microsoft.bot.builder.TurnContext;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;

public class DialogSetTests {

    @Test
    public void DialogSet_ConstructorValid() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogStateProperty = convoState.createProperty("dialogState");
        new DialogSet(dialogStateProperty);
    }

    @Test
    public void DialogSet_ConstructorNullProperty() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new DialogSet(null));
    }

    @Test
    public void DialogSet_CreateContext() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogStateProperty = convoState.createProperty("dialogState");
        DialogSet ds = new DialogSet(dialogStateProperty);
        TurnContext context = TestUtilities.createEmptyContext();
        ds.createContext(context);
    }

    @Test
    public void DialogSet_NullCreateContext() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            try {
                ConversationState convoState = new ConversationState(new MemoryStorage());
                StatePropertyAccessor<DialogState> dialogStateProperty = convoState.createProperty("dialogState");
                DialogSet ds = new DialogSet(dialogStateProperty);
                ds.createContext(null).join();
                } catch (CompletionException ex) {
                    throw ex.getCause();
                }
        });
    }

    @Test
    public void DialogSet_AddWorks() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogStateProperty = convoState.createProperty("dialogState");
        WaterfallDialog dialogA = new WaterfallDialog("A", null);
        WaterfallDialog dialogB = new WaterfallDialog("B", null);
        DialogSet ds = new DialogSet(dialogStateProperty).add(dialogA).add(dialogB);

        Assert.assertNotNull(ds.find("A"));
        Assert.assertNotNull(ds.find("B"));
        Assert.assertNull(ds.find("C"));
    }

    @Test
    public void DialogSet_GetVersion() {
        DialogSet ds = new DialogSet();
        String version1 = ds.getVersion();
        Assert.assertNotNull(version1);

        DialogSet ds2 = new DialogSet();
        String version2 = ds.getVersion();
        Assert.assertNotNull(version2);
        Assert.assertEquals(version1, version2);

        ds2.add(new LamdbaDialog("A", null));
        String version3 = ds2.getVersion();
        Assert.assertNotNull(version3);
        Assert.assertNotEquals(version2, version3);

        String version4 = ds2.getVersion();
        Assert.assertNotNull(version3);
        Assert.assertEquals(version3, version4);

        DialogSet ds3 = new DialogSet().add(new LamdbaDialog("A", null));

        String version5 = ds3.getVersion();
        Assert.assertNotNull(version5);
        Assert.assertEquals(version5, version4);
    }

    @Test
    public void DialogSet_TelemetrySet() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogStateProperty = convoState.createProperty("dialogState");
        DialogSet ds = new DialogSet(dialogStateProperty).add(new WaterfallDialog("A", null))
                .add(new WaterfallDialog("B", null));
        Assert.assertEquals(NullBotTelemetryClient.class.getSimpleName(),
                ds.find("A").getTelemetryClient().getClass().getSimpleName());
        Assert.assertEquals(NullBotTelemetryClient.class.getSimpleName(),
                ds.find("B").getTelemetryClient().getClass().getSimpleName());

        MyBotTelemetryClient botTelemetryClient = new MyBotTelemetryClient();
        ds.setTelemetryClient(botTelemetryClient);

        Assert.assertEquals(MyBotTelemetryClient.class.getSimpleName(),
                ds.find("A").getTelemetryClient().getClass().getSimpleName());
        Assert.assertEquals(MyBotTelemetryClient.class.getSimpleName(),
                ds.find("B").getTelemetryClient().getClass().getSimpleName());
    }

    @Test
    public void DialogSet_NullTelemetrySet() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogStateProperty = convoState.createProperty("dialogState");
        DialogSet ds = new DialogSet(dialogStateProperty).add(new WaterfallDialog("A", null))
                .add(new WaterfallDialog("B", null));

        ds.setTelemetryClient(null);
        Assert.assertEquals(NullBotTelemetryClient.class.getSimpleName(),
                            ds.find("A").getTelemetryClient().getClass().getSimpleName());
        Assert.assertEquals(NullBotTelemetryClient.class.getSimpleName(),
                            ds.find("B").getTelemetryClient().getClass().getSimpleName());
    }

    @Test
    public void DialogSet_AddTelemetrySet() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogStateProperty = convoState.createProperty("dialogState");
        DialogSet ds = new DialogSet(dialogStateProperty).add(new WaterfallDialog("A", null))
                .add(new WaterfallDialog("B", null));

        ds.setTelemetryClient(new MyBotTelemetryClient());
        ds.add(new WaterfallDialog("C", null));

        Assert.assertEquals(MyBotTelemetryClient.class.getSimpleName(),
                ds.find("C").getTelemetryClient().getClass().getSimpleName());
    }

    @Test
    public void DialogSet_HeterogeneousLoggers() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogStateProperty = convoState.createProperty("dialogState");
        DialogSet ds = new DialogSet(dialogStateProperty)
            .add(new WaterfallDialog("A", null))
            .add(new WaterfallDialog("B", null))
            .add(new WaterfallDialog("C", null));

        // Make sure we can (after Adding) the TelemetryClient and "sticks"
        ds.find("C").setTelemetryClient(new MyBotTelemetryClient());

        Assert.assertEquals(NullBotTelemetryClient.class.getSimpleName(),
                            ds.find("A").getTelemetryClient().getClass().getSimpleName());
        Assert.assertEquals(NullBotTelemetryClient.class.getSimpleName(),
                            ds.find("B").getTelemetryClient().getClass().getSimpleName());
        Assert.assertEquals(MyBotTelemetryClient.class.getSimpleName(),
                            ds.find("C").getTelemetryClient().getClass().getSimpleName());
    }

    private final class MyBotTelemetryClient implements BotTelemetryClient {
        private MyBotTelemetryClient() {
        }

        @Override
        public void trackAvailability(String name, OffsetDateTime timeStamp, Duration duration, String runLocation,
                boolean success, String message, Map<String, String> properties, Map<String, Double> metrics) {
            throw new NotImplementedException("trackAvailability is not implemented");
        }

        @Override
        public void trackDependency(String dependencyTypeName, String target, String dependencyName, String data,
                OffsetDateTime startTime, Duration duration, String resultCode, boolean success) {
            throw new NotImplementedException("trackDependency is not implemented");
        }

        @Override
        public void trackEvent(String eventName, Map<String, String> properties, Map<String, Double> metrics) {
            throw new NotImplementedException("trackEvent is not implemented");
        }

        @Override
        public void trackException(Exception exception, Map<String, String> properties, Map<String, Double> metrics) {
            throw new NotImplementedException("trackException is not implemented");
        }

        @Override
        public void trackTrace(String message, Severity severityLevel, Map<String, String> properties) {
            throw new NotImplementedException("trackTrace is not implemented");
        }

        @Override
        public void trackDialogView(String dialogName, Map<String, String> properties, Map<String, Double> metrics) {
            throw new NotImplementedException("trackDialogView is not implemented");
        }

        @Override
        public void flush() {
            throw new NotImplementedException("flush is not implemented");
        }
    }
}

