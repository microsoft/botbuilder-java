// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.teams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ResultPair;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.teams.AppBasedLinkQuery;
import com.microsoft.bot.schema.teams.ChannelInfo;
import com.microsoft.bot.schema.teams.FileConsentCardResponse;
import com.microsoft.bot.schema.teams.MessagingExtensionAction;
import com.microsoft.bot.schema.teams.MessagingExtensionActionResponse;
import com.microsoft.bot.schema.teams.MessagingExtensionQuery;
import com.microsoft.bot.schema.teams.MessagingExtensionResponse;
import com.microsoft.bot.schema.teams.O365ConnectorCardActionQuery;
import com.microsoft.bot.schema.teams.TaskModuleRequest;
import com.microsoft.bot.schema.teams.TaskModuleResponse;
import com.microsoft.bot.schema.teams.TeamInfo;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.microsoft.bot.schema.teams.TeamsChannelData;
import org.apache.commons.lang3.StringUtils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * A Teams implementation of the Bot interface intended for further subclassing.
 * Derive from this class to plug in code to handle particular Activity types.
 * Pre and post processing of Activities can be plugged in by deriving and calling
 * the base class implementation.
 */
@SuppressWarnings({"checkstyle:JavadocMethod", "checkstyle:DesignForExtension"})
public class TeamsActivityHandler extends ActivityHandler {
    /**
     * Invoked when an invoke activity is received from the connector when the base behavior of
     * onTurn is used.
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
                        result = onTeamsFileConsent(turnContext, Serialization.safeGetAs(
                            turnContext.getActivity().getValue(), FileConsentCardResponse.class)
                        );
                        break;

                    case "actionableMessage/executeAction":
                         result = onTeamsO365ConnectorCardAction(turnContext, Serialization.safeGetAs(
                             turnContext.getActivity().getValue(), O365ConnectorCardActionQuery.class)
                         )
                            .thenApply(aVoid -> createInvokeResponse(null));
                        break;

                    case "composeExtension/queryLink":
                        result = onTeamsAppBasedLinkQuery(turnContext, Serialization.safeGetAs(
                            turnContext.getActivity().getValue(), AppBasedLinkQuery.class)
                        )
                            .thenApply(ActivityHandler::createInvokeResponse);
                        break;

                    case "composeExtension/query":
                        result = onTeamsMessagingExtensionQuery(turnContext, Serialization.safeGetAs(
                            turnContext.getActivity().getValue(), MessagingExtensionQuery.class)
                        )
                            .thenApply(ActivityHandler::createInvokeResponse);
                        break;

                    case "composeExtension/selectItem":
                        result = onTeamsMessagingExtensionSelectItem(turnContext, turnContext.getActivity().getValue())
                            .thenApply(ActivityHandler::createInvokeResponse);
                        break;

                    case "composeExtension/submitAction":
                        result = onTeamsMessagingExtensionSubmitActionDispatch(turnContext, Serialization.safeGetAs(
                            turnContext.getActivity().getValue(), MessagingExtensionAction.class)
                        )
                            .thenApply(ActivityHandler::createInvokeResponse);
                        break;

                    case "composeExtension/fetchTask":
                        result = onTeamsMessagingExtensionFetchTask(turnContext, Serialization.safeGetAs(
                            turnContext.getActivity().getValue(), MessagingExtensionAction.class)
                        )
                            .thenApply(ActivityHandler::createInvokeResponse);
                        break;

                    case "composeExtension/querySettingUrl":
                        result = onTeamsMessagingExtensionConfigurationQuerySettingUrl(
                            turnContext, Serialization.safeGetAs(
                                turnContext.getActivity().getValue(), MessagingExtensionQuery.class
                            )
                        )
                            .thenApply(ActivityHandler::createInvokeResponse);
                        break;

                    case "composeExtension/setting":
                        result = onTeamsMessagingExtensionConfigurationSetting(
                            turnContext, turnContext.getActivity().getValue()
                        )
                            .thenApply(ActivityHandler::createInvokeResponse);
                        break;

                    case "composeExtension/onCardButtonClicked":
                        result = onTeamsMessagingExtensionCardButtonClicked(
                            turnContext, turnContext.getActivity().getValue()
                        )
                            .thenApply(ActivityHandler::createInvokeResponse);
                        break;

                    case "task/fetch":
                        result = onTeamsTaskModuleFetch(turnContext, Serialization.safeGetAs(
                            turnContext.getActivity().getValue(), TaskModuleRequest.class)
                        )
                            .thenApply(ActivityHandler::createInvokeResponse);
                        break;

                    case "task/submit":
                        result = onTeamsTaskModuleSubmit(turnContext, Serialization.safeGetAs(
                            turnContext.getActivity().getValue(), TaskModuleRequest.class)
                        )
                            .thenApply(ActivityHandler::createInvokeResponse);
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
            if (e instanceof InvokeResponseExcetion) {
                return ((InvokeResponseExcetion) e).createInvokeResponse();
            }
            return new InvokeResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getLocalizedMessage());
        });
    }

    protected CompletableFuture<InvokeResponse> onTeamsCardActionInvoke(TurnContext turnContext) {
        return notImplemented();
    }

    protected CompletableFuture<Void> onSignInInvoke(TurnContext turnContext) {
        return onTeamsSigninVerifyState(turnContext);
    }

    protected CompletableFuture<Void> onTeamsSigninVerifyState(TurnContext turnContext) {
        return notImplemented();
    }

    protected CompletableFuture<InvokeResponse> onTeamsFileConsent(
        TurnContext turnContext,
        FileConsentCardResponse fileConsentCardResponse) {

        switch (fileConsentCardResponse.getAction()) {
            case "accept":
                return onTeamsFileConsentAccept(turnContext, fileConsentCardResponse)
                    .thenApply(aVoid -> createInvokeResponse(null));

            case "decline":
                return onTeamsFileConsentDecline(turnContext, fileConsentCardResponse)
                    .thenApply(aVoid -> createInvokeResponse(null));

            default:
                CompletableFuture<InvokeResponse> result = new CompletableFuture<>();
                result.completeExceptionally(new InvokeResponseExcetion(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    fileConsentCardResponse.getAction() + " is not a supported Action."
                ));
                return result;
        }
    }

    protected CompletableFuture<Void> onTeamsFileConsentAccept(
        TurnContext turnContext,
        FileConsentCardResponse fileConsentCardResponse) {

        return notImplemented();
    }

    protected CompletableFuture<Void> onTeamsFileConsentDecline(
        TurnContext turnContext,
        FileConsentCardResponse fileConsentCardResponse) {

        return notImplemented();
    }

    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionQuery(
        TurnContext turnContext,
        MessagingExtensionQuery query) {

        return notImplemented();
    }

    protected CompletableFuture<Void> onTeamsO365ConnectorCardAction(
        TurnContext turnContext,
        O365ConnectorCardActionQuery query) {

        return notImplemented();
    }

    protected CompletableFuture<MessagingExtensionResponse> onTeamsAppBasedLinkQuery(
        TurnContext turnContext,
        AppBasedLinkQuery query) {

        return notImplemented();
    }

    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionSelectItem(
        TurnContext turnContext,
        Object query) {

        return notImplemented();
    }

    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionFetchTask(
        TurnContext turnContext,
        MessagingExtensionAction action) {

        return notImplemented();
    }

    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionSubmitActionDispatch(
        TurnContext turnContext,
        MessagingExtensionAction action) {

        if (!StringUtils.isEmpty(action.getBotMessagePreviewAction())) {
            switch (action.getBotMessagePreviewAction()) {
                case "edit":
                    return onTeamsMessagingExtensionBotMessagePreviewEdit(turnContext, action);

                case "send":
                    return onTeamsMessagingExtensionBotMessagePreviewSend(turnContext, action);

                default:
                    CompletableFuture<MessagingExtensionActionResponse> result = new CompletableFuture<>();
                    result.completeExceptionally(new InvokeResponseExcetion(
                        HttpURLConnection.HTTP_BAD_REQUEST,
                        action.getBotMessagePreviewAction() + " is not a support BotMessagePreviewAction"
                    ));
                    return result;
            }
        } else {
            return onTeamsMessagingExtensionSubmitAction(turnContext, action);
        }
    }

    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionSubmitAction(
        TurnContext turnContext,
        MessagingExtensionAction action) {

        return notImplemented();
    }

    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewEdit(
        TurnContext turnContext,
        MessagingExtensionAction action) {

        return notImplemented();
    }

    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewSend(
        TurnContext turnContext,
        MessagingExtensionAction action) {

        return notImplemented();
    }

    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionConfigurationQuerySettingUrl(
        TurnContext turnContext,
        MessagingExtensionQuery query) {

        return notImplemented();
    }

    protected CompletableFuture<Void> onTeamsMessagingExtensionConfigurationSetting(
        TurnContext turnContext,
        Object settings) {

        return notImplemented();
    }

    protected CompletableFuture<TaskModuleResponse> onTeamsTaskModuleFetch(
        TurnContext turnContext,
        TaskModuleRequest taskModuleRequest) {

        return notImplemented();
    }

    protected CompletableFuture<Void> onTeamsMessagingExtensionCardButtonClicked(
        TurnContext turnContext,
        Object cardData) {

        return notImplemented();
    }

    protected CompletableFuture<Void> onTeamsTaskModuleSubmit(
        TurnContext turnContext,
        TaskModuleRequest taskModuleRequest) {

        return notImplemented();
    }

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
                    turnContext.getActivity().getMembersAdded(),
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

                    case "teamRenamed":
                        return onTeamsTeamRenamed(
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

    protected CompletableFuture<Void> onTeamsMembersAddedDispatch(
        List<ChannelAccount> membersAdded,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        Map<String, TeamsChannelAccount> teamMembers = null;

        List<TeamsChannelAccount> teamsMembersAdded = new ArrayList<>();
        for (ChannelAccount memberAdded : membersAdded) {
            if (!memberAdded.getProperties().isEmpty()) {
                try {
                    JsonNode node = mapper.valueToTree(memberAdded);
                    teamsMembersAdded.add(mapper.treeToValue(node, TeamsChannelAccount.class));
                } catch (JsonProcessingException jpe) {
                    return withException(jpe);
                }
            } else {
                // this code path is intended to be temporary and should be removed in 4.7/4.8
                // or whenever Teams is updated

                // we have a simple ChannelAccount so will try to flesh out the details using
                // the getMembers call
                if (teamMembers == null) {
                    List<TeamsChannelAccount> result = TeamsInfo.getMembers(turnContext).join();
                    teamMembers = result.stream().collect(Collectors.toMap(ChannelAccount::getId, item -> item));
                }

                if (teamMembers.containsKey(memberAdded.getId())) {
                    teamsMembersAdded.add(teamMembers.get(memberAdded.getId()));
                } else {
                    // unable to find the member added in ConversationUpdate Activity in the response from
                    // the getMembers call
                    TeamsChannelAccount newTeamsChannelAccount = new TeamsChannelAccount();
                    newTeamsChannelAccount.setId(memberAdded.getId());
                    newTeamsChannelAccount.setName(memberAdded.getName());
                    newTeamsChannelAccount.setAadObjectId(memberAdded.getAadObjectId());
                    newTeamsChannelAccount.setRole(memberAdded.getRole());

                    teamsMembersAdded.add(newTeamsChannelAccount);
                }
            }
        }

        return onTeamsMembersAdded(teamsMembersAdded, teamInfo, turnContext);
    }

    protected CompletableFuture<Void> onTeamsMembersRemovedDispatch(
        List<ChannelAccount> membersRemoved,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        ObjectMapper mapper = new ObjectMapper();
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

    protected CompletableFuture<Void> onTeamsMembersAdded(
        List<TeamsChannelAccount> membersAdded,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return onMembersAdded(new ArrayList<>(membersAdded), turnContext);
    }

    protected CompletableFuture<Void> onTeamsMembersRemoved(
        List<TeamsChannelAccount> membersRemoved,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return onMembersRemoved(new ArrayList<>(membersRemoved), turnContext);
    }

    protected CompletableFuture<Void> onTeamsChannelCreated(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    protected CompletableFuture<Void> onTeamsChannelDeleted(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }


    protected CompletableFuture<Void> onTeamsChannelRenamed(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    protected CompletableFuture<Void> onTeamsTeamRenamed(
        ChannelInfo channelInfo,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return CompletableFuture.completedFuture(null);
    }

    protected <T> CompletableFuture<T> notImplemented() {
        return notImplemented(null);
    }

    protected <T> CompletableFuture<T> notImplemented(String body) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(new InvokeResponseExcetion(HttpURLConnection.HTTP_NOT_IMPLEMENTED, body));
        return result;
    }

    protected <T> CompletableFuture<T> withException(Throwable t) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(new CompletionException(t));
        return result;
    }
}
