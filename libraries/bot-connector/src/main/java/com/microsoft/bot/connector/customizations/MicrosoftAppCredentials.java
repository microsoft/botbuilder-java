package com.microsoft.bot.connector.customizations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.rest.credentials.TokenCredentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class MicrosoftAppCredentials extends TokenCredentials {
    private String appId;
    private String appPassword;

    private OkHttpClient client;
    private ObjectMapper mapper;

    private String currentToken = null;
    private long expiredTime = 0;

    public MicrosoftAppCredentials(String appId, String appPassword) {
        super("Bearer", "");
        this.appId = appId;
        this.appPassword = appPassword;
        client = new OkHttpClient.Builder().build();
        mapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    protected String getToken(Request request) throws IOException {
        if (System.currentTimeMillis() < expiredTime) {
            return currentToken;
        }
        Request reqToken = request.newBuilder()
                .url(AuthSettings.REFRESH_ENDPOINT)
                .post(new FormBody.Builder()
                        .add("grant_type", "client_credentials")
                        .add("client_id", this.appId)
                        .add("client_secret", this.appPassword)
                        .add("scope", AuthSettings.REFRESH_SCOPE)
                        .build()).build();
        Response response = client.newCall(reqToken).execute();
        if (response.isSuccessful()) {
            String payload = response.body().string();
            AuthenticationResponse authResponse = mapper.readValue(payload, AuthenticationResponse.class);
            expiredTime = System.currentTimeMillis() + (authResponse.expiresIn * 1000);
            currentToken = authResponse.accessToken;
        }
        return currentToken;
    }

    private static class AuthenticationResponse {
        @JsonProperty(value = "token_type")
        String tokenType;
        @JsonProperty(value = "expires_in")
        long expiresIn;
        @JsonProperty(value = "ext_expires_in")
        long extExpiresIn;
        @JsonProperty(value = "access_token")
        String accessToken;
    }
}
