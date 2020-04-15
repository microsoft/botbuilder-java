// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.teamsactionpreview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.teams.*;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>This is where application specific logic for interacting with the users would be
 * added.  For this sample, the {@link #onMessageActivity(TurnContext)} echos the text
 * back to the user.  The {@link #onMembersAdded(List, TurnContext)} will send a greeting
 * to new conversation participants.</p>
 */
@Component
public class TeamsMessagingExtensionsActionPreviewBot extends TeamsActivityHandler {
    private String appId;
    private String appPassword;

    public TeamsMessagingExtensionsActionPreviewBot(Configuration configuration) {
        appId = configuration.getProperty("MicrosoftAppId");
        appPassword = configuration.getProperty("MicrosoftAppPassword");
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(
        TurnContext turnContext) {
        if (turnContext.getActivity().getValue() != null) {
            // This was a message from the card.
            LinkedHashMap obj = (LinkedHashMap) turnContext.getActivity().getValue();
            String answer = (String) obj.get("Answer");
            String choices = (String) obj.get("Choices");
         return turnContext.sendActivity(
             MessageFactory.text(
                 String.format("%1$s answered '%2$s' and chose '%3$s",
                     turnContext.getActivity().getFrom().getName(),
                     answer,
                     choices)))
             .thenApply(resourceResponse -> null);
        }

        // This is a regular text message.
        return turnContext.sendActivity(MessageFactory.text("Hello from the TeamsMessagingExtensionsActionPreviewBot."))
            .thenApply(resourceResponse -> null);
    }

    @Override
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionFetchTask(
        TurnContext turnContext,
        MessagingExtensionAction action) {

        Attachment adaptiveCardEditor = getAdaptiveCardAttachment("adaptiveCardEditor.json");

        return CompletableFuture.completedFuture(new MessagingExtensionActionResponse(){{
            setTask(new TaskModuleContinueResponse(){{
                setValue(new TaskModuleTaskInfo(){{
                    setCard(adaptiveCardEditor);
                    setWidth(500);
                    setHeight(450);
                    setTitle("Task Module Fetch Example");
                }});
                setType("continue");
            }});
        }});
    }

    @Override
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionSubmitAction(
        TurnContext turnContext,
        MessagingExtensionAction action) {

        Attachment adaptiveCard = getAdaptiveCardAttachment("submitCard.json");

        updateAttachmentAdaptiveCard(adaptiveCard, action);

        return CompletableFuture.completedFuture(new MessagingExtensionActionResponse(){{
            setComposeExtension(new MessagingExtensionResult(){{
                setType("botMessagePreview");
                setActivityPreview(MessageFactory.attachment(adaptiveCard));
            }});
        }});
    }

    @Override
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewEdit(
        TurnContext turnContext,
        MessagingExtensionAction action) {

        // This is a preview edit call and so this time we want to re-create the adaptive card editor.
        Attachment adaptiveCard = getAdaptiveCardAttachment("adaptiveCardEditor.json");

        Activity preview = action.getBotActivityPreview().get(0);
        Attachment previewCard = preview.getAttachments().get(0);
        updateAttachmentAdaptiveCardEdit(adaptiveCard, previewCard);

        return CompletableFuture.completedFuture(new MessagingExtensionActionResponse(){{
            setTask(new TaskModuleContinueResponse(){{
                setValue(new TaskModuleTaskInfo(){{
                    setCard(adaptiveCard);
                    setHeight(450);
                    setWidth(500);
                    setTitle("Task Module Fetch Example");
                }});
                setType("continue");
            }});
        }});
    }

    @Override
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionBotMessagePreviewSend(
        TurnContext turnContext,
        MessagingExtensionAction action) {
        // The data has been returned to the bot in the action structure.

        Activity preview = action.getBotActivityPreview().get(0);
        Attachment previewCard = preview.getAttachments().get(0);

        Activity message = MessageFactory.attachment(previewCard);

        // THIS WILL WORK IF THE BOT IS INSTALLED. (SendActivityAsync will throw if the bot is not installed.)
        turnContext.sendActivity(message)
            .thenApply(resourceResponse -> null);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected CompletableFuture<Void> onTeamsMessagingExtensionCardButtonClicked(
        TurnContext turnContext,
        Object cardData) {
        // If the adaptive card was added to the compose window (by either the OnTeamsMessagingExtensionSubmitActionAsync or
        // OnTeamsMessagingExtensionBotMessagePreviewSendAsync handler's return values) the submit values will come in here.
        Activity reply = MessageFactory.text("OnTeamsMessagingExtensionCardButtonClickedAsync Value: ");
        return turnContext.sendActivity(reply)
            .thenApply(resourceResponse -> null);
    }

    private Attachment getAdaptiveCardAttachment(String fileName) {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream(fileName);
            String content = IOUtils.toString(input, StandardCharsets.UTF_8);

            return new Attachment(){{
                setContentType("application/vnd.microsoft.card.adaptive");
                setContent(new ObjectMapper().readValue(content, ObjectNode.class));
            }};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Attachment();
    }

    private void updateAttachmentAdaptiveCard(
        Attachment attachment,
        MessagingExtensionAction action
    ){
        LinkedHashMap data = (LinkedHashMap) action.getData();
        ObjectNode content = (ObjectNode) attachment.getContent();
        JsonNode body = content.get("body");
        for (JsonNode arrayItem : body) {
            if (arrayItem.has("choices")){
                JsonNode choices = arrayItem.get("choices");
                for (int index = 0 ; index < 3 ; index++) {
                    ObjectNode choice = (ObjectNode) choices.get(index);
                    choice.put("title", (String) data.get("Option" + (index + 1)));
                    choice.put("value", (String) data.get("Option" + (index + 1)));
                }
            }

            if(arrayItem.has("id") && arrayItem.get("id").asText().equals("Question")){
                ObjectNode question = (ObjectNode) arrayItem;
                question.put("text", (String) data.get("Question"));
            }
        }
    }

    private void updateAttachmentAdaptiveCardEdit(
        Attachment attachment,
        Attachment preview
    ){
        LinkedHashMap data = (LinkedHashMap)preview.getContent();
        List bodyPreview = (ArrayList<LinkedHashMap>) data.get("body");
        ObjectNode content = (ObjectNode) attachment.getContent();
        JsonNode body = content.get("body");
        for (JsonNode arrayItem : body) {

            if(arrayItem.has("id") && arrayItem.get("id").asText().equals("Question")){
                ObjectNode question = (ObjectNode) arrayItem;
                LinkedHashMap previewQuestion = (LinkedHashMap) bodyPreview.get(1);
                question.put("value", (String) previewQuestion.get("text"));
            }

            if(arrayItem.has("id") && arrayItem.get("id").asText().startsWith("Option")){
                ObjectNode option = (ObjectNode) arrayItem;
                int responseIndex = Integer.parseInt(arrayItem.get("id").asText().charAt(6) + "");
                LinkedHashMap previewOptions = (LinkedHashMap) bodyPreview.get(3);
                List choices = (ArrayList) previewOptions.get("choices");
                LinkedHashMap previewOption = (LinkedHashMap) choices.get(responseIndex - 1);
                option.put("value", (String) previewOption.get("value"));
            }
        }
    }
}
