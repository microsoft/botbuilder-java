// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.servlet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.authentication.*;
import com.microsoft.bot.connector.rest.RestConnectorClient;
import com.microsoft.bot.integration.ClasspathPropertiesConfiguration;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.ConfigurationChannelProvider;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the Servlet that will receive incoming Channel Activity messages.
 */
@WebServlet(name = "EchoServlet", urlPatterns = "/api/messages")
public class EchoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EchoServlet.class.getName());

    private ObjectMapper objectMapper;
    private CredentialProvider credentialProvider;
    private MicrosoftAppCredentials credentials;
    private Configuration configuration;
    private ChannelProvider channelProvider;

    @Override
    public void init() throws ServletException {
        objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();

        // Load the application.properties from the classpath
        configuration = new ClasspathPropertiesConfiguration();
        String appId = configuration.getProperty("MicrosoftAppId");
        String appPassword = configuration.getProperty("MicrosoftAppPassword");

        credentialProvider = new SimpleCredentialProvider(appId, appPassword);
        channelProvider = new ConfigurationChannelProvider(configuration);

        if (channelProvider.isGovernment()) {
            credentials = new MicrosoftGovernmentAppCredentials(appId, appPassword);
        } else {
            credentials = new MicrosoftAppCredentials(appId, appPassword);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            Activity activity = getActivity(request);
            String authHeader = request.getHeader("Authorization");

            JwtTokenValidation.authenticateRequest(activity, authHeader, credentialProvider, channelProvider)
                .thenAccept(identity -> {
                   if (activity.getType().equals(ActivityTypes.MESSAGE)) {
                       // reply activity with the same text
                       ConnectorClient connector = new RestConnectorClient(activity.getServiceUrl(), credentials);
                       connector.getConversations().sendToConversation(
                           activity.getConversation().getId(),
                           activity.createReply("Echo: " + activity.getText()));
                   }
                })
                .join();

            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof AuthenticationException) {
                LOGGER.log(Level.WARNING, "Auth failed!", ex);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                LOGGER.log(Level.WARNING, "Execution failed", ex);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Execution failed", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Creates an Activity object from the request
    private Activity getActivity(HttpServletRequest request) throws IOException, JsonParseException, JsonMappingException {
        String body = getRequestBody(request);
        LOGGER.log(Level.INFO, body);
        return objectMapper.readValue(body, Activity.class);
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder buffer = new StringBuilder();
        InputStream stream = request.getInputStream();
        int rByte;
        while ((rByte = stream.read()) != -1) {
            buffer.append((char)rByte);
        }
        stream.close();
        if (buffer.length() > 0) {
            return buffer.toString();
        }
        return "";
    }
}
