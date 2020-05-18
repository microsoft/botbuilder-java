// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.teams;

import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.rest.RestTeamsConnectorClient;
import com.microsoft.bot.connector.teams.TeamsConnectorClient;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.PagedMembersResult;
import com.microsoft.bot.schema.Pair;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.teams.ChannelInfo;
import com.microsoft.bot.schema.teams.ConversationList;
import com.microsoft.bot.schema.teams.TeamDetails;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.microsoft.bot.schema.teams.TeamsChannelData;
import com.microsoft.bot.schema.teams.TeamsPagedMembersResult;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Teams helper methods.
 */
@SuppressWarnings({ "checkstyle:JavadocMethod" })
public final class TeamsInfo {
    private TeamsInfo() {
    }

    /**
     * Returns TeamDetails for a Team.
     *
     * @param turnContext The current TurnContext.
     * @param teamId      The team id.
     * @return A TeamDetails object.
     */
    public static CompletableFuture<TeamDetails> getTeamDetails(
        TurnContext turnContext,
        String teamId
    ) {
        String effectiveTeamId = teamId != null
            ? teamId
            : turnContext.getActivity().teamsGetTeamId();
        if (effectiveTeamId == null) {
            return illegalArgument("This method is only valid within the scope of MS Teams Team.");
        }

        return getTeamsConnectorClient(turnContext).getTeams().fetchTeamDetails(effectiveTeamId);
    }

    /**
     * Returns a list of Teams channels.
     *
     * @param turnContext The current TurnContext.
     * @param teamId      The team id.
     * @return A list of ChannelInfo objects.
     */
    public static CompletableFuture<List<ChannelInfo>> getTeamChannels(
        TurnContext turnContext,
        String teamId
    ) {
        String effectiveTeamId = teamId != null
            ? teamId
            : turnContext.getActivity().teamsGetTeamId();
        if (effectiveTeamId == null) {
            return illegalArgument("This method is only valid within the scope of MS Teams Team.");
        }

        return getTeamsConnectorClient(turnContext).getTeams().fetchChannelList(
            effectiveTeamId
        ).thenApply(ConversationList::getConversations);
    }

    /**
     * Returns a list of team members for the specified team.
     *
     * @param turnContext The current TurnContext.
     * @param teamId      The team id.
     * @return A list of TeamChannelAccount objects.
     */
    public static CompletableFuture<List<TeamsChannelAccount>> getTeamMembers(
        TurnContext turnContext,
        String teamId
    ) {
        String effectiveTeamId = teamId != null
            ? teamId
            : turnContext.getActivity().teamsGetTeamId();
        if (effectiveTeamId == null) {
            return illegalArgument("This method is only valid within the scope of MS Teams Team.");
        }

        return getMembers(getConnectorClient(turnContext), effectiveTeamId);
    }

    /**
     * Returns info for the specified user.
     *
     * @param turnContext The current TurnContext.
     * @param userId      The user id.
     * @param teamId      The team id for the user.
     * @return A TeamsChannelAccount for the user, or null if not found.
     */
    public static CompletableFuture<TeamsChannelAccount> getTeamMember(
        TurnContext turnContext,
        String userId,
        String teamId
    ) {
        String effectiveTeamId = teamId != null
            ? teamId
            : turnContext.getActivity().teamsGetTeamId();
        if (effectiveTeamId == null) {
            return illegalArgument("This method is only valid within the scope of MS Teams Team.");
        }

        return getMember(getConnectorClient(turnContext), userId, effectiveTeamId);
    }

    /**
     * Returns a list of members for the current conversation.
     *
     * @param turnContext The current TurnContext.
     * @return A list of TeamsChannelAccount for each member. If this isn't a Teams
     *         conversation, a list of ChannelAccounts is converted to
     *         TeamsChannelAccount.
     */
    public static CompletableFuture<List<TeamsChannelAccount>> getMembers(TurnContext turnContext) {
        String teamId = turnContext.getActivity().teamsGetTeamId();
        if (!StringUtils.isEmpty(teamId)) {
            return getTeamMembers(turnContext, teamId);
        }

        String conversationId = turnContext.getActivity().getConversation() != null
            ? turnContext.getActivity().getConversation().getId()
            : null;
        return getMembers(getConnectorClient(turnContext), conversationId);
    }

    public static CompletableFuture<TeamsChannelAccount> getMember(
        TurnContext turnContext,
        String userId
    ) {
        String teamId = turnContext.getActivity().teamsGetTeamId();
        if (!StringUtils.isEmpty(teamId)) {
            return getTeamMember(turnContext, userId, teamId);
        }

        String conversationId = turnContext.getActivity().getConversation() != null
            ? turnContext.getActivity().getConversation().getId()
            : null;
        return getMember(getConnectorClient(turnContext), userId, conversationId);
    }

    /**
     * Returns paged Team member list.
     *
     * @param turnContext       The current TurnContext.
     * @param teamId            The team id.
     * @param continuationToken The continuationToken from a previous call, or null
     *                          the first call.
     * @return A TeamsPageMembersResult.
     */
    public static CompletableFuture<TeamsPagedMembersResult> getPagedTeamMembers(
        TurnContext turnContext,
        String teamId,
        String continuationToken
    ) {
        String effectiveTeamId = teamId != null
            ? teamId
            : turnContext.getActivity().teamsGetTeamId();
        if (effectiveTeamId == null) {
            return illegalArgument("This method is only valid within the scope of MS Teams Team.");
        }

        return getPagedMembers(getConnectorClient(turnContext), effectiveTeamId, continuationToken);
    }

