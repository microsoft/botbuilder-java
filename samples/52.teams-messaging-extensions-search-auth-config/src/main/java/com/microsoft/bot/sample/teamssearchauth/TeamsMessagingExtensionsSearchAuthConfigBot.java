// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.teamssearchauth;

import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.UserTokenProvider;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.connector.ExecutorFactory;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.*;
import com.microsoft.bot.schema.teams.*;
import com.microsoft.graph.models.extensions.Message;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the users would be added. For this
 * sample, the {@link #onMessageActivity(TurnContext)} echos the text back to the user. The {@link
 * #onMembersAdded(List, TurnContext)} will send a greeting to new conversation participants.
 * </p>
 */
@Component
public class TeamsMessagingExtensionsSearchAuthConfigBot extends TeamsActivityHandler {

    private String appId;
    private String appPassword;
    private String siteUrl;
    private String connectionName;
    private UserState userState;
    private StatePropertyAccessor<String> userConfigProperty;

    public TeamsMessagingExtensionsSearchAuthConfigBot(
        Configuration configuration,
        UserState userState
    ) {
        appId = configuration.getProperty("MicrosoftAppId");
        appPassword = configuration.getProperty("MicrosoftAppPassword");
        connectionName = configuration.getProperty("ConnectionName");
        siteUrl = configuration.getProperty("SiteUrl");
        userConfigProperty = userState.createProperty("UserConfiguration");
        this.userState = userState;
    }

    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        return super.onTurn(turnContext)
            .thenCompose(saveResult -> userState.saveChanges(turnContext));
    }

    @Override
    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionConfigurationQuerySettingUrl(
        TurnContext turnContext,
        MessagingExtensionQuery query
    ) {
        return userConfigProperty.get(turnContext, () -> "").thenApply(userConfigSettings -> {
            AtomicReference<String> escapedSettings = new AtomicReference<>("");
            if (!StringUtils.isEmpty(userConfigSettings)) {
                try {
                    escapedSettings.set(
                        URLEncoder.encode(userConfigSettings, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    escapedSettings.set(userConfigSettings);
                }
            }

            return new MessagingExtensionResponse(new MessagingExtensionResult() {{
                setType("config");
                setSuggestedActions(new MessagingExtensionSuggestedAction() {{
                    setAction(new CardAction() {{
                        setType(ActionTypes.OPEN_URL);
                        setValue(String.format("%s/searchSettings.html?settings=%s",
                            siteUrl, escapedSettings.get()));
                    }});
                }});
            }});
        });
    }

    @Override
    protected CompletableFuture<Void> onTeamsMessagingExtensionConfigurationSetting(
        TurnContext turnContext,
        Object settings
    ) {
        if (settings != null) {
            Map<String, String> settingsData = (Map<String, String>) settings;
            String state = settingsData.get("state");
            return userConfigProperty.set(turnContext, state);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionQuery(
        TurnContext turnContext,
        MessagingExtensionQuery query
    ) {
        return userConfigProperty.get(turnContext)
            .thenCompose(settings -> {
                if (settings != null && settings.toLowerCase().contains("email")) {
                    return emailExtensionQuery(turnContext, query);
                }

                return packageExtensionQuery(query);
            });
    }

    private CompletableFuture<MessagingExtensionResponse> packageExtensionQuery(
        MessagingExtensionQuery query
    ) {
        String search = "";
        if (query.getParameters() != null && !query.getParameters().isEmpty()) {
            search = (String) query.getParameters().get(0).getValue();
        }

        return findPackages(search).thenApply(packages -> {
            List<MessagingExtensionAttachment> attachments = new ArrayList<>();
            for (String[] item : packages) {
                ThumbnailCard previewCard = new ThumbnailCard() {{
                    setTitle(item[0]);
                    setTap(new CardAction() {{
                        setType(ActionTypes.INVOKE);
                        setValue(new JSONObject().put("data", item).toString());
                    }});
                }};

                if (!StringUtils.isEmpty(item[4])) {
                    previewCard.setImage(new CardImage() {{
                        setUrl(item[4]);
                        setAlt("Icon");
                    }});
                }

                MessagingExtensionAttachment attachment = new MessagingExtensionAttachment() {{
                    setContentType(HeroCard.CONTENTTYPE);
                    setContent(new HeroCard() {{
                        setTitle(item[0]);
                    }});
                    setPreview(previewCard.toAttachment());
                }};

                attachments.add(attachment);
            }

            return new MessagingExtensionResponse(new MessagingExtensionResult() {{
                setType("result");
                setAttachmentLayout("list");
                setAttachments(attachments);
            }});
        });
    }

    private CompletableFuture<MessagingExtensionResponse> emailExtensionQuery(
        TurnContext turnContext,
        MessagingExtensionQuery query
    ) {
        // When the Bot Service Auth flow completes, the action.State will contain a
        // magic code used for verification.
        String magicCode = "";
        String state = query.getState();
        if (state != null && !state.isEmpty()) {
            magicCode = state;
        }

        UserTokenProvider tokenProvider = (UserTokenProvider) turnContext.getAdapter();
        return tokenProvider.getUserToken(turnContext, connectionName, magicCode)
            .thenApply(response -> {
                if (response == null || StringUtils.isEmpty(response.getToken())) {
                    // There is no token, so the user has not signed in yet.
                    String link = tokenProvider.getOauthSignInLink(turnContext, connectionName).join();
                        //.thenApply(link -> {
                            return new MessagingExtensionResponse() {{
                                setComposeExtension(new MessagingExtensionResult() {{
                                    setType("auth");
                                    setSuggestedActions(
                                        new MessagingExtensionSuggestedAction() {{
                                            setAction(
                                                new CardAction() {{
                                                    setType(ActionTypes.OPEN_URL);
                                                    setValue(link);
                                                    setTitle("Bot Service OAuth");
                                                }}
                                            );
                                        }}
                                    );
                                }});
                            }};
                        //});
                }

                String search = "";
                if (query.getParameters() != null && !query.getParameters().isEmpty()) {
                    search = (String) query.getParameters().get(0).getValue();
                }

                return new MessagingExtensionResponse(searchMail(search, response.getToken()));
            });
    }

    @Override
    protected CompletableFuture<MessagingExtensionResponse> onTeamsMessagingExtensionSelectItem(
        TurnContext turnContext,
        Object query
    ) {
        Map cardValue = (Map) query;
        List<String> data = (ArrayList) cardValue.get("data");
        ThumbnailCard card = new ThumbnailCard() {{
            setTitle(data.get(0));
            setSubtitle(data.get(2));
            setButtons(Arrays.asList(new CardAction() {{
                setType(ActionTypes.OPEN_URL);
                setTitle("Project");
                setValue(data.get(3));
            }}));
        }};

        if (!StringUtils.isEmpty(data.get(4))) {
            card.setImage(new CardImage() {{
                setUrl(data.get(4));
                setAlt("Icon");
            }});
        }

        MessagingExtensionAttachment attachment = new MessagingExtensionAttachment() {{
            setContentType(ThumbnailCard.CONTENTTYPE);
            setContent(card);
        }};

        MessagingExtensionResult composeExtension = new MessagingExtensionResult() {{
            setType("result");
            setAttachmentLayout("list");
            setAttachments(Collections.singletonList(attachment));
        }};

        return CompletableFuture.completedFuture(new MessagingExtensionResponse(composeExtension));
    }

    @Override
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionSubmitAction(
        TurnContext turnContext,
        MessagingExtensionAction action
    ) {
        return CompletableFuture.completedFuture(new MessagingExtensionActionResponse());
    }

    @Override
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionFetchTask(
        TurnContext turnContext,
        MessagingExtensionAction action
    ) {
        if (action.getCommandId().toUpperCase().equals("SIGNOUTCOMMAND")) {
            UserTokenProvider tokenProvider = (UserTokenProvider) turnContext.getAdapter();
            return tokenProvider.signOutUser(
                turnContext,
                connectionName,
                turnContext.getActivity().getFrom().getId()
            ).thenApply(response -> new MessagingExtensionActionResponse() {{
                setTask(new TaskModuleContinueResponse() {{
                    setValue(new TaskModuleTaskInfo() {{
                        setCard(createAdaptiveCardAttachment());
                        setHeight(200);
                        setWidth(400);
                        setTitle("Adaptive Card: Inputs");
                    }});
                }});
            }});
        }

        return notImplemented();
    }

    private Attachment createAdaptiveCardAttachment() {
        try (
            InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("adaptiveCard.json")
        ) {
            String adaptiveCardJson = IOUtils.toString(input, StandardCharsets.UTF_8.toString());

            return new Attachment() {{
                setContentType("application/vnd.microsoft.card.adaptive");
                setContent(Serialization.jsonToTree(adaptiveCardJson));
            }};
        } catch (IOException e) {
            (LoggerFactory.getLogger(TeamsMessagingExtensionsSearchAuthConfigBot.class))
                .error("Unable to createAdaptiveCardAttachment", e);
            return new Attachment();
        }
    }

    private MessagingExtensionResult searchMail(String text, String token) {
        SimpleGraphClient graph = new SimpleGraphClient(token);
        List<Message> messages = graph.searchMailInbox(text);

        List<MessagingExtensionAttachment> attachments = new ArrayList<>();
        for (Message msg : messages) {
            attachments.add(new MessagingExtensionAttachment() {{
                setContentType(HeroCard.CONTENTTYPE);
                setContent(new HeroCard() {{
                    setTitle(msg.from.emailAddress.address);
                    setSubtitle(msg.subject);
                    setText(msg.body.content);
                }});
                setPreview(new ThumbnailCard() {{
                    setTitle(msg.from.emailAddress.address);
                    setText(String.format("%s<br />%s", msg.subject, msg.bodyPreview));
                    setImage(new CardImage() {{
                        setUrl(
                            "https://raw.githubusercontent.com/microsoft/botbuilder-samples/master/docs/media/OutlookLogo.jpg"
                        );
                        setAlt("Outlook logo");
                    }});
                }}.toAttachment());
            }});
        }

        return new MessagingExtensionResult() {{
            setType("result");
            setAttachmentLayout("list");
            setAttachments(attachments);
        }};
    }

    private CompletableFuture<List<String[]>> findPackages(String text) {
        return CompletableFuture.supplyAsync(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(
                String.format(
                    "https://azuresearch-usnc.nuget.org/query?q=id:%s&prerelease=true",
                    text
                )
            ).build();

            List<String[]> filteredItems = new ArrayList<String[]>();
            try {
                Response response = client.newCall(request).execute();
                JSONObject obj = new JSONObject(response.body().string());
                JSONArray dataArray = (JSONArray) obj.get("data");

                dataArray.forEach(i -> {
                    JSONObject item = (JSONObject) i;
                    filteredItems.add(
                        new String[]{
                            item.getString("id"), item.getString("version"),
                            item.getString("description"),
                            item.has("projectUrl") ? item.getString("projectUrl") : "",
                            item.has("iconUrl") ? item.getString("iconUrl") : ""
                        }
                    );
                });

            } catch (IOException e) {
                LoggerFactory.getLogger(TeamsMessagingExtensionsSearchAuthConfigBot.class)
                    .error("findPackages", e);
                throw new CompletionException(e);
            }
            return filteredItems;
        }, ExecutorFactory.getExecutor());
    }
}
