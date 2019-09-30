package com.microsoft.bot.sample.servlet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.ClasspathPropertiesConfiguration;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.Activity;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public abstract class ControllerBase extends HttpServlet {
    private ObjectMapper objectMapper;
    private BotFrameworkHttpAdapter adapter;
    private Bot bot;

    @Override
    public void init() {
        objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();

        Configuration configuration = getConfiguration();
        adapter = getBotFrameworkHttpAdaptor(configuration);
        bot = getBot();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try (PrintWriter out = response.getWriter()) {
            out.println("hello world");
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (Throwable t) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            Activity activity = getActivity(request);
            String authHeader = request.getHeader("Authorization");

            adapter.processIncomingActivity(authHeader, activity, turnContext -> bot.onTurn(turnContext))
                .handle((result, exception) -> {
                    if (exception == null) {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        return null;
                    }

                    if (exception.getCause() instanceof AuthenticationException) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }

                    return null;
                });
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected Configuration getConfiguration() {
        return new ClasspathPropertiesConfiguration();
    }

    protected BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        return new BotFrameworkHttpAdapter(configuration);
    }

    protected abstract Bot getBot();

    // Creates an Activity object from the request
    private Activity getActivity(HttpServletRequest request) throws IOException {
        String body = getRequestBody(request);
        return objectMapper.readValue(body, Activity.class);
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder buffer = new StringBuilder();
        InputStream stream = request.getInputStream();
        int rByte;
        while ((rByte = stream.read()) != -1) {
            buffer.append((char) rByte);
        }
        stream.close();
        if (buffer.length() > 0) {
            return buffer.toString();
        }
        return "";
    }
}
