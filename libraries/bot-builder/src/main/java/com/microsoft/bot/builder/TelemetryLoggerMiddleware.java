// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ResultPair;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.teams.TeamsChannelData;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Middleware for logging incoming, outgoing, updated or deleted Activity
 * messages. Uses the {@link BotTelemetryClient} interface.
 */
public class TelemetryLoggerMiddleware implements Middleware {
    /**
     * Indicates whether determines whether to log personal information that came
     * from the user.
     */
    private boolean logPersonalInformation;

    /**
     * The currently configured {@link BotTelemetryClient} that logs the QnaMessage
     * event.
     */
    private BotTelemetryClient telemetryClient;

    /**
     * Initializes a new instance of the class.
     *
     * @param withTelemetryClient        The IBotTelemetryClient implementation used
     *                                   for registering telemetry events.
     * @param withLogPersonalInformation TRUE to include personally identifiable
     *                                   information.
     */
    public TelemetryLoggerMiddleware(
        BotTelemetryClient withTelemetryClient,
        boolean withLogPersonalInformation
    ) {
        telemetryClient =
            withTelemetryClient == null ? new NullBotTelemetryClient() : withTelemetryClient;
        logPersonalInformation = withLogPersonalInformation;
    }

    /**
     * Gets the currently configured BotTelemetryClient that logs the event.
     *
     * @return The {@link BotTelemetryClient} being used to log events.
     */
    public BotTelemetryClient getTelemetryClient() {
        return telemetryClient;
    }

