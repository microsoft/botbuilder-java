// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.teams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.rest.ErrorResponseException;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.Error;
import com.microsoft.bot.schema.ErrorResponse;
import com.microsoft.bot.schema.ResultPair;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.teams.AppBasedLinkQuery;
import com.microsoft.bot.schema.teams.ChannelInfo;
import com.microsoft.bot.schema.teams.FileConsentCardResponse;
import com.microsoft.bot.schema.teams.MeetingEndEventDetails;
import com.microsoft.bot.schema.teams.MeetingStartEventDetails;
import com.microsoft.bot.schema.teams.MessagingExtensionAction;
import com.microsoft.bot.schema.teams.MessagingExtensionActionResponse;
import com.microsoft.bot.schema.teams.MessagingExtensionQuery;
import com.microsoft.bot.schema.teams.MessagingExtensionResponse;
import com.microsoft.bot.schema.teams.O365ConnectorCardActionQuery;
import com.microsoft.bot.schema.teams.TabRequest;
import com.microsoft.bot.schema.teams.TabResponse;
import com.microsoft.bot.schema.teams.TabSubmit;
import com.microsoft.bot.schema.teams.TaskModuleRequest;
import com.microsoft.bot.schema.teams.TaskModuleResponse;
import com.microsoft.bot.schema.teams.TeamInfo;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.microsoft.bot.schema.teams.TeamsChannelData;
import org.apache.commons.lang3.StringUtils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * A Teams implementation of the Bot interface intended for further subclassing.
 * Derive from this class to plug in code to handle particular Activity types.
 * Pre and post processing of Activities can be plugged in by deriving and
 * calling the base class implementation.
 */
