package com.microsoft.bot.connector.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.TokenExchangeState;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ConversationReference;
import com.microsoft.bot.schema.models.TokenResponse;
import com.microsoft.rest.ServiceClient;

import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.ea.async.Async.await;
import static com.microsoft.bot.connector.authentication.MicrosoftAppCredentials.JSON;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.joining;

/// <summary>
/// Service client to handle requests to the botframework api service.
/// </summary>
public class OAuthClient extends ServiceClient {
    private final ConnectorClientImpl client;
    private final String uri;

    private ObjectMapper mapper;


    public OAuthClient(ConnectorClientImpl client, String uri) throws URISyntaxException, MalformedURLException {
        super(client.restClient());
        URI uriResult = new URI(uri);

        // Sanity check our url
        uriResult.toURL();

        if (new URI(uri).getScheme() == "https")
            throw new IllegalArgumentException("Please supply a valid https uri");
        if (client == null)
            throw new IllegalArgumentException("client");
        this.client = client;
        this.uri = uri + (uri.endsWith("/")? "" : "/");
        this.mapper = new ObjectMapper();
    }

    /// <summary>
/// Get User Token for given user and connection.
/// </summary>
/// <param name="userId"></param>
/// <param name="connectionName"></param>
/// <param name="magicCode"></param>
/// <param name="customHeaders"></param>
/// <param name="cancellationToken"></param>
/// <returns></returns>
    public CompletableFuture<TokenResponse> GetUserTokenAsync(String userId, String connectionName, String magicCode) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        return GetUserTokenAsync(userId, connectionName, magicCode, null);
    }

    public URI MakeUri(String uri, HashMap<String, String> queryStrings) throws URISyntaxException {
        String newUri = queryStrings.keySet().stream()
                .map(key -> {
                    try {
                        return key + "=" + URLEncoder.encode(queryStrings.get(key), StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(joining("&", (uri.endsWith("?") ? uri : uri + "?"), ""));
        return new URI(newUri);


    }

    public CompletableFuture<TokenResponse> GetUserTokenAsync(String userId, String connectionName, String magicCode, HashMap<String, ArrayList<String>> customHeaders) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId");
        }
        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName");
        }

        // Construct URL
        HashMap <String, String> qstrings = new HashMap<>();
        qstrings.put("userId", userId);
        qstrings.put("connectionName", connectionName);
        if (!StringUtils.isBlank(magicCode))
            qstrings.put("code", magicCode);
        String strUri = String.format("%sapi/usertoken/GetToken", this.uri);
        URI tokenUrl = MakeUri(strUri, qstrings);

        // add botframework api service url to the list of trusted service url's for these app credentials.
        MicrosoftAppCredentials.trustServiceUrl(tokenUrl.toString());

        // Set Credentials and make call
        MicrosoftAppCredentials appCredentials = (MicrosoftAppCredentials) client.restClient().credentials();
        CompletableFuture<Response> result = appCredentials.ProcessHttpRequestAsync(true, "GET", tokenUrl.toString(), null);
        result.exceptionally(ex -> {
                    throw new RuntimeException("GET " + tokenUrl.toString() + " failed. " + ex.getMessage());
                    });

        Response httpResponse = result.get();
        if (null == httpResponse) {
            return completedFuture(null);
        }
        int statusCode = httpResponse.code();
        if (statusCode == HTTP_OK) {
            return completedFuture(this.mapper.readValue(httpResponse.body().string(), TokenResponse.class));
        }
        else if (statusCode == HTTP_NOT_FOUND) {
            return completedFuture(null);
        }
        else {
            return completedFuture(null);
        }
    }

    /// <summary>
    /// Signs Out the User for the given ConnectionName.
    /// </summary>
    /// <param name="userId"></param>
    /// <param name="connectionName"></param>
    /// <param name="cancellationToken"></param>
    /// <returns></returns>
    public CompletableFuture<Boolean> SignOutUserAsync(String userId, String connectionName) throws URISyntaxException, IOException {
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId");
        }
        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName");
        }

        String invocationId = null;

        // Construct URL
        HashMap <String, String> qstrings = new HashMap<>();
        qstrings.put("userId", userId);
        qstrings.put("connectionName", connectionName);
        String strUri = String.format("%sapi/usertoken/SignOut", this.uri);
        URI tokenUrl = MakeUri(strUri, qstrings);

        // add botframework api service url to the list of trusted service url's for these app credentials.
        MicrosoftAppCredentials.trustServiceUrl(tokenUrl);

        // Set Credentials and make call
        MicrosoftAppCredentials appCredentials = (MicrosoftAppCredentials) client.restClient().credentials();
        Response httpResponse = await(appCredentials.ProcessHttpRequestAsync(true, "DELETE", tokenUrl.toString()));

        int statusCode = httpResponse.code();
        if (statusCode == HTTP_OK) {
            return completedFuture(true);
        }
        return completedFuture(false);
    }


    /// <summary>
    /// Gets the Link to be sent to the user for signin into the given ConnectionName
    /// </summary>
    /// <param name="activity"></param>
    /// <param name="connectionName"></param>
    /// <param name="cancellationToken"></param>
    /// <returns></returns>
    public CompletableFuture<String> GetSignInLinkAsync(Activity activity, String connectionName) throws IOException, URISyntaxException {
        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName");
        }
        if (activity == null) {
            throw new IllegalArgumentException("activity");
        }

        MicrosoftAppCredentials creds = (MicrosoftAppCredentials) this.client.restClient().credentials();
        TokenExchangeState tokenExchangeState = new TokenExchangeState()
            .withConnectionName(connectionName)
            .withConversation(new ConversationReference()
                    .withActivityId(activity.id())
                    .withBot(activity.recipient())
                    .withChannelId(activity.channelId())
                    .withConversation(activity.conversation())
                    .withServiceUrl(activity.serviceUrl())
                    .withUser(activity.from()))
            .withMsAppId((creds == null) ? null : creds.microsoftAppId());


        String serializedState = this.mapper.writeValueAsString(tokenExchangeState);
        String encoded = Base64.getEncoder().encodeToString(serializedState.getBytes(StandardCharsets.UTF_8));

        // Construct URL

        HashMap <String, String> qstrings = new HashMap<>();
        qstrings.put("state", encoded);
        String strUri = String.format("%sapi/botsignin/getsigninurl", this.uri);
        URI tokenUrl = MakeUri(strUri, qstrings);

        // add botframework api service url to the list of trusted service url's for these app credentials.
        MicrosoftAppCredentials.trustServiceUrl(tokenUrl);

        // Set Credentials and make call
        MicrosoftAppCredentials appCredentials = (MicrosoftAppCredentials) client.restClient().credentials();
        Response httpResponse = await(appCredentials.ProcessHttpRequestAsync(true, "GET", tokenUrl.toString()));

        int statusCode = httpResponse.code();
        if (statusCode == HTTP_OK) {
            return completedFuture(httpResponse.body().string());
        }
        return completedFuture(null);
    }

    /// <summary>
    /// Send a dummy OAuth card when the bot is being used on the emulator for testing without fetching a real token.
    /// </summary>
    /// <param name="emulateOAuthCards"></param>
    /// <returns></returns>
    public CompletableFuture SendEmulateOAuthCardsAsync(Boolean emulateOAuthCards) throws URISyntaxException, IOException {

        // Construct URL
        HashMap <String, String> qstrings = new HashMap<>();
        qstrings.put("emulate", emulateOAuthCards.toString());
        String strUri = String.format("%sapi/usertoken/emulateOAuthCards", this.uri);
        URI tokenUrl = MakeUri(strUri, qstrings);

        // add botframework api service url to the list of trusted service url's for these app credentials.
        MicrosoftAppCredentials.trustServiceUrl(tokenUrl);

        // Construct dummy body
        RequestBody body = RequestBody.create(JSON, "{}" );

        // Set Credentials and make call
        MicrosoftAppCredentials appCredentials = (MicrosoftAppCredentials) client.restClient().credentials();
        Response httpResponse = await(appCredentials.ProcessHttpRequestAsync(true, "POST", tokenUrl.toString(), body));

        // Apparently swallow any results
        return completedFuture(null);
    }
}
