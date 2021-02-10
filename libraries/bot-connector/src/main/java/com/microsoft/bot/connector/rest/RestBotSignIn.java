/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.bot.connector.rest;

import com.google.common.reflect.TypeToken;
import com.microsoft.bot.connector.Async;
import retrofit2.Retrofit;
import com.microsoft.bot.connector.BotSignIn;
import com.microsoft.bot.restclient.ServiceResponse;
import com.microsoft.bot.schema.SignInResource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.Response;
/**
 * An instance of this class provides access to all the operations defined in
 * BotSignIns.
 */
@SuppressWarnings("PMD")
public class RestBotSignIn implements BotSignIn {
    /** The Retrofit service to perform REST calls. */
    private BotSignInsService service;
    /** The service client containing this operation class. */
    private RestOAuthClient client;
    /**
     * Initializes an instance of BotSignInsImpl.
     *
     * @param withRetrofit the Retrofit instance built from a Retrofit Builder.
     * @param withClient   the instance of the service client containing this
     *                     operation class.
     */
    public RestBotSignIn(Retrofit withRetrofit, RestOAuthClient withClient) {
        this.service = withRetrofit.create(BotSignInsService.class);
        this.client = withClient;
    }
    /**
     * The interface defining all the services for BotSignIns to be used by Retrofit
     * to perform actually REST calls.
     */
    @SuppressWarnings({"checkstyle:linelength", "checkstyle:JavadocMethod"})
    interface BotSignInsService {
        @Headers({"Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.BotSignIns getSignInUrl"})
        @GET("api/botsignin/GetSignInUrl")
        CompletableFuture<Response<ResponseBody>> getSignInUrl(
            @Query("state") String state,
            @Query("code_challenge") String codeChallenge,
            @Query("emulatorUrl") String emulatorUrl,
            @Query("finalRedirect") String finalRedirect
        );
        @Headers({"Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.BotSignIns GetSignInResource"})
        @GET("api/botsignin/GetSignInResource")
        CompletableFuture<Response<ResponseBody>> getSignInResource(
            @Query("state") String state,
            @Query("code_challenge") String codeChallenge,
            @Query("emulatorUrl") String emulatorUrl,
            @Query("finalRedirect") String finalRedirect
        );
    }
    /**
     *
     * @param state the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public CompletableFuture<String> getSignInUrl(String state) {
        if (state == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter state is required and cannot be null."
            ));
        }
        final String codeChallenge = null;
        final String emulatorUrl = null;
        final String finalRedirect = null;
        return service.getSignInUrl(state, codeChallenge, emulatorUrl, finalRedirect)
            .thenApply(responseBodyResponse -> {
                try {
                    return getSignInUrlDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getSignInUrl", responseBodyResponse);
                }
            });
    }
    /**
     *
     * @param state         the String value
     * @param codeChallenge the String value
     * @param emulatorUrl   the String value
     * @param finalRedirect the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public CompletableFuture<String> getSignInUrl(
        String state,
        String codeChallenge,
        String emulatorUrl,
        String finalRedirect
    ) {
        if (state == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter state is required and cannot be null."
            ));
        }
        return service.getSignInUrl(state, codeChallenge, emulatorUrl, finalRedirect)
            .thenApply(responseBodyResponse -> {
                try {
                    return getSignInUrlDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getSignInUrl", responseBodyResponse);
                }
            });
    }
    private ServiceResponse<String> getSignInUrlDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IllegalArgumentException {
        if (!response.isSuccessful()) {
            throw new ErrorResponseException("getSignInUrl", response);
        }
        return new ServiceResponse<>(response.body().source().buffer().readUtf8(), response);
    }
    /**
     *
     * @param state the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public CompletableFuture<SignInResource> getSignInResource(String state) {
        if (state == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter state is required and cannot be null."
            ));
        }
        final String codeChallenge = null;
        final String emulatorUrl = null;
        final String finalRedirect = null;
        return service.getSignInResource(state, codeChallenge, emulatorUrl, finalRedirect)
            .thenApply(responseBodyResponse -> {
                try {
                    return getSignInResourceDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getSignInResource", responseBodyResponse);
                }
            });
    }
    /**
     *
     * @param state         the String value
     * @param codeChallenge the String value
     * @param emulatorUrl   the String value
     * @param finalRedirect the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public CompletableFuture<SignInResource> getSignInResource(
        String state,
        String codeChallenge,
        String emulatorUrl,
        String finalRedirect
    ) {
        if (state == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter state is required and cannot be null."
            ));
        }
        return service.getSignInResource(state, codeChallenge, emulatorUrl, finalRedirect)
            .thenApply(responseBodyResponse -> {
                try {
                    return getSignInResourceDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getSignInResource", responseBodyResponse);
                }
            });
    }
    private ServiceResponse<SignInResource> getSignInResourceDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IllegalArgumentException, IOException {
        if (!response.isSuccessful()) {
            throw new ErrorResponseException("getSignInResource", response);
        }
        return this.client.restClient()
            .responseBuilderFactory()
            .<SignInResource, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<SignInResource>() {
            }.getType())
            .register(HttpURLConnection.HTTP_MOVED_PERM, new TypeToken<Void>() {
            }.getType())
            .register(HttpURLConnection.HTTP_MOVED_TEMP, new TypeToken<Void>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }
}
