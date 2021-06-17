/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector.rest;

import com.google.common.reflect.TypeToken;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.teams.TeamsOperations;
import com.microsoft.bot.restclient.ServiceResponse;
import com.microsoft.bot.schema.teams.ConversationList;
import com.microsoft.bot.schema.teams.MeetingInfo;
import com.microsoft.bot.schema.teams.TeamDetails;
import com.microsoft.bot.schema.teams.TeamsMeetingParticipant;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;
import retrofit2.http.Query;

/**
 * msrest impl of TeamsOperations.
 */
public class RestTeamsOperations implements TeamsOperations {
    /** The Retrofit service to perform REST calls. */
    private TeamsService service;

    /** The service client containing this operation class. */
    private RestTeamsConnectorClient client;

    /**
     * Initializes an instance of ConversationsImpl.
     *
     * @param withRetrofit the Retrofit instance built from a Retrofit Builder.
     * @param withClient   the instance of the service client containing this
     *                     operation class.
     */
    RestTeamsOperations(Retrofit withRetrofit, RestTeamsConnectorClient withClient) {
        service = withRetrofit.create(RestTeamsOperations.TeamsService.class);
        client = withClient;
    }

    /**
     * Implementation of fetchChannelList.
     *
     * @see TeamsOperations#fetchChannelList
     */
    @Override
    public CompletableFuture<ConversationList> fetchChannelList(String teamId) {
        if (teamId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter teamId is required and cannot be null."
            ));
        }

        return service.fetchChannelList(teamId, client.getAcceptLanguage(), client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return fetchChannelListDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("fetchChannelList", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<ConversationList> fetchChannelListDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<ConversationList, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<ConversationList>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Implementation of fetchTeamDetails.
     *
     * @see TeamsOperations#fetchTeamDetails
     */
    @Override
    public CompletableFuture<TeamDetails> fetchTeamDetails(String teamId) {
        if (teamId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter teamId is required and cannot be null."
            ));
        }

        return service.fetchTeamDetails(teamId, client.getAcceptLanguage(), client.getUserAgent())
            .thenApply(responseBodyResponse -> {
                try {
                    return fetchTeamDetailsDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("fetchTeamDetails", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<TeamDetails> fetchTeamDetailsDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<TeamDetails, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<TeamDetails>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Fetches Teams meeting participant details.
     * @param meetingId Teams meeting id
     * @param participantId Teams meeting participant id
     * @param tenantId Teams meeting tenant id
     * @return TeamsParticipantChannelAccount
     */
    public CompletableFuture<TeamsMeetingParticipant> fetchParticipant(
        String meetingId,
        String participantId,
        String tenantId
    ) {
        return service.fetchParticipant(
            meetingId, participantId, tenantId, client.getAcceptLanguage(), client.getUserAgent()
        )
            .thenApply(responseBodyResponse -> {
                try {
                    return fetchParticipantDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("fetchParticipant", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<TeamsMeetingParticipant> fetchParticipantDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {
        return client.restClient()
            .responseBuilderFactory()
            .<TeamsMeetingParticipant, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<TeamsMeetingParticipant>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Fetches Teams meeting participant details.
     * @param meetingId Teams meeting id
     * @return TeamsParticipantChannelAccount
     */
    @Override
    public CompletableFuture<MeetingInfo> fetchMeetingInfo(String meetingId) {
        return service.fetchMeetingInfo(
            meetingId, client.getAcceptLanguage(), client.getUserAgent()
        )
            .thenApply(responseBodyResponse -> {
                try {
                    return fetchMeetingInfoDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("fetchMeetingInfo", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<MeetingInfo> fetchMeetingInfoDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {
        return client.restClient()
            .responseBuilderFactory()
            .<MeetingInfo, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<MeetingInfo>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * The interface defining all the services for TeamsOperations to be used by
     * Retrofit to perform actually REST calls.
     */
    @SuppressWarnings({ "checkstyle:linelength", "checkstyle:JavadocMethod" })
    interface TeamsService {
        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Teams fetchChannelList" })
        @GET("v3/teams/{teamId}/conversations")
        CompletableFuture<Response<ResponseBody>> fetchChannelList(
            @Path("teamId") String teamId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Teams fetchTeamDetails" })
        @GET("v3/teams/{teamId}")
        CompletableFuture<Response<ResponseBody>> fetchTeamDetails(
            @Path("teamId") String teamId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Teams fetchParticipant" })
        @GET("v1/meetings/{meetingId}/participants/{participantId}")
        CompletableFuture<Response<ResponseBody>> fetchParticipant(
            @Path("meetingId") String meetingId,
            @Path("participantId") String participantId,
            @Query("tenantId") String tenantId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.Teams fetchMeetingInfo" })
        @GET("v1/meetings/{meetingId}")
        CompletableFuture<Response<ResponseBody>> fetchMeetingInfo(
            @Path("meetingId") String meetingId,
            @Header("accept-language") String acceptLanguage,
            @Header("User-Agent") String userAgent
        );
    }
}