@SuppressWarnings({"checkstyle:JavadocMethod", "checkstyle:DesignForExtension", "checkstyle:MethodLength"})
public class TeamsActivityHandler extends ActivityHandler {
    /**
     * Invoked when an invoke activity is received from the connector when the base
     * behavior of onTurn is used.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    @Override
    protected CompletableFuture<InvokeResponse> onInvokeActivity(TurnContext turnContext) {
        CompletableFuture<InvokeResponse> result;

        try {
            if (turnContext.getActivity().getName() == null && turnContext.getActivity().isTeamsActivity()) {
                result = onTeamsCardActionInvoke(turnContext);
            } else {
                switch (turnContext.getActivity().getName()) {
                    case "fileConsent/invoke":
                        result = onTeamsFileConsent(
                            turnContext,
                            Serialization.safeGetAs(turnContext.getActivity().getValue(), FileConsentCardResponse.class)
                        );
                        break;

                    case "actionableMessage/executeAction":
                        result = onTeamsO365ConnectorCardAction(
                            turnContext,
                            Serialization
                                .safeGetAs(turnContext.getActivity().getValue(), O365ConnectorCardActionQuery.class)
                        ).thenApply(aVoid -> createInvokeResponse(null));
                        break;

                    case "composeExtension/queryLink":
                        result = onTeamsAppBasedLinkQuery(
                            turnContext,
                            Serialization.safeGetAs(turnContext.getActivity().getValue(), AppBasedLinkQuery.class)
                        ).thenApply(this::createInvokeResponse);
                        break;

                    case "composeExtension/query":
                        result = onTeamsMessagingExtensionQuery(
                            turnContext,
                            Serialization.safeGetAs(turnContext.getActivity().getValue(), MessagingExtensionQuery.class)
                        ).thenApply(this::createInvokeResponse);
                        break;

                    case "composeExtension/selectItem":
                        result = onTeamsMessagingExtensionSelectItem(turnContext, turnContext.getActivity().getValue())
                            .thenApply(this::createInvokeResponse);
                        break;

                    case "composeExtension/submitAction":
                        result =
                            onTeamsMessagingExtensionSubmitActionDispatch(
                                turnContext,
                                Serialization
                                    .safeGetAs(turnContext.getActivity().getValue(), MessagingExtensionAction.class)
                            ).thenApply(this::createInvokeResponse);
                        break;

                    case "composeExtension/fetchTask":
                        result =
                            onTeamsMessagingExtensionFetchTask(
                                turnContext,
                                Serialization
                                    .safeGetAs(turnContext.getActivity().getValue(), MessagingExtensionAction.class)
                            ).thenApply(this::createInvokeResponse);
                        break;

                    case "composeExtension/querySettingUrl":
                        result = onTeamsMessagingExtensionConfigurationQuerySettingUrl(
                            turnContext,
                            Serialization.safeGetAs(turnContext.getActivity().getValue(), MessagingExtensionQuery.class)
                        ).thenApply(this::createInvokeResponse);
                        break;

                    case "composeExtension/setting":
                        result = onTeamsMessagingExtensionConfigurationSetting(
                            turnContext,
                            turnContext.getActivity().getValue()
                        ).thenApply(this::createInvokeResponse);
                        break;

                    case "composeExtension/onCardButtonClicked":
                        result = onTeamsMessagingExtensionCardButtonClicked(
                            turnContext,
                            turnContext.getActivity().getValue()
                        ).thenApply(this::createInvokeResponse);
                        break;

                    case "task/fetch":
                        result = onTeamsTaskModuleFetch(
                            turnContext,
                            Serialization.safeGetAs(turnContext.getActivity().getValue(), TaskModuleRequest.class)
                        ).thenApply(this::createInvokeResponse);
                        break;

                    case "task/submit":
                        result = onTeamsTaskModuleSubmit(
                            turnContext,
                            Serialization.safeGetAs(turnContext.getActivity().getValue(), TaskModuleRequest.class)
                        ).thenApply(this::createInvokeResponse);
                        break;

                    case "tab/fetch":
                        result = onTeamsTabFetch(
                            turnContext,
                            Serialization.safeGetAs(turnContext.getActivity().getValue(), TabRequest.class)
                        ).thenApply(this::createInvokeResponse);
                        break;

                    case "tab/submit":
                        result = onTeamsTabSubmit(
                            turnContext,
                            Serialization.safeGetAs(turnContext.getActivity().getValue(), TabSubmit.class)
                        ).thenApply(this::createInvokeResponse);
                        break;

                    default:
                        result = super.onInvokeActivity(turnContext);
                        break;
                }
            }
        } catch (Throwable t) {
            result = new CompletableFuture<>();
            result.completeExceptionally(t);
        }

        return result.exceptionally(e -> {
            if (e instanceof CompletionException && e.getCause() instanceof InvokeResponseException) {
                return ((InvokeResponseException) e.getCause()).createInvokeResponse();
            } else if (e instanceof InvokeResponseException) {
                return ((InvokeResponseException) e).createInvokeResponse();
            }
            return new InvokeResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getLocalizedMessage());
        });
    }

    /**
     * Invoked when a card action invoke activity is received from the connector.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<InvokeResponse> onTeamsCardActionInvoke(TurnContext turnContext) {
        return notImplemented();
    }

    /**
     * Invoked when a signIn invoke activity is received from the connector.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onSignInInvoke(TurnContext turnContext) {
        return onTeamsSigninVerifyState(turnContext);
    }

    /**
     * Invoked when a signIn verify state activity is received from the connector.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsSigninVerifyState(TurnContext turnContext) {
        return notImplemented();
    }

    /**
     * Invoked when a file consent card activity is received from the connector.
     *
     * @param turnContext             The current TurnContext.
     * @param fileConsentCardResponse The response representing the value of the
     *                                invoke activity sent when the user acts on a
     *                                file consent card.
     * @return An InvokeResponse depending on the action of the file consent card.
     */
    protected CompletableFuture<InvokeResponse> onTeamsFileConsent(
        TurnContext turnContext,
        FileConsentCardResponse fileConsentCardResponse
    ) {
        switch (fileConsentCardResponse.getAction()) {
            case "accept":
                return onTeamsFileConsentAccept(turnContext, fileConsentCardResponse)
                    .thenApply(aVoid -> createInvokeResponse(null));

            case "decline":
                return onTeamsFileConsentDecline(turnContext, fileConsentCardResponse)
                    .thenApply(aVoid -> createInvokeResponse(null));

            default:
                CompletableFuture<InvokeResponse> result = new CompletableFuture<>();
                result.completeExceptionally(
                    new InvokeResponseException(
                        HttpURLConnection.HTTP_BAD_REQUEST,
                        fileConsentCardResponse.getAction() + " is not a supported Action."
                    )
                );
                return result;
        }
    }