    /**
     * Logs events based on incoming and outgoing activities using the
     * {@link BotTelemetryClient} interface.
     *
     * @param context The context object for this turn.
     * @param next    The delegate to call to continue the bot middleware pipeline.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
        if (context == null) {
            return Async.completeExceptionally(new IllegalArgumentException("TurnContext"));
        }

        // log incoming activity at beginning of turn
        return onReceiveActivity(context.getActivity()).thenCompose(receiveResult -> {
            // hook up onSend pipeline
            context.onSendActivities(
                (sendContext, sendActivities, sendNext) -> sendNext.get().thenApply(responses -> {
                    for (Activity sendActivity : sendActivities) {
                        onSendActivity(sendActivity);
                    }

                    return responses;
                })
            );

            // hook up update activity pipeline
            // @formatter:off
            context.onUpdateActivity(
                (updateContext, updateActivity, updateNext) -> updateNext.get()
                    .thenCombine(
                        onUpdateActivity(updateActivity), (resourceResponse, updateResult)
                            -> resourceResponse
                    )
            );
            // @formatter:off

            // hook up delete activity pipeline
            context.onDeleteActivity(
                (deleteContext, deleteReference, deleteNext) -> deleteNext.get()
                    .thenCompose(nextResult -> {
                        Activity deleteActivity = new Activity(ActivityTypes.MESSAGE_DELETE);
                        deleteActivity.setId(deleteReference.getActivityId());
                        deleteActivity.applyConversationReference(deleteReference, false);

                        return onDeleteActivity(deleteActivity);
                    })
            );

            if (next != null) {
                return next.next();
            }

            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Invoked when a message is received from the user. Performs logging of
     * telemetry data using the {@link BotTelemetryClient#trackEvent} method. This
     * event name used is "BotMessageReceived".
     *
     * @param activity Current activity sent from user.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onReceiveActivity(Activity activity) {
        if (activity == null) {
            return CompletableFuture.completedFuture(null);
        }

        return fillReceiveEventProperties(activity, null).thenAccept(properties -> {
            telemetryClient.trackEvent(TelemetryLoggerConstants.BOTMSGRECEIVEEVENT, properties);
        });
    }

    /**
     * Invoked when the bot sends a message to the user. Performs logging of
     * telemetry data using the {@link BotTelemetryClient#trackEvent} method. This
     * event name used is "BotMessageSend".
     *
     * @param activity Current activity sent from user.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onSendActivity(Activity activity) {
        return fillSendEventProperties(activity, null).thenAccept(properties -> {
            telemetryClient.trackEvent(TelemetryLoggerConstants.BOTMSGSENDEVENT, properties);
        });
    }

    /**
     * Invoked when the bot updates a message. Performs logging of telemetry data
     * using the {@link BotTelemetryClient#trackEvent} method. This event name used
     * is "BotMessageUpdate".
     *
     * @param activity Current activity sent from user.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onUpdateActivity(Activity activity) {
        return fillUpdateEventProperties(activity, null).thenAccept(properties -> {
            telemetryClient.trackEvent(TelemetryLoggerConstants.BOTMSGUPDATEEVENT, properties);
        });
    }

    /**
     * Invoked when the bot deletes a message. Performs logging of telemetry data
     * using the {@link BotTelemetryClient#trackEvent} method. This event name used
     * is "BotMessageDelete".
     *
     * @param activity Current activity sent from user.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onDeleteActivity(Activity activity) {
        return fillDeleteEventProperties(activity, null).thenAccept(properties -> {
            telemetryClient.trackEvent(TelemetryLoggerConstants.BOTMSGDELETEEVENT, properties);
        });
    }

    /**
     * Fills the event properties for the BotMessageReceived. Adheres to the
     * LogPersonalInformation flag to filter Name, Text and Speak properties.
     *
     * @param activity             Last activity sent from user.
     * @param additionalProperties Additional properties to add to the event.
     * @return A dictionary that is sent as "Properties" to
     *         {@link BotTelemetryClient#trackEvent} method for the
     *         BotMessageReceived event.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    protected CompletableFuture<Map<String, String>> fillReceiveEventProperties(
        Activity activity,
        Map<String, String> additionalProperties
    ) {

        Map<String, String> properties = new HashMap<String, String>();
        String fromId = activity.getFrom().getId() != null ? activity.getFrom().getId() : "";
        properties.put(TelemetryConstants.FROMIDPROPERTY, fromId);
        String conversationName =
            activity.getConversation().getName() != null ? activity.getConversation().getName() : "";
        properties.put(TelemetryConstants.CONVERSATIONNAMEPROPERTY, conversationName);
        String activityLocale = activity.getLocale() != null ? activity.getLocale() : "";
        properties.put(TelemetryConstants.LOCALEPROPERTY, activityLocale);
        String recipientId = activity.getRecipient().getId() != null ? activity.getRecipient().getId() : "";
        properties.put(TelemetryConstants.RECIPIENTIDPROPERTY, recipientId);
        String recipientName = activity.getRecipient().getName() != null ? activity.getRecipient().getName() : "";
        properties.put(TelemetryConstants.RECIPIENTNAMEPROPERTY, recipientName);

        // Use the LogPersonalInformation flag to toggle logging PII data, text and user
        // name are common examples
        if (logPersonalInformation) {
            if (!StringUtils.isEmpty(activity.getFrom().getName())) {
                properties.put(TelemetryConstants.FROMNAMEPROPERTY, activity.getFrom().getName());
            }

            if (!StringUtils.isEmpty(activity.getText())) {
                properties.put(TelemetryConstants.TEXTPROPERTY, activity.getText());
            }

            if (!StringUtils.isEmpty(activity.getSpeak())) {
                properties.put(TelemetryConstants.SPEAKPROPERTY, activity.getSpeak());
            }

            if (activity.getAttachments() != null && activity.getAttachments().size() > 0) {
                try {
                    properties.put(TelemetryConstants.ATTACHMENTSPROPERTY,
                                   Serialization.toString(activity.getAttachments()));
                } catch (JsonProcessingException e) {
                }
            }
        }

        populateAdditionalChannelProperties(activity, properties);

        // Additional Properties can override "stock" properties.
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return CompletableFuture.completedFuture(properties);
    }

    /**
     * Fills the event properties for BotMessageSend. These properties are logged
     * when an activity message is sent by the Bot to the user.
     *
     * @param activity             Last activity sent from user.
     * @param additionalProperties Additional properties to add to the event.
     * @return A dictionary that is sent as "Properties" to
     *         {@link BotTelemetryClient#trackEvent} method for the BotMessageSend
     *         event.
     */
    protected CompletableFuture<Map<String, String>> fillSendEventProperties(
        Activity activity,
        Map<String, String> additionalProperties
    ) {

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(TelemetryConstants.REPLYACTIVITYIDPROPERTY, activity.getReplyToId());
        properties.put(TelemetryConstants.RECIPIENTIDPROPERTY, activity.getRecipient().getId());
        properties.put(
                    TelemetryConstants.CONVERSATIONNAMEPROPERTY,
                    activity.getConversation().getName()
        );
        properties.put(TelemetryConstants.LOCALEPROPERTY, activity.getLocale());

        // Use the LogPersonalInformation flag to toggle logging PII data, text and user
        // name are common examples
        if (logPersonalInformation) {
            if (!StringUtils.isEmpty(activity.getRecipient().getName())) {
                properties.put(
                    TelemetryConstants.RECIPIENTNAMEPROPERTY, activity.getRecipient().getName()
                );
            }

            if (!StringUtils.isEmpty(activity.getText())) {
                properties.put(TelemetryConstants.TEXTPROPERTY, activity.getText());
            }

            if (!StringUtils.isEmpty(activity.getSpeak())) {
                properties.put(TelemetryConstants.SPEAKPROPERTY, activity.getSpeak());
            }
        }

        // Additional Properties can override "stock" properties.
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return CompletableFuture.completedFuture(properties);
    }

