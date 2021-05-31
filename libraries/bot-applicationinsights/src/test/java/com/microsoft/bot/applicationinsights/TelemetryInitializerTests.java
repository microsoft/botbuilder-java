// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.applicationinsights;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import com.microsoft.bot.applicationinsights.core.TelemetryInitializerMiddleware;
import com.microsoft.bot.builder.BotTelemetryClient;
import com.microsoft.bot.builder.TelemetryLoggerMiddleware;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TelemetryInitializerTests {

    @Captor
    ArgumentCaptor<String> eventNameCaptor;

    @Captor
    ArgumentCaptor<Map<String, String>> propertiesCaptor;

	@Test
	public void telemetryInitializerMiddlewareLogActivitiesEnabled() {

		// Arrange
		BotTelemetryClient mockTelemetryClient = Mockito.mock(BotTelemetryClient.class);
		TelemetryLoggerMiddleware telemetryLoggerMiddleware = new TelemetryLoggerMiddleware(mockTelemetryClient, false);

		TestAdapter testAdapter = new TestAdapter()
            .use(new TelemetryInitializerMiddleware(telemetryLoggerMiddleware, true));

		// Act
		// Default case logging Send/Receive Activities
		new TestFlow(testAdapter, turnContext -> {
			Activity typingActivity = new Activity(ActivityTypes.TYPING);
			typingActivity.setRelatesTo(turnContext.getActivity().getRelatesTo());

			turnContext.sendActivity(typingActivity).join();
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException e) {
				// Empty error
			}
			turnContext.sendActivity(String.format("echo:%s", turnContext.getActivity().getText())).join();
			return CompletableFuture.completedFuture(null);
		})
		.send("foo")
			.assertReply(activity -> {
				Assert.assertTrue(activity.isType(ActivityTypes.TYPING));
			})
			.assertReply("echo:foo")
		.send("bar")
			.assertReply(activity -> {
				Assert.assertTrue(activity.isType(ActivityTypes.TYPING));
			})
			.assertReply("echo:bar")
		.startTest().join();

		// Verify
        verify(mockTelemetryClient, times(6)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
            );

        List<String> eventNames = eventNameCaptor.getAllValues();
        Assert.assertEquals(6, eventNames.size());

	}

	@Test
	public void telemetryInitializerMiddlewareNotLogActivitiesDisabled() {

		// Arrange
        BotTelemetryClient mockTelemetryClient = Mockito.mock(BotTelemetryClient.class);
		TelemetryLoggerMiddleware telemetryLoggerMiddleware = new TelemetryLoggerMiddleware(mockTelemetryClient, false);

		TestAdapter testAdapter = new TestAdapter()
            .use(new TelemetryInitializerMiddleware(telemetryLoggerMiddleware, false));

		// Act
		// Default case logging Send/Receive Activities
		new TestFlow(testAdapter, (turnContext) -> {
			Activity typingActivity = new Activity(ActivityTypes.TYPING);
			typingActivity.setRelatesTo(turnContext.getActivity().getRelatesTo());

			turnContext.sendActivity(typingActivity).join();
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException e) {
				// Empty error
			}
			turnContext.sendActivity(String.format("echo:%s", turnContext.getActivity().getText())).join();
			return CompletableFuture.completedFuture(null);
		})
		.send("foo")
			.assertReply(activity -> {
				Assert.assertTrue(activity.isType(ActivityTypes.TYPING));
			})
			.assertReply("echo:foo")
		.send("bar")
			.assertReply(activity -> {
				Assert.assertTrue(activity.isType(ActivityTypes.TYPING));
			})
			.assertReply("echo:bar")
		.startTest().join();

		// Verify
        verify(mockTelemetryClient, times(0)).trackEvent(
            eventNameCaptor.capture(),
            propertiesCaptor.capture()
        );
        List<String> eventNames = eventNameCaptor.getAllValues();
        Assert.assertEquals(0, eventNames.size());
	}

	@Test
    public void telemetryInitializerMiddlewareWithUndefinedContext() {
        // Arrange
        BotTelemetryClient mockTelemetryClient = Mockito.mock(BotTelemetryClient.class);
        TelemetryLoggerMiddleware telemetryLoggerMiddleware = new TelemetryLoggerMiddleware(mockTelemetryClient, false);
        TelemetryInitializerMiddleware telemetryInitializerMiddleware = new TelemetryInitializerMiddleware(telemetryLoggerMiddleware, true);
        // Assert
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            // Act
            telemetryInitializerMiddleware.onTurn(null, () -> null);
        });
    }
}
