package com.microsoft.bot.connector.authentication;

import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.TokenResponse;
import com.microsoft.rest.ServiceClient;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/// <summary>
/// Service client to handle requests to the botframework api service.
/// </summary>
public class OAuthClient extends ServiceClient {
    private final ConnectorClient _client;
    private final String _uri;


    public OAuthClient(ConnectorClient client, String uri) throws URISyntaxException, MalformedURLException {
        super(client.restClient());
        URI uriResult = new URI(uri);

        // Sanity check our url
        uriResult.toURL();

        if (new URI(uri).getScheme() == "https")
            throw new IllegalArgumentException("Please supply a valid https uri");
        if (client == null)
            throw new IllegalArgumentException("client");
        this._client = client;
        this._uri = uri;
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
    public CompletableFuture<TokenResponse> GetUserTokenAsync(String userId, String connectionName, String magicCode) {
        return GetUserTokenAsync(userId, connectionName, magicCode, null);
    }

    public CompletableFuture<TokenResponse> GetUserTokenAsync(String userId, String connectionName, String magicCode, HashMap<String, ArrayList<String>> customHeaders) {
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId");
        }
        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName");
        }

        // Tracing
//        boolean shouldTrace = ServiceClientTracing.IsEnabled;
//         String invocationId = null;
//        if (shouldTrace)
//        {
//        invocationId =  ServiceClientTracing.NextInvocationId.ToString();
//        HashMap<String, Object> tracingParameters = new HashMap<String, Object>();
//        tracingParameters.put("userId", userId);
//        tracingParameters.put("connectionName", connectionName);
//        tracingParameters.put("magicCode", magicCode);
//        //tracingParameters.put("cancellationToken", cancellationToken);
//        // ServiceClientTracing.Enter(invocationId, this, "GetUserTokenAsync", tracingParameters);
//        }
        // Construct URL
//        var tokenUrl = new Uri(new Uri(_uri + (_uri.EndsWith("/") ? "" : "/")), "api/usertoken/GetToken?userId={userId}&connectionName={connectionName}{magicCodeParam}").ToString();
//        tokenUrl = tokenUrl.Replace("{connectionName}", Uri.EscapeDataString(connectionName));
//        tokenUrl = tokenUrl.Replace("{userId}", Uri.EscapeDataString(userId));
//        if (!StringUtils.isEmpty(magicCode))
//        {
//        tokenUrl = tokenUrl.Replace("{magicCodeParam}", $"&code={Uri.EscapeDataString(magicCode)}");
//        }
//        else
//        {
//        tokenUrl = tokenUrl.Replace("{magicCodeParam}", String.Empty);
//        }

        // Create HTTP transport objects
//        var httpRequest = new HttpRequestMessage();
//        HttpResponseMessage httpResponse = null;
//        httpRequest.Method = new HttpMethod("GET");
//        httpRequest.RequestUri = new Uri(tokenUrl);

        // add botframework api service url to the list of trusted service url's for these app credentials.
//        MicrosoftAppCredentials.trustServiceUrl(tokenUrl);

        // Set Credentials
//        if (_client.Credentials != null)
//        {
//        cancellationToken.ThrowIfCancellationRequested();
//        await(_client.Credentials.ProcessHttpRequestAsync(httpRequest));
//        }
//        cancellationToken.ThrowIfCancellationRequested();

//        if (shouldTrace)
//        {
//         ServiceClientTracing.SendRequest(invocationId, httpRequest);
//        }
//        httpResponse = await(_client.HttpClient.SendAsync(httpRequest, cancellationToken));
//        if (shouldTrace)
//        {
//         ServiceClientTracing.ReceiveResponse(invocationId, httpResponse);
//        }
//        HttpStatusCode statusCode = httpResponse.StatusCode;
//        cancellationToken.ThrowIfCancellationRequested();
//        if (statusCode == HttpStatusCode.OK)
//        {
//         String responseContent = await(httpResponse.Content.ReadAsStringAsync());
//        try
//        {
//        var tokenResponse = Rest.Serialization.SafeJsonConvert.DeserializeObject<TokenResponse>(responseContent);
//        return tokenResponse;
//        }
//        catch (JsonException)
//        {
//        // ignore json exception and return null
//        httpRequest.Dispose();
//        if (httpResponse != null)
//        {
//        httpResponse.Dispose();
//        }
        return null;
    }
    //}
