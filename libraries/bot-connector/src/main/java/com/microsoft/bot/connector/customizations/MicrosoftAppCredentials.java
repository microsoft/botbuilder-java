package com.microsoft.bot.connector.customizations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.rest.credentials.TokenCredentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.joda.time.DateTime;

import java.io.DataInput;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
                .url(AuthenticationConstants.ToChannelFromBotLoginUrl)
                .post(new FormBody.Builder()
                        .add("grant_type", "client_credentials")
                        .add("client_id", this.appId)
                        .add("client_secret", this.appPassword)
                        .add("scope", AuthenticationConstants.ToChannelFromBotOAuthScope)
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

    public static void trustServiceUrl(String serviceUrl) {
        trustServiceUrl(serviceUrl, LocalDateTime.now().plusDays(1));
    }

    public static void trustServiceUrl(String serviceUrl, LocalDateTime expirationTime) {
        try {
            URL url = new URL(serviceUrl);
            trustServiceUrl(url, expirationTime);
        } catch (MalformedURLException e) { }
    }

    public static void trustServiceUrl(URL serviceUrl, LocalDateTime expirationTime) {
        _trustHostNames.putIfAbsent(serviceUrl.getHost(), expirationTime);
    }

    public static boolean isTrustedServiceUrl(String serviceUrl) {
        try {
            URL url = new URL(serviceUrl);
            return isTrustedServiceUrl(url);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static boolean isTrustedServiceUrl(URL url) {
        return !_trustHostNames.getOrDefault(url.getHost(), LocalDateTime.MIN).isBefore(LocalDateTime.now().minusMinutes(5));
    }

    private static ConcurrentMap<String, LocalDateTime> _trustHostNames = new ConcurrentHashMap<>();

    static {
        _trustHostNames.putIfAbsent("state.botframework.com", LocalDateTime.MAX);
    }
}
