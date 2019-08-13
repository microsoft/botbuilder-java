package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.schema.models.TokenResponse;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;
import okhttp3.Response;


import static java.util.concurrent.CompletableFuture.completedFuture;

public class BotAccessTokenStub extends MicrosoftAppCredentials {
    private final String token;

    public BotAccessTokenStub(String token, String appId, String appSecret) {
        super(appId,appSecret);
        this.token = token;
    }

    /**
     * Apply the credentials to the HTTP client builder.
     *
     * @param clientBuilder the builder for building up an {@link OkHttpClient}
     */
    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new TestBearerTokenInterceptor(this.token));
    }




}
