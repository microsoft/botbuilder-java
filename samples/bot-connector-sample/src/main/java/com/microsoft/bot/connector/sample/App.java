package com.microsoft.bot.connector.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.connector.customizations.*;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ActivityTypes;
import com.microsoft.bot.schema.models.ResourceResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    private static final Logger LOGGER = Logger.getLogger( App.class.getName() );
    private static String appId = "be5a8c67-75dd-424a-b7da-746141e11f7e";
    private static String appPassword = "poR3ZiMgA3JSUkFOcSjtUGp";

    public static void main( String[] args ) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3978), 0);
        server.createContext("/api/messages", new MessageHandle());
        server.setExecutor(null);
        server.start();
    }

    static class MessageHandle implements HttpHandler {
        private ObjectMapper objectMapper;
        private BotAuthenticator authenticator;

        MessageHandle() {
            objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            BotCredentials credentials = new BotCredentials()
                    .withAppId(appId)
                    .withAppPassword(appPassword);
            authenticator = new BotAuthenticator(credentials);
        }

        public void handle(HttpExchange httpExchange) throws IOException {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Activity activity = getActivity(httpExchange);
                if (activity != null && activity.type() == ActivityTypes.MESSAGE) {
                    String authHeader = httpExchange.getRequestHeaders().getFirst("Authorization");
                    try {
                        JwtTokenValidation.assertValidActivity(activity, authHeader, new CredentialProviderImpl(appId, appPassword)).get();
                    } catch (ExecutionException e) {
                        LOGGER.log(Level.WARNING, "auth failed! - [execution]", e);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, "auth failed! - [interrupt]", e);
                    }

                    ConnectorClientImpl connector = new ConnectorClientImpl(activity.serviceUrl(), new MicrosoftAppCredentials(appId, appPassword));
                    // send ack to user activity
                    httpExchange.sendResponseHeaders(202, 0);
                    httpExchange.getResponseBody().close();
                    // reply activity with the same text
                    ResourceResponse response = connector.conversations().sendToConversation(activity.conversation().id(),
                            new Activity()
                                    .withType(ActivityTypes.MESSAGE)
                                    .withText("Echo: " + activity.text())
                                    .withRecipient(activity.from())
                                    .withFrom(activity.recipient())
                    );
                }
            }
        }

        private String getRequestBody(HttpExchange httpExchange) throws IOException {
            StringBuilder buffer = new StringBuilder();
            InputStream stream = httpExchange.getRequestBody();
            int rByte;
            while ((rByte = stream.read()) != -1) {
                buffer.append((char)rByte);
            }
            stream.close();
            if (buffer.length() > 0) {
                return URLDecoder.decode(buffer.toString(), "UTF-8");
            }
            return "";
        }

        private Activity getActivity(HttpExchange httpExchange) throws IOException {
            String body = getRequestBody(httpExchange);
            try {
                return objectMapper.readValue(body, Activity.class);
            } catch (Exception ex) {
                return null;
            }

        }
    }
}
