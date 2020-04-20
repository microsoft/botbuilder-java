// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.teamssearch;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.*;
import com.microsoft.bot.schema.teams.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>This is where application specific logic for interacting with the users would be
 * added.  This sample illustrates how to build a Search-based Messaging Extension.</p>
 */

@Component
public class TeamsMessagingExtensionsSearchBot extends TeamsActivityHandler {
    private String appId;
    private String appPassword;
    private static final Logger botLogger = LogManager.getLogger();

    public TeamsMessagingExtensionsSearchBot(Configuration configuration) {
        appId = configuration.getProperty("MicrosoftAppId");
        appPassword = configuration.getProperty("MicrosoftAppPassword");
    }

    @Override
    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionQuery(
        TurnContext turnContext,
        MessagingExtensionQuery query) {
        List<MessagingExtensionParameter> queryParams = query.getParameters();
        String text = "";
        if (queryParams != null && !queryParams.isEmpty()) {
            text = (String) queryParams.get(0).getValue();
        }

        List<String []> packages = null;

        try {
            packages = FindPackages(text).get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            packages = new ArrayList<String[]>();
        }

        List<MessagingExtensionAttachment> attachments = new ArrayList<>();
        for (String [] item: packages) {
            ThumbnailCard previewCard = new ThumbnailCard(){{
                setTitle(item[0]);
                setTap(new CardAction(){{
                    setType(ActionTypes.INVOKE);
                    setValue(new JSONObject().put("data", item).toString());
                }});
            }};

            if (!StringUtils.isEmpty(item[4])) {
                previewCard.setImages(Collections.singletonList(new CardImage(){{
                    setUrl(item[4]);
                    setAlt("Icon");
                }}));
            }

            MessagingExtensionAttachment attachment = new MessagingExtensionAttachment(){{
                setContentType(HeroCard.CONTENTTYPE);
                setContent(new HeroCard(){{
                    setTitle(item[0]);
                }});
                setPreview(previewCard.toAttachment());
            }};

            attachments.add(attachment);
        }


        MessagingExtensionResult composeExtension = new MessagingExtensionResult(){{
            setType("result");
            setAttachmentLayout("list");
            setAttachments(attachments);
        }};

        return CompletableFuture.completedFuture(new MessagingExtensionResponse(composeExtension));
    }

    @Override
    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionSelectItem(
        TurnContext turnContext,
        Object query) {

        LinkedHashMap cardValue = ((LinkedHashMap) query);
        List<String> data = (ArrayList) cardValue.get("data");
        ThumbnailCard card  = new ThumbnailCard(){{
            setTitle(data.get(0));
            setSubtitle(data.get(2));
            setButtons(Arrays.asList(new CardAction(){{
                setType(ActionTypes.OPEN_URL);
                setTitle("Project");
                setValue(data.get(3));
            }}));
        }};

        if (!StringUtils.isEmpty(data.get(4))) {
            card.setImages(Collections.singletonList(new CardImage(){{
                setUrl(data.get(4));
                setAlt("Icon");
            }}));
        }

        MessagingExtensionAttachment attachment = new MessagingExtensionAttachment(){{
            setContentType(ThumbnailCard.CONTENTTYPE);
            setContent(card);
        }};

        MessagingExtensionResult composeExtension = new MessagingExtensionResult(){{
            setType("result");
            setAttachmentLayout("list");
            setAttachments(Collections.singletonList(attachment));
        }};
        return CompletableFuture.completedFuture(new MessagingExtensionResponse(composeExtension));
    }

    private CompletableFuture<List<String []>> FindPackages(
        String text) throws IOException {
        return CompletableFuture.supplyAsync(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                .url(String.format("https://azuresearch-usnc.nuget.org/query?q=id:%s&prerelease=true", text))
                .build();

            List<String []> filteredItems = new ArrayList<String []>();
            try {
                Response response = client.newCall(request).execute();
                JSONObject obj = new JSONObject(response.body().string());
                JSONArray dataArray = (JSONArray) obj.get("data");

                dataArray.forEach(i -> {
                    JSONObject item = (JSONObject) i;
                    filteredItems.add(new String [] {
                        item.getString("id"),
                        item.getString("version"),
                        item.getString("description"),
                        item.has("projectUrl") ? item.getString("projectUrl") : "",
                        item.has("iconUrl") ? item.getString("iconUrl") : ""
                    });
                });

            } catch (IOException e) {
                botLogger.log(Level.ERROR, e.getStackTrace());
            }
            return filteredItems;
        });

    }
}
