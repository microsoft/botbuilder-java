// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentialsInterceptor;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.TokenExchangeState;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.TokenResponse;
import com.microsoft.rest.ServiceClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.microsoft.bot.connector.authentication.MicrosoftAppCredentials.JSON;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.stream.Collectors.joining;

/**
 * Service client to handle requests to the botframework api service.
 * <p>
 * Uses the MicrosoftInterceptor class to add Authorization header from idp.
 */
public class OAuthClientOld extends ServiceClient {
    private final RestConnectorClient client;
    private final String uri;

    private ObjectMapper mapper;


    public OAuthClientOld(RestConnectorClient client, String uri) throws URISyntaxException, MalformedURLException {
        super(client.restClient());
        URI uriResult = new URI(uri);

        // Sanity check our url
        uriResult.toURL();
        String scheme = uriResult.getScheme();
        if (!scheme.toLowerCase().equals("https"))
            throw new IllegalArgumentException("Please supply a valid https uri");
        if (client == null)
            throw new IllegalArgumentException("client");
        this.client = client;
        this.uri = uri + (uri.endsWith("/") ? "" : "/");
        this.mapper = new ObjectMapper();
    }

    /**
     * Get User Token for given user and connection.
     *
     * @param userId
     * @param connectionName
     * @param magicCode
     * @return CompletableFuture<TokenResponse> on success; otherwise null.
     */
    public CompletableFuture<TokenResponse> getUserToken(String userId, String connectionName, String magicCode) {
        return getUserToken(userId, connectionName, magicCode, null);
    }

    protected URI makeUri(String uri, HashMap<String, String> queryStrings) throws URISyntaxException {
        String newUri = queryStrings.keySet().stream()
            .map(key -> {
                try {
                    return key + "=" + URLEncoder.encode(queryStrings.get(key), StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(joining("&", uri.endsWith("?") ? uri : uri + "?", ""));
        return new URI(newUri);
    }

    /**
     * Get User Token for given user and connection.
     *
     * @param userId
     * @param connectionName
     * @param magicCode
     * @param customHeaders
     * @return CompletableFuture<TokenResponse> on success; null otherwise.
     */
    public CompletableFuture<TokenResponse> getUserToken(String userId,
                                                         String connectionName,
                                                         String magicCode,
                                                         Map<String, ArrayList<String>> customHeaders) {
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId");
        }
        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName");
        }

        return CompletableFuture.supplyAsync(() -> {
            // Construct URL
            HashMap<String, String> qstrings = new HashMap<>();
            qstrings.put("userId", userId);
            qstrings.put("connectionName", connectionName);
            if (!StringUtils.isBlank(magicCode)) {
                qstrings.put("code", magicCode);
            }
            String strUri = String.format("%sapi/usertoken/GetToken", this.uri);
            URI tokenUrl = null;
            try {
                tokenUrl = makeUri(strUri, qstrings);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            }

            // add botframework api service url to the list of trusted service url's for these app credentials.
            MicrosoftAppCredentials.trustServiceUrl(tokenUrl.toString());

            // Set Credentials and make call
            MicrosoftAppCredentials appCredentials = (MicrosoftAppCredentials) client.restClient().credentials();

            // Later: Use client in clientimpl?
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new MicrosoftAppCredentialsInterceptor(appCredentials))
                .build();

            Request request = new Request.Builder()
                .url(tokenUrl.toString())
                .header("User-Agent", UserAgent.value())
                .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                int statusCode = response.code();
                if (statusCode == HTTP_OK) {
                    return this.mapper.readValue(response.body().string(), TokenResponse.class);
                } else if (statusCode == HTTP_NOT_FOUND) {
                    return null;
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (response != null)
                    response.body().close();
            }
            return null;
        }, ExecutorFactory.getExecutor());
    }

    /**
     * Signs Out the User for the given ConnectionName.
     *
     * @param userId
     * @param connectionName
     * @return True on successful sign-out; False otherwise.
     */
    public CompletableFuture<Boolean> signOutUser(String userId, String connectionName) {
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId");
        }
        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName");
        }