    /**
     * Invoked when a file consent card is accepted by the user.
     *
     * @param turnContext             The current TurnContext.
     * @param fileConsentCardResponse The response representing the value of the
     *                                invoke activity sent when the user accepts a
     *                                file consent card.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsFileConsentAccept(
        TurnContext turnContext,
        FileConsentCardResponse fileConsentCardResponse
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a file consent card is declined by the user.
     *
     * @param turnContext             The current TurnContext.
     * @param fileConsentCardResponse The response representing the value of the
     *                                invoke activity sent when the user declines a
     *                                file consent card.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsFileConsentDecline(
        TurnContext turnContext,
        FileConsentCardResponse fileConsentCardResponse
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a Messaging Extension Query activity is received from the
     * connector.
     *
     * @param turnContext The current TurnContext.
     * @param query       The query for the search command.
     * @return The Messaging Extension Response for the query.
     */
    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionQuery(
        TurnContext turnContext,
        MessagingExtensionQuery query
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a O365 Connector Card Action activity is received from the
     * connector.
     *
     * @param turnContext The current TurnContext.
     * @param query       The O365 connector card HttpPOST invoke query.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsO365ConnectorCardAction(
        TurnContext turnContext,
        O365ConnectorCardActionQuery query
    ) {
        return notImplemented();
    }

    /**
     * Invoked when an app based link query activity is received from the connector.
     *
     * @param turnContext The current TurnContext.
     * @param query       The invoke request body type for app-based link query.
     * @return The Messaging Extension Response for the query.
     */
    protected CompletableFuture<MessagingExtensionResponse> onTeamsAppBasedLinkQuery(
        TurnContext turnContext,
        AppBasedLinkQuery query
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a messaging extension select item activity is received from the
     * connector.
     *
     * @param turnContext The current TurnContext.
     * @param query       The object representing the query.
     * @return The Messaging Extension Response for the query.
     */
    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionSelectItem(
        TurnContext turnContext,
        Object query
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a Messaging Extension Fetch activity is received from the
     * connector.
     *
     * @param turnContext The current TurnContext.
     * @param action      The messaging extension action.
     * @return The Messaging Extension Action Response for the action.
     */
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionFetchTask(
        TurnContext turnContext,
        MessagingExtensionAction action
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a messaging extension submit action dispatch activity is
     * received from the connector.
     *
     * @param turnContext The current TurnContext.
     * @param action      The messaging extension action.
     * @return The Messaging Extension Action Response for the action.
     */
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionSubmitActionDispatch(
        TurnContext turnContext,
        MessagingExtensionAction action
    ) {
        if (!StringUtils.isEmpty(action.getBotMessagePreviewAction())) {
            switch (action.getBotMessagePreviewAction()) {
                case "edit":
                    return onTeamsMessagingExtensionBotMessagePreviewEdit(turnContext, action);

                case "send":
                    return onTeamsMessagingExtensionBotMessagePreviewSend(turnContext, action);

                default:
                    CompletableFuture<MessagingExtensionActionResponse> result = new CompletableFuture<>();
                    result.completeExceptionally(
                        new InvokeResponseException(
                            HttpURLConnection.HTTP_BAD_REQUEST,
                            action.getBotMessagePreviewAction() + " is not a support BotMessagePreviewAction"
                        )
                    );
                    return result;
            }
        } else {
            return onTeamsMessagingExtensionSubmitAction(turnContext, action);
        }
    }

    /**
     * Invoked when a messaging extension submit action activity is received from
     * the connector.
     *
     * @param turnContext The current TurnContext.
     * @param action      The messaging extension action.
     * @return The Messaging Extension Action Response for the action.
     */
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionSubmitAction(
        TurnContext turnContext,
        MessagingExtensionAction action
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a messaging extension bot message preview edit activity is
     * received from the connector.
     *
     * @param turnContext The current TurnContext.
     * @param action      The messaging extension action.
     * @return The Messaging Extension Action Response for the action.
     */
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewEdit(
        TurnContext turnContext,
        MessagingExtensionAction action
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a messaging extension bot message preview send activity is
     * received from the connector.
     *
     * @param turnContext The current TurnContext.
     * @param action      The messaging extension action.
     * @return The Messaging Extension Action Response for the action.
     */
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewSend(
        TurnContext turnContext,
        MessagingExtensionAction action
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a messaging extension configuration query setting url activity
     * is received from the connector.
     *
     * @param turnContext The current TurnContext.
     * @param query       The Messaging extension query.
     * @return The Messaging Extension Response for the query.
     */
    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionConfigurationQuerySettingUrl(
        TurnContext turnContext,
        MessagingExtensionQuery query
    ) {
        return notImplemented();
    }

    /**
     * Override this in a derived class to provide logic for when a configuration is
     * set for a messaging extension.
     *
     * @param turnContext The current TurnContext.
     * @param settings    Object representing the configuration settings.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsMessagingExtensionConfigurationSetting(
        TurnContext turnContext,
        Object settings
    ) {
        return notImplemented();
    }

    /**
     * Override this in a derived class to provide logic for when a task module is
     * fetched.
     *
     * @param turnContext       The current TurnContext.
     * @param taskModuleRequest The task module invoke request value payload.
     * @return A Task Module Response for the request.
     */
    protected CompletableFuture<TaskModuleResponse> onTeamsTaskModuleFetch(
        TurnContext turnContext,
        TaskModuleRequest taskModuleRequest
    ) {
        return notImplemented();
    }

    /**
     * Override this in a derived class to provide logic for when a card button is
     * clicked in a messaging extension.
     *
     * @param turnContext The current TurnContext.
     * @param cardData    Object representing the card data.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsMessagingExtensionCardButtonClicked(
        TurnContext turnContext,
        Object cardData
    ) {
        return notImplemented();
    }

    /**
     * Override this in a derived class to provide logic for when a task module is
     * submited.
     *
     * @param turnContext       The current TurnContext.
     * @param taskModuleRequest The task module invoke request value payload.
     * @return A Task Module Response for the request.
     */
    protected CompletableFuture<TaskModuleResponse> onTeamsTaskModuleSubmit(
        TurnContext turnContext,
        TaskModuleRequest taskModuleRequest
    ) {
        return notImplemented();
    }

    /**
     * Invoked when a conversation update activity is received from the channel.
     * Conversation update activities are useful when it comes to responding to
     * users being added to or removed from the channel. For example, a bot could
     * respond to a user being added by greeting the user.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        if (turnContext.getActivity().isTeamsActivity()) {
            ResultPair<TeamsChannelData> channelData =
                turnContext.getActivity().tryGetChannelData(TeamsChannelData.class);

            if (turnContext.getActivity().getMembersAdded() != null) {
                return onTeamsMembersAddedDispatch(
                    turnContext.getActivity().getMembersAdded(),
                    channelData.result() ? channelData.value().getTeam() : null,
                    turnContext
                );
            }

            if (turnContext.getActivity().getMembersRemoved() != null) {
                return onTeamsMembersRemovedDispatch(
                    turnContext.getActivity().getMembersRemoved(),
                    channelData.result() ? channelData.value().getTeam() : null,
                    turnContext
                );
            }

            if (channelData.result()) {
                switch (channelData.value().getEventType()) {
                    case "channelCreated":
                        return onTeamsChannelCreated(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    case "channelDeleted":
                        return onTeamsChannelDeleted(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    case "channelRenamed":
                        return onTeamsChannelRenamed(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    case "channelRestored":
                        return onTeamsChannelRestored(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    case "teamArchived":
                        return onTeamsTeamArchived(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    case "teamDeleted":
                        return onTeamsTeamDeleted(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    case "teamHardDeleted":
                        return onTeamsTeamHardDeleted(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    case "teamRenamed":
                        return onTeamsTeamRenamed(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    case "teamRestored":
                        return onTeamsTeamRestored(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    case "teamUnarchived":
                        return onTeamsTeamUnarchived(
                            channelData.value().getChannel(),
                            channelData.value().getTeam(),
                            turnContext
                        );

                    default:
                        return super.onConversationUpdateActivity(turnContext);
                }
            }
        }

        return super.onConversationUpdateActivity(turnContext);
    }

    /**
     * Override this in a derived class to provide logic for when members other than
     * the bot join the channel, such as your bot's welcome logic. It will get the
     * associated members with the provided accounts.
     *
     * @param membersAdded A list of all the accounts added to the channel, as
     *                     described by the conversation update activity.
     * @param teamInfo     The team info object representing the team.
     * @param turnContext  The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsMembersAddedDispatch(
        List<ChannelAccount> membersAdded,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.findAndRegisterModules();

        List<TeamsChannelAccount> teamsMembersAdded = new ArrayList<>();
        for (ChannelAccount memberAdded : membersAdded) {
            boolean isBot = turnContext.getActivity().getRecipient() != null
                && StringUtils.equals(memberAdded.getId(), turnContext.getActivity().getRecipient().getId());

            if (!memberAdded.getProperties().isEmpty() || isBot) {
                // when the ChannelAccount object is fully a TeamsChannelAccount, or for the bot
                // (when Teams changes the service to return the full details)
                try {
                    JsonNode node = mapper.valueToTree(memberAdded);
                    teamsMembersAdded.add(mapper.treeToValue(node, TeamsChannelAccount.class));
                } catch (JsonProcessingException jpe) {
                    return withException(jpe);
                }
            } else {
                TeamsChannelAccount teamsChannelAccount = null;
                try {
                    teamsChannelAccount = TeamsInfo.getMember(turnContext, memberAdded.getId()).join();
                } catch (CompletionException ex) {
                    Throwable causeException = ex.getCause();
                    if (causeException instanceof ErrorResponseException) {
                        ErrorResponse response = ((ErrorResponseException) causeException).body();
                        if (response != null) {
                            Error error = response.getError();
                            if (error != null && !error.getCode().equals("ConversationNotFound")) {
                                throw ex;
                            }
                        }
                    } else {
                        throw ex;
                    }
                }

                if (teamsChannelAccount == null) {
                    // unable to find the member added in ConversationUpdate Activity in
                    // the response from the getMember call
                    teamsChannelAccount = new TeamsChannelAccount();
                    teamsChannelAccount.setId(memberAdded.getId());
                    teamsChannelAccount.setName(memberAdded.getName());
                    teamsChannelAccount.setAadObjectId(memberAdded.getAadObjectId());
                    teamsChannelAccount.setRole(memberAdded.getRole());
                }
                teamsMembersAdded.add(teamsChannelAccount);
            }
        }

        return onTeamsMembersAdded(teamsMembersAdded, teamInfo, turnContext);
    }

    /**
     * Override this in a derived class to provide logic for when members other than
     * the bot leave the channel, such as your bot's good-bye logic. It will get the
     * associated members with the provided accounts.
     *
     * @param membersRemoved A list of all the accounts removed from the channel, as
     *                       described by the conversation update activity.
     * @param teamInfo       The team info object representing the team.
     * @param turnContext    The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsMembersRemovedDispatch(
        List<ChannelAccount> membersRemoved,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.findAndRegisterModules();

        List<TeamsChannelAccount> teamsMembersRemoved = new ArrayList<>();
        for (ChannelAccount memberRemoved : membersRemoved) {
            try {
                JsonNode node = mapper.valueToTree(memberRemoved);
                teamsMembersRemoved.add(mapper.treeToValue(node, TeamsChannelAccount.class));
            } catch (JsonProcessingException jpe) {
                return withException(jpe);
            }
        }

        return onTeamsMembersRemoved(teamsMembersRemoved, teamInfo, turnContext);
    }

    /**
     * Override this in a derived class to provide logic for when members other than
     * the bot join the channel, such as your bot's welcome logic.
     *
     * @param membersAdded A list of all the members added to the channel, as
     *                     described by the conversation update activity.
     * @param teamInfo     The team info object representing the team.
     * @param turnContext  The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsMembersAdded(
        List<TeamsChannelAccount> membersAdded,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return onMembersAdded(new ArrayList<>(membersAdded), turnContext);
    }

    /**
     * Override this in a derived class to provide logic for when members other than
     * the bot leave the channel, such as your bot's good-bye logic.
     *
     * @param membersRemoved A list of all the members removed from the channel, as
     *                       described by the conversation update activity.
     * @param teamInfo       The team info object representing the team.
     * @param turnContext    The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsMembersRemoved(
        List<TeamsChannelAccount> membersRemoved,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return onMembersRemoved(new ArrayList<>(membersRemoved), turnContext);
    }

    /**
     * Invoked when a Channel Created event activity is received from the connector.
     * Channel Created correspond to the user creating a new channel.
     *
     * @param channelInfo The channel info object which describes the channel.
     * @param teamInfo    The team info object representing the team.
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsChannelCreated(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Channel Deleted event activity is received from the connector.
     * Channel Deleted correspond to the user deleting an existing channel.
     *
     * @param channelInfo The channel info object which describes the channel.
     * @param teamInfo    The team info object representing the team.
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsChannelDeleted(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Channel Renamed event activity is received from the connector.
     * Channel Renamed correspond to the user renaming an existing channel.
     *
     * @param channelInfo The channel info object which describes the channel.
     * @param teamInfo    The team info object representing the team.
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsChannelRenamed(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Channel Restored event activity is received from the
     * connector. Channel Restored correspond to the user restoring a previously
     * deleted channel.
     *
     * @param channelInfo The channel info object which describes the channel.
     * @param teamInfo    The team info object representing the team.
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsChannelRestored(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Team Archived event activity is received from the connector.
     * Team Archived correspond to the user archiving a team.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsTeamArchived(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Team Deleted event activity is received from the connector.
     * Team Deleted correspond to the user deleting a team.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsTeamDeleted(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Team Hard Deleted event activity is received from the
     * connector. Team Hard Deleted correspond to the user hard-deleting a team.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsTeamHardDeleted(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Channel Renamed event activity is received from the connector.
     * Channel Renamed correspond to the user renaming an existing channel.
     *
     * @param channelInfo The channel info object which describes the channel.
     * @param teamInfo    The team info object representing the team.
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsTeamRenamed(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Team Restored event activity is received from the connector.
     * Team Restored correspond to the user restoring a team.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsTeamRestored(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Team Unarchived event activity is received from the connector.
     * Team Unarchived correspond to the user unarchiving a team.
     *
     * @param turnContext The current TurnContext.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsTeamUnarchived(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Override this in a derived class to provide logic for when a tab is fetched.
     * 
     * @param turnContext The context object for this turn.
     * @param tabRequest  The tab invoke request value payload.
     * @return A Tab Response for the request.
     */
    protected CompletableFuture<TabResponse> onTeamsTabFetch(TurnContext turnContext, TabRequest tabRequest) {
        return withException(new InvokeResponseException(HttpURLConnection.HTTP_NOT_IMPLEMENTED));
    }

    /**
     * Override this in a derived class to provide logic for when a tab is
     * submitted.
     * 
     * @param turnContext The context object for this turn.
     * @param tabSubmit   The tab submit invoke request value payload.
     * @return A Tab Response for the request.
     */
    protected CompletableFuture<TabResponse> onTeamsTabSubmit(TurnContext turnContext, TabSubmit tabSubmit) {
        return withException(new InvokeResponseException(HttpURLConnection.HTTP_NOT_IMPLEMENTED));
    }

    /**
     * Invoked when a "tokens/response" event is received when the base behavior of
     * {@link #onEventActivity(TurnContext)} is used.
     *
     * <p>
     * If using an OAuthPrompt, override this method to forward this
     * {@link com.microsoft.bot.schema.Activity} to the current dialog.
     * </p>
     *
     * <p>
     * By default, this method does nothing.
     * </p>
     * <p>
     * When the {@link #onEventActivity(TurnContext)} method receives an event with
     * a {@link com.microsoft.bot.schema.Activity#getName()} of `tokens/response`, it calls this method.
     *
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    @Override
    protected CompletableFuture<Void> onEventActivity(TurnContext turnContext) {
        if (StringUtils.equals(turnContext.getActivity().getChannelId(), Channels.MSTEAMS)) {
            try {
                switch (turnContext.getActivity().getName()) {
                    case "application/vnd.microsoft.meetingStart":
                        return onTeamsMeetingStart(
                            Serialization.safeGetAs(
                                turnContext.getActivity().getValue(),
                                MeetingStartEventDetails.class
                            ),
                            turnContext
                        );

                    case "application/vnd.microsoft.meetingEnd":
                        return onTeamsMeetingEnd(
                            Serialization.safeGetAs(
                                turnContext.getActivity().getValue(),
                                MeetingEndEventDetails.class
                            ),
                            turnContext
                        );

                    default:
                        break;
                }
            } catch (Throwable t) {
                return Async.completeExceptionally(t);
            }
        }

        return super.onEventActivity(turnContext);
    }

    /**
     * Invoked when a Teams Meeting Start event activity is received from the
     * connector. Override this in a derived class to provide logic for when a
     * meeting is started.
     *
     * @param meeting     The details of the meeting.
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsMeetingStart(MeetingStartEventDetails meeting, TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoked when a Teams Meeting End event activity is received from the
     * connector. Override this in a derived class to provide logic for when a
     * meeting is ended.
     *
     * @param meeting     The details of the meeting.
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<Void> onTeamsMeetingEnd(MeetingEndEventDetails meeting, TurnContext turnContext) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Invoke a new InvokeResponseException with a HTTP 501 code status.
     *
     * @return true if this invocation caused this CompletableFuture to transition
     *         to a completed state, else false
     */
    protected <T> CompletableFuture<T> notImplemented() {
        return notImplemented(null);
    }

    /**
     * Invoke a new InvokeResponseException with a HTTP 501 code status.
     *
     * @param body The body for the InvokeResponseException.
     * @return true if this invocation caused this CompletableFuture to transition
     *         to a completed state, else false
     */
    protected <T> CompletableFuture<T> notImplemented(String body) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(new InvokeResponseException(HttpURLConnection.HTTP_NOT_IMPLEMENTED, body));
        return result;
    }

    /**
     * Error handler that can catch exceptions.
     *
     * @param t The exception thrown.
     * @return A task that represents the work queued to execute.
     */
    protected <T> CompletableFuture<T> withException(Throwable t) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(new CompletionException(t));
        return result;
    }
}