    /**
     * Returns paged Team member list. If the Activity is not from a Teams channel,
     * a PagedMembersResult is converted to TeamsPagedMembersResult.
     *
     * @param turnContext       The current TurnContext.
     * @param continuationToken The continuationToken from a previous call, or null
     *                          the first call.
     * @return A TeamsPageMembersResult.
     */
    public static CompletableFuture<TeamsPagedMembersResult> getPagedMembers(
        TurnContext turnContext,
        String continuationToken
    ) {
        String teamId = turnContext.getActivity().teamsGetTeamId();
        if (!StringUtils.isEmpty(teamId)) {
            return getPagedTeamMembers(turnContext, teamId, continuationToken);
        }

        String conversationId = turnContext.getActivity().getConversation() != null
            ? turnContext.getActivity().getConversation().getId()
            : null;
        return getPagedMembers(getConnectorClient(turnContext), conversationId, continuationToken);
    }

    private static CompletableFuture<List<TeamsChannelAccount>> getMembers(
        ConnectorClient connectorClient,
        String conversationId
    ) {
        if (StringUtils.isEmpty(conversationId)) {
            return illegalArgument("The getMembers operation needs a valid conversation Id.");
        }

        return connectorClient.getConversations().getConversationMembers(conversationId).thenApply(
            teamMembers -> {
                List<TeamsChannelAccount> members = teamMembers.stream().map(
                    channelAccount -> Serialization.convert(
                        channelAccount,
                        TeamsChannelAccount.class
                    )
                ).collect(Collectors.toCollection(ArrayList::new));

                members.removeIf(Objects::isNull);
                return members;
            }
        );
    }

    private static CompletableFuture<TeamsChannelAccount> getMember(
        ConnectorClient connectorClient,
        String userId,
        String conversationId
    ) {
        if (StringUtils.isEmpty(conversationId) || StringUtils.isEmpty(userId)) {
            return illegalArgument(
                "The getMember operation needs a valid userId and conversationId."
            );
        }

        return connectorClient.getConversations().getConversationMember(
            userId,
            conversationId
        ).thenApply(teamMember -> Serialization.convert(teamMember, TeamsChannelAccount.class));
    }

    private static CompletableFuture<TeamsPagedMembersResult> getPagedMembers(
        ConnectorClient connectorClient,
        String conversationId,
        String continuationToken
    ) {
        if (StringUtils.isEmpty(conversationId)) {
            return illegalArgument("The getPagedMembers operation needs a valid conversation Id.");
        }

        CompletableFuture<PagedMembersResult> pagedResult;
        if (StringUtils.isEmpty(continuationToken)) {
            pagedResult = connectorClient.getConversations().getConversationPagedMembers(
                conversationId
            );
        } else {
            pagedResult = connectorClient.getConversations().getConversationPagedMembers(
                conversationId,
                continuationToken
            );
        }

        // return a converted TeamsPagedMembersResult
        return pagedResult.thenApply(TeamsPagedMembersResult::new);
    }

    private static ConnectorClient getConnectorClient(TurnContext turnContext) {
        ConnectorClient client = turnContext.getTurnState().get(
            BotFrameworkAdapter.CONNECTOR_CLIENT_KEY
        );
        if (client == null) {
            throw new IllegalStateException("This method requires a connector client.");
        }
        return client;
    }

    private static TeamsConnectorClient getTeamsConnectorClient(TurnContext turnContext) {
        // for testing to be able to provide a custom client.
        TeamsConnectorClient teamsClient = turnContext.getTurnState().get(
            BotFrameworkAdapter.TEAMSCONNECTOR_CLIENT_KEY
        );
        if (teamsClient != null) {
            return teamsClient;
        }

        ConnectorClient client = getConnectorClient(turnContext);
        return new RestTeamsConnectorClient(client.baseUrl(), client.credentials());
    }

    public static CompletableFuture<Pair<ConversationReference, String>> sendMessageToTeamsChannel(
        TurnContext turnContext,
        Activity activity,
        String teamsChannelId,
        MicrosoftAppCredentials credentials
    ) {
        if (turnContext == null) {
            return illegalArgument("turnContext is required");
        }
        if (turnContext.getActivity() == null) {
            return illegalArgument("turnContext.Activity is required");
        }
        if (StringUtils.isEmpty(teamsChannelId)) {
            return illegalArgument("teamsChannelId is required");
        }
        if (credentials == null) {
            return illegalArgument("credentials is required");
        }

        AtomicReference<ConversationReference> conversationReference = new AtomicReference<>();
        AtomicReference<String> newActivityId = new AtomicReference<>();
        String serviceUrl = turnContext.getActivity().getServiceUrl();
        TeamsChannelData teamsChannelData = new TeamsChannelData();
        teamsChannelData.setChannel(new ChannelInfo(teamsChannelId));

        ConversationParameters conversationParameters = new ConversationParameters();
        conversationParameters.setIsGroup(true);

        conversationParameters.setChannelData(teamsChannelData);
        conversationParameters.setActivity(activity);

        return ((BotFrameworkAdapter) turnContext.getAdapter()).createConversation(
            teamsChannelId,
            serviceUrl,
            credentials,
            conversationParameters,
            (TurnContext context) -> {
                conversationReference.set(context.getActivity().getConversationReference());
                newActivityId.set(context.getActivity().getId());
                return CompletableFuture.completedFuture(null);
            }
        ).thenApply(aVoid -> new Pair<>(conversationReference.get(), newActivityId.get()));
    }

    private static <T> CompletableFuture<T> illegalArgument(String message) {
        CompletableFuture<T> detailResult = new CompletableFuture<>();
        detailResult.completeExceptionally(new IllegalArgumentException(message));
        return detailResult;
    }
}