        return CompletableFuture.supplyAsync(() -> {
            // Construct URL
            HashMap<String, String> qstrings = new HashMap<>();
            qstrings.put("userId", userId);
            qstrings.put("connectionName", connectionName);
            String strUri = String.format("%sapi/usertoken/SignOut", this.uri);
            URI tokenUrl = null;
            try {
                tokenUrl = makeUri(strUri, qstrings);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return false;
            }

            // add botframework api service url to the list of trusted service url's for these app credentials.
            MicrosoftAppCredentials.trustServiceUrl(tokenUrl);

            // Set Credentials and make call
            MicrosoftAppCredentials appCredentials = (MicrosoftAppCredentials) client.restClient().credentials();

            // Later: Use client in clientimpl?
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new MicrosoftAppCredentialsInterceptor(appCredentials))
                .build();

            Request request = new Request.Builder()
                .delete()
                .url(tokenUrl.toString())
                .header("User-Agent", UserAgent.value())
                .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                int statusCode = response.code();
                if (statusCode == HTTP_OK)
                    return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }, ExecutorFactory.getExecutor());
    }


    /**
     * Gets the Link to be sent to the user for signin into the given ConnectionName
     *
     * @param activity
     * @param connectionName
     * @return Sign in link on success; null otherwise.
     */
    public CompletableFuture<String> getSignInLink(Activity activity, String connectionName) {
        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName");
        }
        if (activity == null) {
            throw new IllegalArgumentException("activity");
        }

        return CompletableFuture.supplyAsync(() -> {
            final MicrosoftAppCredentials creds = (MicrosoftAppCredentials) this.client.restClient().credentials();
            TokenExchangeState tokenExchangeState = new TokenExchangeState() {{
                setConnectionName(connectionName);
                setConversation(new ConversationReference() {{
                    setActivityId(activity.getId());
                    setBot(activity.getRecipient());
                    setChannelId(activity.getChannelId());
                    setConversation(activity.getConversation());
                    setServiceUrl(activity.getServiceUrl());
                    setUser(activity.getFrom());
                }});
                setMsAppId((creds == null) ? null : creds.getAppId());
            }};

            String serializedState;
            try {
                serializedState = mapper.writeValueAsString(tokenExchangeState);
            } catch(Throwable t) {
                throw new CompletionException(t);
            }

            // Construct URL
            String encoded = Base64.getEncoder().encodeToString(serializedState.getBytes(StandardCharsets.UTF_8));
            HashMap<String, String> qstrings = new HashMap<>();
            qstrings.put("state", encoded);

            String strUri = String.format("%sapi/botsignin/getsigninurl", this.uri);
            final URI tokenUrl;

            try {
                tokenUrl = makeUri(strUri, qstrings);
            } catch(Throwable t) {
                throw new CompletionException(t);
            }

            // add botframework api service url to the list of trusted service url's for these app credentials.
            MicrosoftAppCredentials.trustServiceUrl(tokenUrl);

            // Later: Use client in clientimpl?
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new MicrosoftAppCredentialsInterceptor(creds))
                .build();

            Request request = new Request.Builder()
                .url(tokenUrl.toString())
                .header("User-Agent", UserAgent.value())
                .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                int statusCode = response.code();
                if (statusCode == HTTP_OK)
                    return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }, ExecutorFactory.getExecutor());
    }

    /**
     * Send a dummy OAuth card when the bot is being used on the emulator for testing without fetching a real token.
     *
     * @param emulateOAuthCards
     * @return CompletableFuture with no result code
     */
    public CompletableFuture<Void> sendEmulateOAuthCards(Boolean emulateOAuthCards) {
        // Construct URL
        HashMap<String, String> qstrings = new HashMap<>();
        qstrings.put("emulate", emulateOAuthCards.toString());
        String strUri = String.format("%sapi/usertoken/emulateOAuthCards", this.uri);

        return CompletableFuture.runAsync(() -> {
            URI tokenUrl;
            try {
                tokenUrl = makeUri(strUri, qstrings);
            } catch(Throwable t) {
                throw new CompletionException(t);
            }

            // add botframework api service url to the list of trusted service url's for these app credentials.
            MicrosoftAppCredentials.trustServiceUrl(tokenUrl);

            // Construct dummy body
            RequestBody body = RequestBody.create(JSON, "{}");

            // Set Credentials and make call
            MicrosoftAppCredentials appCredentials = (MicrosoftAppCredentials) client.restClient().credentials();

            // Later: Use client in clientimpl?
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new MicrosoftAppCredentialsInterceptor(appCredentials))
                .build();

            Request request = new Request.Builder()
                .url(tokenUrl.toString())
                .header("User-Agent", UserAgent.value())
                .post(body)
                .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                int statusCode = response.code();
                if (statusCode == HTTP_OK)
                    return;
            } catch (IOException e) {
                e.printStackTrace();
            }


            // Apparently swallow any results
            return;

        }, ExecutorFactory.getExecutor());
    }
}
