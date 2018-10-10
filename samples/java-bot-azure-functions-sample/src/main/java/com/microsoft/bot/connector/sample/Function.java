/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.bot.connector.sample;

import java.util.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.CredentialProviderImpl;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ActivityTypes;
import com.microsoft.bot.schema.models.ResourceResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Bot Builder SDK v4 for Java with Azure Functions Sample Application.
 */
public class Function {

    private final static String BOT_APP_ID = "";        // <-- app id -->
    private final static String BOT_APP_PASSWORD = "";  // <-- app password -->
    private final static String AUTH_HEADER_NAME = "authorization";

    private final static CredentialProvider CREDENTIAL_PROVIDER;
    private final static MicrosoftAppCredentials CREDENTIALS;

    static {
        CREDENTIAL_PROVIDER = new CredentialProviderImpl(BOT_APP_ID, BOT_APP_PASSWORD);
        CREDENTIALS = new MicrosoftAppCredentials(BOT_APP_ID, BOT_APP_PASSWORD);
    }

    /**
     * Message Endpoint of this Function will be following.
     * DEPLOIED-FUNCTION-SERVER-NAME/api/java-bot  
     * 
     * @param request
     * @param context
     * @return
     */
    @FunctionName("java-bot")
    public HttpResponseMessage HttpTriggerJavaForBot(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        HttpMethod httpMethod = request.getHttpMethod();
        if (httpMethod.equals(HttpMethod.OPTIONS)) {
            return request.createResponseBuilder(HttpStatus.ACCEPTED).build();
        }

        if (httpMethod.equals(HttpMethod.POST)) {
            Optional<String> body = request.getBody();
            body.ifPresent(bodyMessage -> {
                if (bodyMessage.equals("")) {
                    return;
                }
                try {
                    String authHeader = request.getHeaders().get("AUTH_HEADER_NAME");
                    final Activity activity = getActivity(bodyMessage);

                    CompletableFuture<ClaimsIdentity> authenticateRequest = JwtTokenValidation.authenticateRequest(activity, authHeader, CREDENTIAL_PROVIDER);
                    authenticateRequest.thenRunAsync(() -> {
                        if (activity.type().equals(ActivityTypes.MESSAGE)) {
                            ConnectorClientImpl connector = new ConnectorClientImpl(activity.serviceUrl(), CREDENTIALS);
                            sendMessageToBotFramework(connector, activity, "Echo: " + activity.text());
                        }
                    });
                } catch (AuthenticationException | IOException | InterruptedException | ExecutionException e) {
                    context.getLogger().severe(e.getMessage());
                    //e.printStackTrace();
                }
            });
        }
        return request.createResponseBuilder(HttpStatus.OK).body("OK").build();
    }

    private void sendMessageToBotFramework(ConnectorClientImpl connector, Activity activity, String message) {
        ResourceResponse response = connector.conversations().sendToConversation(activity.conversation().id(),
                new Activity()
                        .withType(ActivityTypes.MESSAGE)
                        .withText(message)
                        .withRecipient(activity.from())
                        .withFrom(activity.recipient())
        );
    }

    private Activity getActivity(String bodyMessage) throws IOException {
        //JSON Binding from JSON to Activity Object
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .findAndRegisterModules();
        objectMapper.registerModule(new JodaModule());
        Activity activity = objectMapper.readValue(bodyMessage, Activity.class);
        return activity;
    }
}