//        else if (statusCode == HttpStatusCode.NotFound)
//        {
//        return null;
//        }
//        else
//        {
//        return null;
//        }
//        }

    /// <summary>
/// Signs Out the User for the given ConnectionName.
/// </summary>
/// <param name="userId"></param>
/// <param name="connectionName"></param>
/// <param name="cancellationToken"></param>
/// <returns></returns>
    public CompletableFuture<Boolean> SignOutUserAsync(String userId, String connectionName) {
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId");
        }
        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName");
        }

        //boolean shouldTrace = // ServiceClientTracing.IsEnabled;
        String invocationId = null;
//        if (shouldTrace)
//        {
//        invocationId = // ServiceClientTracing.NextInvocationId.ToString();
//        HashMap<String, Object> tracingParameters = new HashMap<String, Object>();
//        tracingParameters.put("userId", userId);
//        tracingParameters.put("connectionName", connectionName);
//        
//        // ServiceClientTracing.Enter(invocationId, this, "SignOutUserAsync", tracingParameters);
//        }

        // Construct URL
//        var tokenUrl = new Uri(new Uri(_uri + (_uri.EndsWith("/") ? "" : "/")), "api/usertoken/SignOut?&userId={userId}&connectionName={connectionName}").ToString();
//        tokenUrl = tokenUrl.Replace("{connectionName}", Uri.EscapeDataString(connectionName));
//        tokenUrl = tokenUrl.Replace("{userId}", Uri.EscapeDataString(userId));
//
//        // add botframework api service url to the list of trusted service url's for these app credentials.
//        MicrosoftAppCredentials.TrustServiceUrl(tokenUrl);
//
//        // Create HTTP transport objects
//        var httpRequest = new HttpRequestMessage();
//        HttpResponseMessage httpResponse = null;
//        httpRequest.Method = new HttpMethod("DELETE");
//        httpRequest.RequestUri = new Uri(tokenUrl);
//
//        // Set Credentials
//        if (_client.Credentials != null)
//        {
//        cancellationToken.ThrowIfCancellationRequested();
//        await(_client.Credentials.ProcessHttpRequestAsync(httpRequest, cancellationToken));
//        }
//        cancellationToken.ThrowIfCancellationRequested();
//
//        if (shouldTrace)
//        {
//        // ServiceClientTracing.SendRequest(invocationId, httpRequest);
//        }
//        httpResponse = await(_client.HttpClient.SendAsync(httpRequest, cancellationToken));
//        if (shouldTrace)
//        {
//        // ServiceClientTracing.ReceiveResponse(invocationId, httpResponse);
//        }
//
//        HttpStatusCode _statusCode = httpResponse.StatusCode;
//        cancellationToken.ThrowIfCancellationRequested();
//        if (_statusCode == HttpStatusCode.OK)
//        {
//        return true;
//        }
//        else
//        {
//        return false;
//        }
        return completedFuture(false);
    }

    /// <summary>
