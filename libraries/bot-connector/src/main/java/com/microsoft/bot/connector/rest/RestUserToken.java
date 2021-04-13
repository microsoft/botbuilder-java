/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector.rest;

import com.microsoft.bot.connector.Async;
import retrofit2.Retrofit;
import com.microsoft.bot.connector.UserToken;
import com.google.common.reflect.TypeToken;
import com.microsoft.bot.schema.AadResourceUrls;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.bot.schema.TokenStatus;
import com.microsoft.bot.restclient.ServiceResponse;
import com.microsoft.bot.restclient.Validator;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined in
 * UserTokens.
 */
public class RestUserToken implements UserToken {
    /** The Retrofit service to perform REST calls. */
    private UserTokensService service;
    /** The service client containing this operation class. */
    private RestOAuthClient client;

    /**
     * Initializes an instance of UserTokensImpl.
     *
     * @param withRetrofit the Retrofit instance built from a Retrofit Builder.
     * @param withClient   the instance of the service client containing this
     *                     operation class.
     */
    public RestUserToken(Retrofit withRetrofit, RestOAuthClient withClient) {
        this.service = withRetrofit.create(UserTokensService.class);
        this.client = withClient;
    }

    /**
     * The interface defining all the services for UserTokens to be used by Retrofit
     * to perform actually REST calls.
     */
    @SuppressWarnings({ "checkstyle:linelength", "checkstyle:JavadocMethod" })
    interface UserTokensService {
        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.UserTokens getToken" })
        @GET("api/usertoken/GetToken")
        CompletableFuture<Response<ResponseBody>> getToken(
            @Query("userId") String userId,
            @Query("connectionName") String connectionName,
            @Query("channelId") String channelId,
            @Query("code") String code
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
        "x-ms-logging-context: com.microsoft.bot.schema.UserTokens exchangeToken" })
        @POST("api/usertoken/Exchange")
        CompletableFuture<Response<ResponseBody>> exchangeToken(
            @Query("userId") String userId,
            @Query("connectionName") String connectionName,
            @Query("channelId") String channelId,
            @Body TokenExchangeRequest exchangeRequest
        );


        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.UserTokens getAadTokens" })
        @POST("api/usertoken/GetAadTokens")
        CompletableFuture<Response<ResponseBody>> getAadTokens(
            @Query("userId") String userId,
            @Query("connectionName") String connectionName,
            @Body AadResourceUrls aadResourceUrls,
            @Query("channelId") String channelId
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.UserTokens signOut" })
        @HTTP(path = "api/usertoken/SignOut", method = "DELETE", hasBody = true)
        CompletableFuture<Response<ResponseBody>> signOut(
            @Query("userId") String userId,
            @Query("connectionName") String connectionName,
            @Query("channelId") String channelId
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.UserTokens signOut" })
        @HTTP(path = "api/usertoken/SignOut", method = "DELETE", hasBody = true)
        CompletableFuture<Response<ResponseBody>> signOut(@Query("userId") String userId);

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.UserTokens getTokenStatus" })
        @GET("api/usertoken/GetTokenStatus")
        CompletableFuture<Response<ResponseBody>> getTokenStatus(
            @Query("userId") String userId,
            @Query("channelId") String channelId,
            @Query("include") String include
        );

        @Headers({ "Content-Type: application/json; charset=utf-8",
            "x-ms-logging-context: com.microsoft.bot.schema.UserTokens sendEmulateOAuthCards" })
        @POST("api/usertoken/emulateOAuthCards")
        CompletableFuture<Response<ResponseBody>> sendEmulateOAuthCards(
            @Query("emulate") boolean emulate
        );
    }

    /**
     *
     * @param userId         the String value
     * @param connectionName the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TokenResponse object
     */
    @Override
    public CompletableFuture<TokenResponse> getToken(String userId, String connectionName) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }
        if (connectionName == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter connectionName is required and cannot be null."
            ));
        }

        final String channelId = null;
        final String code = null;
        return service.getToken(userId, connectionName, channelId, code)
            .thenApply(responseBodyResponse -> {
                try {
                    return getTokenDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getToken", responseBodyResponse);
                }
            });
    }

    /**
     *
     * @param userId         the String value
     * @param connectionName the String value
     * @param channelId      the String value
     * @param code           the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TokenResponse object
     */
    @Override
    public CompletableFuture<TokenResponse> getToken(
        String userId,
        String connectionName,
        String channelId,
        String code
    ) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }
        if (connectionName == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter connectionName is required and cannot be null."
            ));
        }

        return service.getToken(userId, connectionName, channelId, code)
            .thenApply(responseBodyResponse -> {
                try {
                    return getTokenDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getToken", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<TokenResponse> getTokenDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient()
            .responseBuilderFactory()
            .<TokenResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())

            .register(HttpURLConnection.HTTP_OK, new TypeToken<TokenResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_NOT_FOUND, new TypeToken<TokenResponse>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     *
     * @param userId            the String value
     * @param connectionName    the String value
     * @param channelId         the String value
     * @param exchangeRequest   a TokenExchangeRequest
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TokenResponse object
     */
    @Override
    public CompletableFuture<TokenResponse> exchangeToken(
        String userId,
        String connectionName,
        String channelId,
        TokenExchangeRequest exchangeRequest
    ) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }
        if (connectionName == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter connectionName is required and cannot be null."
            ));
        }
        if (channelId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter channelId is required and cannot be null."
            ));
        }
        if (exchangeRequest == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter exchangeRequest is required and cannot be null."
            ));
        }

        return service.exchangeToken(userId, connectionName, channelId, exchangeRequest)
            .thenApply(responseBodyResponse -> {
                try {
                    return exchangeTokenDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getToken", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<TokenResponse> exchangeTokenDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient()
            .responseBuilderFactory()
            .<TokenResponse, ErrorResponseException>newInstance(this.client.serializerAdapter())

            .register(HttpURLConnection.HTTP_OK, new TypeToken<TokenResponse>() {
            }.getType())
            .register(HttpURLConnection.HTTP_NOT_FOUND, new TypeToken<TokenResponse>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     *
     * @param userId          the String value
     * @param connectionName  the String value
     * @param aadResourceUrls the AadResourceUrls value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Map&lt;String, TokenResponse&gt; object
     */
    @Override
    public CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        String userId,
        String connectionName,
        AadResourceUrls aadResourceUrls
    ) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }
        if (connectionName == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter connectionName is required and cannot be null."
            ));
        }
        if (aadResourceUrls == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter aadResourceUrls is required and cannot be null."
            ));
        }

        Validator.validate(aadResourceUrls);
        final String channelId = null;
        return service.getAadTokens(userId, connectionName, aadResourceUrls, channelId)
            .thenApply(responseBodyResponse -> {
                try {
                    return getAadTokensDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getAadTokens", responseBodyResponse);
                }
            });
    }

    /**
     *
     * @param userId          the String value
     * @param connectionName  the String value
     * @param aadResourceUrls the AadResourceUrls value
     * @param channelId       the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Map&lt;String, TokenResponse&gt; object
     */
    @Override
    public CompletableFuture<Map<String, TokenResponse>> getAadTokens(
        String userId,
        String connectionName,
        AadResourceUrls aadResourceUrls,
        String channelId
    ) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }
        if (connectionName == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter connectionName is required and cannot be null."
            ));
        }
        if (aadResourceUrls == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter aadResourceUrls is required and cannot be null."
            ));
        }

        return Async.tryCompletable(() -> {
            Validator.validate(aadResourceUrls);
            return service.getAadTokens(userId, connectionName, aadResourceUrls, channelId)
                .thenApply(responseBodyResponse -> {
                    try {
                        return getAadTokensDelegate(responseBodyResponse).body();
                    } catch (ErrorResponseException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new ErrorResponseException("getAadTokens", responseBodyResponse);
                    }

                });
        });
    }

    private ServiceResponse<Map<String, TokenResponse>> getAadTokensDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient()
            .responseBuilderFactory()
            .<Map<String, TokenResponse>, ErrorResponseException>newInstance(
                this.client.serializerAdapter()
            )

            .register(HttpURLConnection.HTTP_OK, new TypeToken<Map<String, TokenResponse>>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     *
     * @param userId the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Object object
     */
    @Override
    public CompletableFuture<Object> signOut(String userId) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }

        return service.signOut(userId).thenApply(responseBodyResponse -> {
            try {
                return signOutDelegate(responseBodyResponse).body();
            } catch (ErrorResponseException e) {
                throw e;
            } catch (Throwable t) {
                throw new ErrorResponseException("signOut", responseBodyResponse);
            }
        });
    }

    /**
     *
     * @param userId         the String value
     * @param connectionName the String value
     * @param channelId      the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Object object
     */
    @Override
    public CompletableFuture<Object> signOut(
        String userId,
        String connectionName,
        String channelId
    ) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }
        if (connectionName == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter connectionName is required and cannot be null."
            ));
        }
        if (channelId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter channelId is required and cannot be null."
            ));
        }

        return service.signOut(userId, connectionName, channelId)
            .thenApply(responseBodyResponse -> {
                try {
                    return signOutDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("signOut", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<Object> signOutDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient()
            .responseBuilderFactory()
            .<Object, ErrorResponseException>newInstance(this.client.serializerAdapter())

            .register(HttpURLConnection.HTTP_OK, new TypeToken<Object>() {
            }.getType())
            .register(HttpURLConnection.HTTP_NO_CONTENT, new TypeToken<Void>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     *
     * @param userId the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;TokenStatus&gt; object
     */
    @Override
    public CompletableFuture<List<TokenStatus>> getTokenStatus(String userId) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }

        final String channelId = null;
        final String include = null;
        return service.getTokenStatus(userId, channelId, include)
            .thenApply(responseBodyResponse -> {
                try {
                    return getTokenStatusDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getTokenStatus", responseBodyResponse);
                }
            });
    }

    /**
     *
     * @param userId    the String value
     * @param channelId the String value
     * @param include   the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;TokenStatus&gt; object
     */
    @Override
    public CompletableFuture<List<TokenStatus>> getTokenStatus(
        String userId,
        String channelId,
        String include
    ) {
        if (userId == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "Parameter userId is required and cannot be null."
            ));
        }

        return service.getTokenStatus(userId, channelId, include)
            .thenApply(responseBodyResponse -> {
                try {
                    return getTokenStatusDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("getTokenStatus", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<List<TokenStatus>> getTokenStatusDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return this.client.restClient()
            .responseBuilderFactory()
            .<List<TokenStatus>, ErrorResponseException>newInstance(this.client.serializerAdapter())

            .register(HttpURLConnection.HTTP_OK, new TypeToken<List<TokenStatus>>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }

    /**
     * Send a dummy OAuth card when the bot is being used on the Emulator for testing without fetching a real token.
     *
     * @param emulateOAuthCards Indicates whether the Emulator should emulate the OAuth card.
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> sendEmulateOAuthCards(boolean emulateOAuthCards) {
        return service.sendEmulateOAuthCards(emulateOAuthCards)
            .thenApply(responseBodyResponse -> {
                try {
                    return sendEmulateOAuthCardsDelegate(responseBodyResponse).body();
                } catch (ErrorResponseException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new ErrorResponseException("sendEmulateOAuthCards", responseBodyResponse);
                }
            });
    }

    private ServiceResponse<Void> sendEmulateOAuthCardsDelegate(
        Response<ResponseBody> response
    ) throws ErrorResponseException, IOException, IllegalArgumentException {

        return client.restClient()
            .responseBuilderFactory()
            .<Void, ErrorResponseException>newInstance(client.serializerAdapter())
            .register(HttpURLConnection.HTTP_OK, new TypeToken<Void>() {
            }.getType())
            .register(HttpURLConnection.HTTP_ACCEPTED, new TypeToken<Void>() {
            }.getType())
            .registerError(ErrorResponseException.class)
            .build(response);
    }
}