    /**
     * Fills the event properties for BotMessageUpdate. These properties are logged
     * when an activity message is sent by the Bot to the user.
     *
     * @param activity             Last activity sent from user.
     * @param additionalProperties Additional properties to add to the event.
     * @return A dictionary that is sent as "Properties" to
     *         {@link BotTelemetryClient#trackEvent} method for the BotMessageUpdate
     *         event.
     */
    protected CompletableFuture<Map<String, String>> fillUpdateEventProperties(
        Activity activity,
        Map<String, String> additionalProperties
    ) {

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(TelemetryConstants.RECIPIENTIDPROPERTY, activity.getRecipient().getId());
        properties.put(TelemetryConstants.CONVERSATIONIDPROPERTY, activity.getConversation().getId());
        properties.put(
            TelemetryConstants.CONVERSATIONNAMEPROPERTY,
            activity.getConversation().getName()
        );
        properties.put(TelemetryConstants.LOCALEPROPERTY, activity.getLocale());

        // Use the LogPersonalInformation flag to toggle logging PII data, text is a
        // common example
        if (logPersonalInformation && !StringUtils.isEmpty(activity.getText())) {
            properties.put(TelemetryConstants.TEXTPROPERTY, activity.getText());
        }

        // Additional Properties can override "stock" properties.
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return CompletableFuture.completedFuture(properties);
    }

    /**
     * Fills the event properties for BotMessageDelete. These properties are logged
     * when an activity message is sent by the Bot to the user.
     *
     * @param activity             Last activity sent from user.
     * @param additionalProperties Additional properties to add to the event.
     * @return A dictionary that is sent as "Properties" to
     *         {@link BotTelemetryClient#trackEvent} method for the BotMessageDelete
     *         event.
     */
    protected CompletableFuture<Map<String, String>> fillDeleteEventProperties(
        Activity activity,
        Map<String, String> additionalProperties
    ) {

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(TelemetryConstants.RECIPIENTIDPROPERTY, activity.getRecipient().getId());
        properties.put(TelemetryConstants.CONVERSATIONIDPROPERTY, activity.getConversation().getId());
        properties.put(
            TelemetryConstants.CONVERSATIONNAMEPROPERTY,
            activity.getConversation().getName()
        );

        // Additional Properties can override "stock" properties.
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return CompletableFuture.completedFuture(properties);
    }

    private void populateAdditionalChannelProperties(
        Activity activity,
        Map<String, String> properties
    ) {
        if (StringUtils.equalsIgnoreCase(activity.getChannelId(), Channels.MSTEAMS)) {
            ResultPair<TeamsChannelData> teamsChannelData =
                activity.tryGetChannelData(TeamsChannelData.class);
            if (teamsChannelData.result()) {
                if (teamsChannelData.value().getTenant() != null) {
                    properties
                        .put("TeamsTenantId", teamsChannelData.value().getTenant().getId());
                }

                if (activity.getFrom() != null) {
                    properties
                        .put("TeamsUserAadObjectId", activity.getFrom().getAadObjectId());
                }

                try {
                    if (teamsChannelData.value().getTeam() != null) {
                        properties.put(
                            "TeamsTeamInfo",
                            Serialization.toString(teamsChannelData.value().getTeam())
                        );
                    }
                } catch (JsonProcessingException ignored) {

                }
            }
        }
    }
}