/// Gets the Link to be sent to the user for signin into the given ConnectionName
/// </summary>
/// <param name="activity"></param>
/// <param name="connectionName"></param>
/// <param name="cancellationToken"></param>
/// <returns></returns>
    public CompletableFuture<String> GetSignInLinkAsync(Activity activity, String connectionName) {
        if (StringUtils.isEmpty(connectionName)) {
            throw new IllegalArgumentException("connectionName");
        }
        if (activity == null) {
            throw new IllegalArgumentException("activity");
        }

        //boolean shouldTrace = // ServiceClientTracing.IsEnabled;
        String invocationId = null;
//        if (shouldTrace)
//        {
//        invocationId = // ServiceClientTracing.NextInvocationId.ToString();
//        HashMap<String, object> tracingParameters = new HashMap<String, object>();
//        tracingParameters.put("activity", activity);
//        tracingParameters.put("connectionName", connectionName);
//        tracingParameters.put("cancellationToken", cancellationToken);
//        // ServiceClientTracing.Enter(invocationId, this, "GetSignInLinkAsync", tracingParameters);
//        }

//        TokenExchangeState tokenExchangeState = new TokenExchangeState()
//        {
//        ConnectionName = connectionName,
//        Conversation = new ConversationReference()
//        {
//        ActivityId = activity.Id,
//        Bot = activity.Recipient,       // Activity is from the user to the bot
//        ChannelId = activity.ChannelId,
//        Conversation = activity.Conversation,
//        ServiceUrl = activity.ServiceUrl,
//        User = activity.From
//        },
//        MsAppId = (_client.Credentials as MicrosoftAppCredentials)?.MicrosoftAppId
//        };

//        var serializedState = JsonConvert.SerializeObject(tokenExchangeState);
//        var encodedState = Encoding.UTF8.GetBytes(serializedState);
//        var finalState = Convert.ToBase64String(encodedState);
//
//        // Construct URL
//        var tokenUrl = new Uri(new Uri(_uri + (_uri.EndsWith("/") ? "" : "/")), "api/botsignin/getsigninurl?&state={state}").ToString();
//        tokenUrl = tokenUrl.Replace("{state}", finalState);
//
//        // add botframework api service url to the list of trusted service url's for these app credentials.
//        MicrosoftAppCredentials.TrustServiceUrl(tokenUrl);
//
//        // Create HTTP transport objects
//        var httpRequest = new HttpRequestMessage();
//        HttpResponseMessage httpResponse = null;
//        httpRequest.Method = new HttpMethod("GET");
//        httpRequest.RequestUri = new Uri(tokenUrl);
//
//        // Set Credentials
//        if (_client.Credentials != null)
//        {
//        cancellationToken.ThrowIfCancellationRequested();
//        await(_client.Credentials.ProcessHttpRequestAsync(httpRequest, cancellationToken));
//        }
//        cancellationToken.ThrowIfCancellationRequested();
//
//        if (shouldTrace)
//        {
//        // ServiceClientTracing.SendRequest(invocationId, httpRequest);
//        }
//        httpResponse = await(_client.HttpClient.SendAsync(httpRequest, cancellationToken));
//        if (shouldTrace)
//        {
//        // ServiceClientTracing.ReceiveResponse(invocationId, httpResponse);
//        }
//
//        HttpStatusCode statusCode = httpResponse.StatusCode;
//        cancellationToken.ThrowIfCancellationRequested();
//        if (statusCode == HttpStatusCode.OK)
//        {
//        var link = await(httpResponse.Content.ReadAsStringAsync());
//        return link;
//        }
        return null;
    }

    /// <summary>
/// Send a dummy OAuth card when the bot is being used on the emulator for testing without fetching a real token.
/// </summary>
/// <param name="emulateOAuthCards"></param>
/// <returns></returns>
    public CompletableFuture SendEmulateOAuthCardsAsync(boolean emulateOAuthCards) {
        //boolean shouldTrace = // ServiceClientTracing.IsEnabled;
        String invocationId = null;
//        if (shouldTrace)
//        {
//        invocationId = // ServiceClientTracing.NextInvocationId.ToString();
//        HashMap<String, Object> tracingParameters = new HashMap<String, Object>();
//        tracingParameters.put("emulateOAuthCards", emulateOAuthCards);
//        // ServiceClientTracing.Enter(invocationId, this, "SendEmulateOAuthCards", tracingParameters);
//        }


        // Construct URL
//        URL tokenUrl = new Uri(new Uri(_uri + (_uri.EndsWith("/") ? "" : "/")), "api/usertoken/emulateOAuthCards?emulate={emulate}").ToString();
//        tokenUrl = tokenUrl.Replace("{emulate}", emulateOAuthCards.toString());
//
//        // Create HTTP transport objects
//        var httpRequest = new HttpRequestMessage();
//        HttpResponseMessage httpResponse = null;
//        httpRequest.Method = new HttpMethod("POST");
//        httpRequest.RequestUri = new Uri(tokenUrl);
//
//        // add botframework api service url to the list of trusted service url's for these app credentials.
//        MicrosoftAppCredentials.trustServiceUrl(tokenUrl);

        // Set Credentials
//        if (_client.Credentials != null)
//        {
//        await(_client.Credentials.ProcessHttpRequestAsync(httpRequest, cancellationToken));
//        }
        return completedFuture(null);
    }
}
